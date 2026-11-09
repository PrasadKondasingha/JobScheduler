# Job Scheduler

COP 5536, Fall 2026 — a simulator using a custom AVL tree and binary max heap.

## Build

From this directory, run:

```sh
make
```

The build uses `make` and JDK 17 or newer, provided by the grading environment.
All application libraries are part of the Java standard library. No external
JAR files, package installations, or downloads are required.

## Run

Supply the input file as a command-line argument:

```sh
java jobScheduler input_file
```

Replace `input_file` with the actual input filename or path. You can also run:

```sh
make run INPUT=path/to/input.txt
```

The first non-empty input line must be `CAPACITY <k>`. Remaining lines contain
timestamped commands as specified in the assignment.

## Output

Results are written next to the input file as `<input-base>_output_file.txt`.
For example, `input.txt` produces `input_output_file.txt`.

The output contains command responses followed by execution order, AVL tree,
inorder traversal, waiting heap, and statistics. Simulation results are written
to the output file rather than the terminal. `Input:` and `Output:` prefixes
are not added to the command responses.

## Files

| File | Purpose |
|---|---|
| `jobScheduler.java` | Required command-line entry point |
| `JobSchedulerApplication.java` | Input parsing and output file handling |
| `SchedulerEngine.java` | Simulation and command handling |
| `Job.java` | Shared job record and execution state |
| `AvlTree.java` | Custom AVL tree keyed by job ID |
| `WaitingMaxHeap.java` | Custom waiting-job heap |
| `AvlTreeStatistics.java` | Immutable AVL statistics record |
| `HeapStatistics.java` | Immutable heap statistics record |
| `OutputFormat.java` | Required output labels, message templates, and formatting |
| `Makefile` | Build and run commands |
| `Report.pdf` | Program organization and data structure description |
| `README.md` | Build and usage instructions |
