# Project Instructions

This document summarizes the requirements in [`Docs/ADS+Project.pdf`](Docs/ADS+Project.pdf). Follow that specification for exact graded output wording and formatting.

## Runtime and input

The program takes one input-file path as a command-line argument. The first non-empty line is `CAPACITY <k>`. Each following command line starts with a nonnegative integer timestamp, and timestamps are nondecreasing. Supported command forms are:

```text
<time> SUBMIT <job_id> <priority> <estimated_time> <delta>
<time> PROCESS_NEXT
<time> FIND <job_id>
<time> JOB_STATUS
<time> CANCEL <job_id>
<time> UPDATE_PRIORITY <job_id> <new_priority>
<time> SHORT_JOBS
```

Actual execution time is `estimated_time + delta`; completion time is execution start time plus actual execution time. Before handling a command at time `t`, complete every running job whose completion time is at most `t`. If completion and command timestamps match, completion is handled first. Input cases will not require simulation time to move backward.

## Scheduling rules

- Capacity `k` limits the number of simultaneously running jobs.
- Initially, select the waiting job with the highest priority. Break ties by earlier submission time, then smaller job ID.
- `SHORT_JOBS` switches the policy permanently to the smallest estimated time. Break ties by higher priority, earlier submission time, then smaller job ID. Reorganize the current waiting heap when switching modes. Delta does not affect this ordering.
- `PROCESS_NEXT` starts one waiting job. If all slots are occupied, advance to the earliest completion, complete every job finishing then, and start one waiting job. If no job is waiting, emit `NO_WAITING_JOBS`.
- A waiting job can be cancelled; remove it from both the AVL tree and waiting heap, mark it `CANCELLED`, and append it to the cancelled list. Other statuses cannot be cancelled.
- Priority can be updated only for a waiting job, and the heap must reflect the new rank.

## Required data structures

- **Job:** one object per submission containing the job ID, submission time, priority, estimated time, delta, execution start time, actual execution time, completion time, and status. Structures should hold references to that same object.
- **AVL tree:** implement from scratch, keyed by job ID. Store waiting, running, and completed jobs; remove cancelled jobs. Maintain balance after each insertion and deletion. Empty subtree height is 0; leaf height is 1.
- **Binary max heap:** implement from scratch and store only waiting jobs. Its comparator follows the current scheduling policy.
- **Cancelled jobs list:** standard-library list is permitted; preserve cancellation order.
- **Running jobs:** standard-library min-priority queue is permitted; order by completion time.

### Heap implementation guidance

Represent the waiting max heap with an array-backed complete binary tree. Keep the heap-order invariant explicit: for every parent-child pair, the parent has at least as high a scheduling rank as its child under the active comparator. Insertion, extraction, and removal restore this invariant by sifting entries up or down; each takes `O(log n)` heap levels. A policy rebuild may heapify all current waiting entries in `O(n)` time.

The heap must support removal of a particular waiting job for `CANCEL`, and rank repair for `UPDATE_PRIORITY`. A practical design keeps a job-ID-to-array-index map (or equivalent location handle) and updates it every time heap entries move. This avoids scanning the heap for a cancelled or updated job and prevents stale positions after sifting. Since job IDs are unique, they can identify the map entry; heap entries should still refer to the shared Job objects used by the AVL tree. Rebuild the location map while reorganizing the heap for `SHORT_JOBS`.

Treat the comparator as the single definition of rank. Apply all tie breakers consistently in insertion, extraction, arbitrary removal, update repair, and rebuild. Priority and estimated time are mutable scheduling fields, so an update must be followed by heap repair before the next scheduling operation. Do not substitute a binomial, Fibonacci, interval, or library heap: the assignment specifically grades the internal array of a hand-built binary max heap.

## Output requirements

Write all output to `<input-base>_output_file.txt` in the input file's directory. For example, `case1.txt` produces `case1_output_file.txt`. Do not write diagnostics or nondeterministic values to this output.

Use the exact labels and field order in the PDF. In particular:

- `FIND` reports submitted time, started time, actual time, completion time, and status. Unknown times are `null`. Search the cancelled list if the ID is absent from the AVL tree.
- `JOB_STATUS` prints AVL-tree records in increasing job ID order, then cancelled records in cancellation order.
- At end of file, print execution order, the AVL tree level by level (including internal `NULL` positions where needed), inorder job IDs, and the max heap's actual internal array as `(job_id,priority,estimated_time)` entries.
- Print the required tree and heap counters. Tree counters include insertions, deletions, and primitive left/right rotations. Heap counters include size, maximum size, insertions, extractions, removals, priority updates, and policy rebuilds.

Count operations at the point they occur: a job selected to start is an extraction; cancelling a waiting job is a removal; a waiting job's successful priority change is a priority update; and changing policy with `SHORT_JOBS` is a rebuild. Keep these counters distinct even when operations involve similar heap repair work. The course notes on advanced data structures explain why sequence costs and deferred work must be counted according to the actual operations performed; the required counters are literal implementation counts, not asymptotic estimates.

## Build and submission

The required submission is one ZIP file with no nested directory: after extraction, source files, Makefile, and required configuration files are at the extracted root. `make` must prepare the program without interactive or administrative steps. The Java invocation specified by the assignment is:

```sh
java jobScheduler input_file
```

The implementation lives in package `edu.ufl.ads.jobscheduler`; a default-package `jobScheduler` launcher delegates to it to preserve this invocation.

Include a short PDF report describing program organization, classes/modules, AVL and heap representations, and any additional libraries. Do not replace either required hand-built data structure with a standard-library implementation.

The companion course notes in `/Users/indika/Documents/Github/notes/AdvancedDataStructures` discuss priority-queue design, heap invariants, known-position removal, and amortized operation costs. They provide useful implementation context, but they cover binomial/Fibonacci and double-ended/interval heaps rather than this assignment's AVL tree. The project PDF remains authoritative for the required structures and exact behavior.

