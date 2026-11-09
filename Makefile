JAVAC ?= javac
JAVA ?= java
JAVA_SOURCES := $(shell find src/main/java -name '*.java')

.PHONY: all run test submission
all:
	$(JAVAC) --release 17 -encoding UTF-8 -d . $(JAVA_SOURCES)

run: all
	@test -n "$(INPUT)" || { echo 'Usage: make run INPUT=path/to/input.txt' >&2; exit 1; }
	$(JAVA) jobScheduler "$(INPUT)"

test: all
	JAVA="$(JAVA)" python3 tests/run_tests.py

submission: test
	python3 tools/package_submission.py
	python3 tools/verify_submission.py

.PHONY: report
report:
	sh tools/build_report.sh

.PHONY: quality
quality:
	sh tools/check_quality.sh
