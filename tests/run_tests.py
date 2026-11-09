#!/usr/bin/env python3
"""Dependency-free acceptance tests; run from the project root after make."""
import os
import difflib
import random
import re
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def normalize(text):
    return [' '.join(line.split()) for line in text.splitlines() if line.strip()]

def run(text):
    with tempfile.TemporaryDirectory() as tmp:
        path = Path(tmp) / 'input.txt'
        path.write_text(text)
        result = subprocess.run([os.environ.get('JAVA', 'java'), '-cp', str(ROOT), 'jobScheduler', str(path)],
                                capture_output=True, text=True, check=False)
        if result.returncode:
            raise AssertionError(
                f"Java exited with status {result.returncode}\n"
                f"Input:\n{text}\nstdout:\n{result.stdout}\nstderr:\n{result.stderr}")
        assert not result.stdout and not result.stderr, result
        return path.with_name('input_output_file.txt').read_text()

def reference(text):
    """List-based model independent of the production AVL, heap, and running queue."""
    lines = [line.split() for line in text.splitlines() if line.strip()]
    capacity = int(lines.pop(0)[1])
    jobs, cancelled, execution, output = {}, [], [], []
    short, now = False, 0
    counts = dict(insertions=0, extractions=0, removals=0, priority_updates=0, rebuilds=0)
    maximum = 0
    def complete(t):
        for j in jobs.values():
            if j['status'] == 'RUNNING' and j['end'] <= t:
                j['status'] = 'COMPLETED'
    def val(v):
        return 'null' if v is None else str(v)
    for tokens in lines:
        now = int(tokens[0]); complete(now)
        cmd, args = tokens[1], list(map(int, tokens[2:]))
        waiting = [j for j in jobs.values() if j['status'] == 'WAITING']
        if cmd == 'SUBMIT':
            id, priority, estimate, delta = args
            if id in jobs:
                output.append(f'ERROR: Job {id} already exists'); continue
            jobs[id] = dict(id=id, submitted=now, priority=priority, estimate=estimate,
                            delta=delta, start=None, actual=None, end=None, status='WAITING')
            counts['insertions'] += 1
            maximum = max(maximum, len(waiting) + 1)
            output.append(f'Job {id} submitted at {now} ms')
        elif cmd == 'PROCESS_NEXT':
            if not waiting:
                output.append('NO_WAITING_JOBS'); continue
            running = [j for j in jobs.values() if j['status'] == 'RUNNING']
            if len(running) == capacity:
                now = min(j['end'] for j in running); complete(now)
            key = lambda j: ((j['estimate'],) if short else ()) + (-j['priority'], j['submitted'], j['id'])
            j = min(waiting, key=key)
            j.update(start=now, actual=j['estimate']+j['delta'], end=now+j['estimate']+j['delta'], status='RUNNING')
            execution.append(j); counts['extractions'] += 1
            output.append(f"Job {j['id']} started at {now} ms")
        elif cmd == 'SHORT_JOBS':
            if not short: counts['rebuilds'] += 1
            short = True
            output.append('Scheduling mode changed to SHORT_JOBS')
        elif cmd == 'JOB_STATUS':
            output += ['JOB STATUS', 'Job Submitted Started Actual Time Completed Status']
            ordered = sorted((j for j in jobs.values() if j['status'] != 'CANCELLED'), key=lambda j:j['id']) + cancelled
            for j in ordered:
                output.append(' '.join(val(j[k]) for k in ['id','submitted','start','actual','end','status']))
            output.append('END JOB STATUS')
        else:
            id = args[0]; j = jobs.get(id)
            if j is None:
                output.append(f'ERROR: Job {id} not found'); continue
            if cmd == 'FIND':
                output.append(f"Job {id}: submitted={j['submitted']}, started={val(j['start'])}, actual_time={val(j['actual'])}, completed={val(j['end'])}, status={j['status']}")
            elif j['status'] != 'WAITING':
                action = 'cancelled' if cmd == 'CANCEL' else 'updated'
                prefix = '' if cmd == 'CANCEL' else ' priority'
                output.append(f"ERROR: Job {id}{prefix} cannot be {action} while status is {j['status']}")
            elif cmd == 'CANCEL':
                j['status'] = 'CANCELLED'; cancelled.append(j); counts['removals'] += 1
                output.append(f'Job {id} cancelled')
            else:
                j['priority'] = args[1]; counts['priority_updates'] += 1
                output.append(f'Job {id} priority updated to {args[1]}')
    output.append('EXECUTION_ORDER')
    output += [f"{j['id']} {j['start']} {j['end']}" for j in execution]
    output.append('END_EXECUTION_ORDER')
    return output, jobs, counts, maximum, short

def check_model(text, actual):
    expected, jobs, counts, maximum, short = reference(text)
    functional = actual.split('\nTREE\n')[0]
    assert normalize(functional) == normalize('\n'.join(expected)), '\n'.join(difflib.unified_diff(normalize('\n'.join(expected)), normalize(functional)))
    live = sorted(id for id,j in jobs.items() if j['status'] != 'CANCELLED')
    assert f'INORDER: {live}' in actual
    levels = re.findall(r'LEVEL \d+: (.*)', actual)
    nodes = {}
    for depth, level in enumerate(levels):
        for index, token in enumerate(level.split('|')):
            if token != 'NULL': nodes[(depth,index)] = int(token.strip('[]'))
    def visit(depth, index, low=float('-inf'), high=float('inf')):
        key = nodes.get((depth,index))
        if key is None: return 0, []
        assert low < key < high
        lh, left = visit(depth+1,index*2,low,key)
        rh, right = visit(depth+1,index*2+1,key,high)
        assert abs(lh-rh) <= 1
        return 1+max(lh,rh), left+[key]+right
    height, ids = visit(0,0)
    assert ids == live and len(nodes) == len(live)
    tree = dict(re.findall(r'(\w+)=(\d+)', actual.split('TREE_STATS\n')[1].split('END_TREE_STATS')[0]))
    assert int(tree['records']) == len(live) and int(tree['height']) == height
    assert int(tree['insertions']) == counts['insertions'] and int(tree['deletions']) == counts['removals']
    heap = [tuple(map(int,t)) for t in re.findall(r'\((-?\d+),(-?\d+),(-?\d+)\)',actual.split('MAX_HEAP: ')[1].split('\n')[0])]
    waiting = {id:j for id,j in jobs.items() if j['status'] == 'WAITING'}
    assert sorted(t[0] for t in heap) == sorted(waiting)
    def rank(entry):
        id, priority, estimate = entry
        j = waiting[id]
        assert (priority,estimate) == (j['priority'],j['estimate'])
        return ((estimate,) if short else ()) + (-priority,j['submitted'],id)
    for i,entry in enumerate(heap):
        rank(entry)
        if i: assert rank(heap[(i-1)//2]) <= rank(entry)
    stats = dict(re.findall(r'(\w+)=(\d+)',actual.split('HEAP_STATS\n')[1]))
    assert {k:int(stats[k]) for k in counts} == counts
    assert int(stats['size']) == len(waiting) and int(stats['max_size']) == maximum

if __name__ == '__main__':
    total = 0
    for path in sorted((ROOT/'tests/cases').glob('*.txt')):
        if path.name.endswith('_output_file.txt'):
            continue
        result = run(path.read_text())
        expected = ROOT/'tests/expected'/path.name
        if expected.exists(): assert normalize(result) == normalize(expected.read_text()), path.name
        check_model(path.read_text(),result)
        if path.stem.startswith('rotation_'):
            counts = {'ll': (0, 1), 'rr': (1, 0), 'lr': (1, 1), 'rl': (1, 1)}
            left, right = counts[path.stem.removeprefix('rotation_')]
            assert f'left_rotations={left}\nright_rotations={right}' in result
            assert 'LEVEL 0: [20]\nLEVEL 1: [10]|[30]' in result
        print('PASS',path.name); total += 1
    for seed in range(30):
        rng = random.Random(seed)
        lines = ['CAPACITY 3']
        for i in range(150):
            # Large gaps keep later input timestamps beyond capacity waits.
            t = i*1000; id = rng.randrange(35)
            cmd = rng.choice(['SUBMIT','SUBMIT','PROCESS_NEXT','CANCEL','UPDATE_PRIORITY','FIND','JOB_STATUS','SHORT_JOBS'])
            args = {'SUBMIT':f'{id} {rng.randrange(-5,6)} {rng.randrange(1,50)} {rng.randrange(0,10)}',
                    'CANCEL':str(id),'UPDATE_PRIORITY':f'{id} {rng.randrange(-5,6)}','FIND':str(id)}.get(cmd,'')
            lines.append(f'{t} {cmd} {args}'.strip())
        text = '\n'.join(lines)+'\n'
        check_model(text,run(text)); total += 1
    print(f'PASS: {total} acceptance scenarios (including 30 seeded model comparisons)')
