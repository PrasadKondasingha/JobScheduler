#!/usr/bin/env python3
"""Compile the flat ZIP in isolation and compare all included fixed test outputs."""
from pathlib import Path
import subprocess
import tempfile
import zipfile
import sys
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT/'tests'))
from run_tests import normalize
with tempfile.TemporaryDirectory() as tmp:
    dest = Path(tmp)
    with zipfile.ZipFile(ROOT/'submission/final_submission.zip') as archive:
        assert all('/' not in name for name in archive.namelist()), 'Archive must be flat'
        assert not any(name.endswith('.class') for name in archive.namelist())
        archive.extractall(dest)
    expected_files = {
        'SchedulerEngine.java' if p.name == 'JobScheduler.java' else p.name
        for p in (ROOT/'src/main/java').rglob('*.java')
    } | {'Makefile', 'README.md', 'Report.pdf'}
    assert {p.name for p in dest.iterdir()} == expected_files
    subprocess.run(['make'],cwd=dest,check=True)
    for case in sorted((ROOT/'tests/cases').glob('*.txt')):
        if case.name.endswith('_output_file.txt'):
            continue
        (dest/case.name).write_text(case.read_text())
        subprocess.run(['java','jobScheduler',case.name],cwd=dest,check=True,capture_output=True)
        actual = (dest/(case.stem+'_output_file.txt')).read_text()
        expected = (ROOT/'tests/expected'/case.name).read_text()
        assert normalize(actual) == normalize(expected),case.name
    print('PASS: isolated flat ZIP build and all 17 fixed cases')
