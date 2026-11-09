#!/bin/sh
# Development checks only; SpotBugs is not a grading/runtime dependency.
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
: "${SPOTBUGS_HOME:?Set SPOTBUGS_HOME to an extracted SpotBugs distribution}"
spotbugs_jar="$SPOTBUGS_HOME/lib/spotbugs.jar"
[ -f "$spotbugs_jar" ] || { echo "Missing $spotbugs_jar" >&2; exit 1; }
mkdir -p build/quality/classes build/quality/tests
# Clear only this check's compiled output to avoid scanning stale nested classes.
find build/quality/classes build/quality/tests -name '*.class' -delete
find src/main/java -name '*.java' > build/quality/sources.txt
javac --release 17 -Xlint:all -Werror -d build/quality/classes @build/quality/sources.txt
javac --release 17 -Xlint:all -Werror -cp build/quality/classes -d build/quality/tests tests/java/RobustnessChecks.java
java -cp build/quality/classes:build/quality/tests RobustnessChecks
java -jar "$spotbugs_jar" -textui -effort:max -low -exitcode \
    -exclude tools/spotbugs-exclude.xml -xml:withMessages \
    -output build/quality/spotbugs.xml build/quality/classes
printf '%s\n' 'PASS: javac lint and SpotBugs; report: build/quality/spotbugs.xml'
