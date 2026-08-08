"""Runner for the phase-1 delivery test cases.

Every test case is one .txt file under test/delivery-tests/<group>/.

File format
-----------
    === TEST <id>: <title>
    #   ... free comments for the TA (ignored) ...
    > a line typed into the game
    ? a substring that must appear in the output after that line (order matters)
    >> RESTART        closes the game and starts it again on the SAME data folder

A file that contains no '>' line is a MANUAL case (repo inspection / UX):
it is reported as MANUAL and not executed.

Usage
-----
    python test/delivery-tests/run_delivery_tests.py                # everything
    python test/delivery-tests/run_delivery_tests.py 05             # one group
    python test/delivery-tests/run_delivery_tests.py TC-0201-1      # one case
    python test/delivery-tests/run_delivery_tests.py TC-0201-1 -v   # + full output
"""
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(os.path.dirname(HERE))
SRC_DIR = os.path.join(PROJECT_ROOT, "src")
HEADER = re.compile(r"^=== TEST (\S+): (.*)$")


class Case:
    def __init__(self, cid, name, path):
        self.cid = cid
        self.name = name
        self.path = path
        self.group = os.path.basename(os.path.dirname(path))
        self.runs = [[]]          # list of runs; each run = list of (cmd, [expects])

    @property
    def manual(self):
        return not any(run for run in self.runs)

    def add_command(self, cmd):
        self.runs[-1].append((cmd, []))

    def add_expect(self, text):
        if not self.runs[-1]:
            self.runs[-1].append(("", []))
        self.runs[-1][-1][1].append(text)

    def restart(self):
        self.runs.append([])


def parse_case(path):
    case = None
    with open(path, encoding="utf-8") as f:
        for raw in f:
            line = raw.rstrip("\n")
            m = HEADER.match(line)
            if m:
                case = Case(m.group(1), m.group(2), path)
            elif case is None:
                continue
            elif line.strip() == ">> RESTART":
                case.restart()
            elif line.startswith("> "):
                case.add_command(line[2:])
            elif line.startswith("? "):
                case.add_expect(line[2:])
    if case is not None:
        case.runs = [r for r in case.runs if r] or [[]]
    return case


def collect(root):
    cases = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames.sort()
        for name in sorted(filenames):
            if name.endswith(".txt") and name != "README.txt":
                case = parse_case(os.path.join(dirpath, name))
                if case is not None:
                    cases.append(case)
    return cases


def compile_project(classes_dir):
    java_files = []
    for root, _dirs, files in os.walk(SRC_DIR):
        for name in files:
            if name.endswith(".java"):
                java_files.append(os.path.join(root, name))
    if not java_files:
        print("ERROR: no .java files found under", SRC_DIR)
        return False
    proc = subprocess.run(["javac", "-encoding", "UTF-8", "-d", classes_dir] + java_files,
                          capture_output=True, text=True)
    if proc.returncode != 0:
        print("ERROR: project failed to compile:")
        print(proc.stderr)
        return False
    return True


def run_case(case, classes_dir):
    """Run every 'run' of the case in one shared data folder; return the joined output."""
    workdir = tempfile.mkdtemp(prefix="pvz_dl_")
    chunks = []
    try:
        for run in case.runs:
            if not run:
                continue
            stdin = "\n".join(cmd for cmd, _ in run) + "\nquit\n"
            try:
                proc = subprocess.run(
                    ["java", "-cp", classes_dir, "Main"],
                    input=stdin, capture_output=True, text=True,
                    cwd=workdir, timeout=90, encoding="utf-8", errors="replace")
                chunks.append(proc.stdout + proc.stderr)
            except subprocess.TimeoutExpired:
                return "__TIMEOUT__"
        return "\n".join(chunks)
    finally:
        shutil.rmtree(workdir, ignore_errors=True)


def check(case, output):
    if output == "__TIMEOUT__":
        return ["the game did not terminate within 90 seconds"]
    lines = output.splitlines()
    problems = []
    cursor = 0
    for run in case.runs:
        for cmd, expects in run:
            for needle in expects:
                found = -1
                for i in range(cursor, len(lines)):
                    if needle in lines[i]:
                        found = i
                        break
                if found < 0:
                    earlier = any(needle in lines[i] for i in range(0, cursor))
                    why = ("output appeared earlier than expected (wrong order)"
                           if earlier else "expected output was not found")
                    problems.append("after '%s': %s\n        expected: %s" % (cmd, why, needle))
                else:
                    cursor = found
    return problems


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("-")]
    verbose = "-v" in sys.argv
    cases = collect(HERE)
    if args:
        needle = args[0].lower()
        cases = [c for c in cases
                 if needle in c.cid.lower() or needle in c.group.lower()]
        if not cases:
            print("No test matches", args[0])
            return 2

    auto = [c for c in cases if not c.manual]
    man = [c for c in cases if c.manual]

    classes_dir = tempfile.mkdtemp(prefix="pvz_dl_classes_")
    try:
        print("Compiling project ...")
        if not compile_project(classes_dir):
            return 2
        print("Running %d automatic case(s)  (%d manual case(s) listed at the end)\n"
              % (len(auto), len(man)))

        passed, failures = 0, []
        current_group = None
        for idx, case in enumerate(auto, 1):
            if case.group != current_group:
                current_group = case.group
                print("--- %s" % current_group)
            output = run_case(case, classes_dir)
            problems = check(case, output)
            if problems:                      # one retry: guards against random spawns
                output = run_case(case, classes_dir)
                problems = check(case, output)
            if problems:
                failures.append((case, problems, output))
                print("  FAIL  %-12s %s" % (case.cid, case.name))
                for p in problems:
                    print("      - " + p)
            else:
                passed += 1
                print("  ok    %-12s %s" % (case.cid, case.name))
            if verbose and len(auto) == 1:
                print("\n----- full game output -----\n" + output)

        print("\n" + "=" * 70)
        print("AUTOMATIC : %d passed / %d" % (passed, len(auto)))
        print("MANUAL    : %d case(s) for the TA to check by hand" % len(man))
        if man and len(man) < 40:
            for c in man:
                print("            %-12s %s  (%s)" % (c.cid, c.name, c.group))
        if failures:
            print("\nFailing cases:")
            for case, _p, _o in failures:
                print("  %-12s %s" % (case.cid, os.path.relpath(case.path, HERE)))
        return 1 if failures else 0
    finally:
        shutil.rmtree(classes_dir, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
