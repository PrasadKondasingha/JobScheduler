# Project Plan

## Goal

Deliver the COP 5536 Fall 2026 job scheduler simulator described in [`Docs/ADS+Project.pdf`](Docs/ADS+Project.pdf), with deterministic output matching the required format and a flat ZIP-ready submission.

## Milestones

1. **Set up the executable project**
   - Confirm Java entry point, source layout, and command-line argument handling.
   - Add a Makefile that builds the program and supports the required `java jobScheduler input_file` invocation.
2. **Implement core records and structures**
   - Define the Job object and status/time fields.
   - Implement the AVL tree with insertion, lookup, deletion, rotations, traversal, level output, and counters.
   - Implement the array-backed binary max heap with insertion, extraction, arbitrary removal, priority update, policy rebuild, and counters.
   - Maintain a job-ID-to-heap-index map (or equivalent handles) during every swap so cancellation and priority changes can find a job directly.
   - Define one rank comparator per mode and use it consistently for every heap operation and rebuild.
3. **Implement scheduler simulation**
   - Parse capacity and timestamped commands.
   - Process completions before commands at equal timestamps.
   - Implement capacity handling, execution timing, priority scheduling, shortest-job mode, cancellation, and updates.
4. **Implement graded output**
   - Write command responses and job tables to the input-derived output filename.
   - Print final execution order, AVL structure and inorder traversal, heap array, and required statistics.
5. **Check assignment examples and package**
   - Compare output against the examples in `Docs/TestCases.docx` and the specification examples, including tie breaks, updates, cancellations, and same-time completions.
   - Verify the required launch/build flow and create a single ZIP with files at its root.
   - Produce the short PDF report describing the implementation.

## Design notes from the course material

The Advanced Data Structures notes at `/Users/indika/Documents/Github/notes/AdvancedDataStructures` emphasize choosing a structure for its required operations, maintaining its invariant after each change, and analyzing the actual operation sequence rather than relying on one-operation intuition. Applied here:

- Keep the waiting structure as the required binary max heap; advanced alternatives in the notes (binomial, Fibonacci, double-ended, and interval heaps) do not satisfy the assignment's explicit structure and final-array requirements.
- A known-position removal is efficient only if the position remains valid as entries move. Track heap indices through swaps so cancellation and priority updates can repair the heap in `O(log n)` without a linear search.
- A `SHORT_JOBS` mode change changes the ordering relation for every waiting job. Rebuild the heap once, restore all index metadata, and count one rebuild.
- Implement and count each observable operation separately. For example, extraction when a job starts, removal on cancellation, and repair after a priority change have distinct statistics even if each uses sift operations.

The notes do not cover the AVL implementation required by this project; use the AVL convention and rotation requirements in the project PDF for that work.

## Completion checklist

- [ ] All seven commands are implemented, including error cases.
- [ ] AVL and binary max heap are independent, hand-built implementations.
- [ ] Heap ordering changes correctly after priority updates and `SHORT_JOBS`.
- [ ] Heap location metadata remains correct after swaps, extraction, arbitrary removal, update, and rebuild.
- [ ] Heap comparison ties follow priority, estimated time where applicable, submission time, and job ID exactly as specified for each mode.
- [ ] Completion events and `PROCESS_NEXT` capacity waits advance simulated time correctly.
- [ ] Functional and structural output follows the specified labels, ordering, and null handling.
- [ ] Statistics count operations according to the assignment definitions.
- [ ] `make` prepares a runnable program; input is supplied as a command-line argument.
- [ ] Submission ZIP is flat and includes source, Makefile, and PDF report.

## Current repository snapshot

The implementation is organized under `src/main/java/edu/ufl/ads/jobscheduler` with a default-package `jobScheduler` launcher and a root Makefile. Remaining submission work includes comparing complete outputs against the supplied examples, reviewing edge cases, preparing the short PDF report, and creating the flat ZIP.

