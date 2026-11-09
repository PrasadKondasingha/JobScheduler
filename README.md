# Job Scheduler Simulator

A Java project for simulating a small batch job scheduler. The simulator reads timestamped commands, schedules jobs into a fixed number of execution slots, and writes deterministic results and data-structure details to an output file.

## Assignment

The project is for COP 5536 (Fall 2026), with a due date of **November 9, 2026**. The complete specification and sample cases are in [`Docs/`](Docs/):

- [`ADS+Project.pdf`](Docs/ADS+Project.pdf) — requirements and output format
- [`TestCases.docx`](Docs/TestCases.docx) — sample inputs and expected outputs

## Required behavior

- Begin in priority scheduling mode; switch permanently to shortest-estimated-time mode on `SHORT_JOBS`.
- Track job records in a hand-built AVL tree keyed by job ID and waiting jobs in a hand-built binary max heap.
- Track running job completions with a standard-library min-priority queue and retain cancelled jobs in cancellation order.
- Support `SUBMIT`, `PROCESS_NEXT`, `FIND`, `JOB_STATUS`, `CANCEL`, `UPDATE_PRIORITY`, and `SHORT_JOBS`.
- Write output to `<input-base>_output_file.txt`, including command results, final execution order, AVL structure and traversal, waiting heap array, and required statistics.
- Include a Makefile that prepares the program for execution on the grading environment.

## Build and run

Compile with `make`, then run:

```sh
make
java jobScheduler input_file
```

Replace `input_file` with the path to a scheduler input text file. The program writes `<input-base>_output_file.txt` next to that input. For example, `cases/sample.txt` produces `cases/sample_output_file.txt`.

The implementation is under `edu.ufl.ads.jobscheduler`; the default-package launcher preserves the assignment's required command. The Makefile compiles for Java 17 compatibility.

## Tests and submission

All three supplied DOCX inputs are ready to run in `tests/cases/supplied_*.txt`.
For example:

```sh
make
java jobScheduler tests/cases/supplied_1.txt
make test
make submission
```

`make test` runs 17 fixed cases and 30 reproducible random scenarios (150 commands
each) against an independent list-based scheduler model. It checks scheduler
messages, execution order, status tables, AVL balance and ordering, heap rank and
membership, and mutation statistics. The four minimal rotation cases also check
exact shapes and primitive rotation counts.

Expected outputs for the three supplied cases were transcribed from the DOCX.
Other expected output files are regression snapshots checked against the model;
they are not additional instructor-provided answers. Table whitespace is normalized.
The two PDF examples are also included. Additional cases cover ties, negative
priorities and delta, cancellation errors and order, duplicate IDs, empty queues,
priority repairs, simultaneous completion, large timestamps and AVL deletion.

`make submission` creates `submission/final_submission.zip` using the compiled
`submission/Report.pdf`, then extracts the ZIP into a temporary directory, compiles
it independently and compares all 17 fixed cases. The ZIP has no nested directories:
all Java sources, the Makefile, README, and PDF report are at
its root. The same files are also written to `submission/final_submission/`.
Test inputs and expected outputs remain in the development project. Packaging removes package declarations and internal imports from the generated
copies and renames `JobScheduler` to `SchedulerEngine` to avoid a filename
collision with `jobScheduler.java` on case-insensitive filesystems.

The program requires JDK 17 or newer and uses only Java standard libraries.
Development validation and packaging additionally use Python 3's standard library.
The grading build uses `javac` and `java` directly. All application imports use
the Java standard library. No external JAR files, dependency downloads, Gradle,
Python, Docker, or LaTeX are required to build and run the submitted program.

The report contains the required technical description; add any personal/course
identification required separately by your submission portal.

## Project documents

- [`Instructions.md`](Instructions.md) — input format, scheduler rules, output, and packaging constraints
- [`ProjectPlan.md`](ProjectPlan.md) — implementation milestones and completion checklist


## Report source

The report is written in `report/Report.tex`. Run `make report` to compile it
with local `latexmk`, or the Docker image `texlive/texlive:latest` when LaTeX is
not installed. Run `make submission` afterward to include the updated PDF in
`submission/final_submission/` and its ZIP. Graders do not need LaTeX or Docker.

## Static analysis

SonarLint is not installed in this workspace. Development checks use SpotBugs
4.10.4 and `javac --release 17 -Xlint:all -Werror`. With an extracted SpotBugs
distribution, run:

```sh
SPOTBUGS_HOME=/path/to/spotbugs-4.10.4 make quality
```

This also runs null-contract and malformed-command regression checks. The XML
report is `build/quality/spotbugs.xml`. The sole rule exclusion allows the
assignment-required lowercase `jobScheduler` launcher name. No null-pointer
rules are excluded. SpotBugs is a development tool and is not shipped or needed
by the grading build. Unknown job timestamps and missing AVL children remain
intentionally nullable.
