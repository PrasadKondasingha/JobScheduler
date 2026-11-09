#!/usr/bin/env python3
"""Build a flat source ZIP using the compiled LaTeX report."""
from pathlib import Path
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
if __name__ == '__main__':
    dest = ROOT/'submission'; dest.mkdir(exist_ok=True)
    folder = dest/'final_submission'; folder.mkdir(exist_ok=True)
    report_path = dest/'Report.pdf'
    if not report_path.is_file():
        raise SystemExit('Missing report PDF: run make report first.')
    report = report_path.read_bytes()
    with zipfile.ZipFile(dest/'final_submission.zip','w',zipfile.ZIP_DEFLATED) as archive:
        for source in sorted((ROOT/'src/main/java').rglob('*.java')):
            lines = source.read_text().splitlines()
            flat = '\n'.join(line for line in lines if not line.startswith('package ') and not line.startswith('import edu.ufl.ads.jobscheduler.'))+'\n'
            flat = re.sub(r'\bJobScheduler\b', 'SchedulerEngine', flat)
            name = 'SchedulerEngine.java' if source.name == 'JobScheduler.java' else source.name
            archive.writestr(name,flat)
        makefile = (ROOT/'Makefile').read_text().split('test: all')[0].replace('run test submission', 'run')
        makefile = makefile.replace("$(shell find src/main/java -name '*.java')", "$(wildcard *.java)")
        archive.writestr('Makefile', makefile)
        archive.writestr('Report.pdf',report)
        archive.write(ROOT/'tools/submission_README.md', 'README.md')
    with zipfile.ZipFile(dest/'final_submission.zip') as archive:
        archive.extractall(folder)
    print(folder)
    print(dest/'final_submission.zip')
