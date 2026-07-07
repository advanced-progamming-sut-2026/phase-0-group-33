import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(HERE)
SRC_DIR = os.path.join(PROJECT_ROOT, "src")
TESTCASES = os.path.join(HERE, "testcases.txt")


class Case:
    def __init__(self, cid, name):
        self.cid = cid
        self.name = name
        self.commands = []
        self.expects = []

    def add_command(self, cmd):
        self.commands.append(cmd)
        self.expects.append([])

    def add_expect(self, text):
        if not self.expects:
            self.commands.append("")
            self.expects.append([])
        self.expects[-1].append(text)


def parse_cases(path):
    cases = []
    current = None
    header = re.compile(r"^=== TEST (\d+): (.*)$")
    with open(path, encoding="utf-8") as f:
        for raw in f:
            line = raw.rstrip("\n")
            m = header.match(line)
            if m:
                current = Case(int(m.group(1)), m.group(2))
                cases.append(current)
            elif line.startswith("> "):
                if current is not None:
                    current.add_command(line[2:])
            elif line.startswith("? "):
                if current is not None:
                    current.add_expect(line[2:])
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
    args = ["javac", "-encoding", "UTF-8", "-d", classes_dir] + java_files
    proc = subprocess.run(args, capture_output=True, text=True)
    if proc.returncode != 0:
        print("ERROR: project failed to compile:")
        print(proc.stderr)
        return False
    return True


def run_case(case, classes_dir):
    workdir = tempfile.mkdtemp(prefix="pvz_case_")
    stdin = "\n".join(case.commands) + "\nmenu exit\nmenu exit\nmenu exit\n"
    try:
        proc = subprocess.run(
            ["java", "-cp", classes_dir, "Main"],
            input=stdin, capture_output=True, text=True,
            cwd=workdir, timeout=60)
        return proc.stdout + proc.stderr
    except subprocess.TimeoutExpired:
        return "__TIMEOUT__"
    finally:
        shutil.rmtree(workdir, ignore_errors=True)


def check_case(case, output):
    if output == "__TIMEOUT__":
        return ["the game did not terminate within 60 seconds"]
    lines = output.splitlines()
    problems = []
    cursor = 0
    for cmd, expects in zip(case.commands, case.expects):
        for needle in expects:
            found_at = -1
            for i in range(cursor, len(lines)):
                if needle in lines[i]:
                    found_at = i
                    break
            if found_at < 0:
                earlier = any(needle in lines[i] for i in range(0, cursor))
                detail = "expected output was not found"
                if earlier:
                    detail = "output appeared earlier than expected (wrong order)"
                problems.append(
                    "after command '%s': %s\n      expected substring: %s"
                    % (cmd, detail, needle))
            else:
                cursor = found_at + 1
    return problems


def main():
    only = None
    if len(sys.argv) > 1:
        try:
            only = int(sys.argv[1])
        except ValueError:
            only = None

    if not os.path.exists(TESTCASES):
        print("ERROR: testcases.txt not found at", TESTCASES)
        return 2

    cases = parse_cases(TESTCASES)
    if only is not None:
        cases = [c for c in cases if c.cid == only]
        if not cases:
            print("No test with id", only)
            return 2

    classes_dir = tempfile.mkdtemp(prefix="pvz_classes_")
    try:
        print("Compiling project ...")
        if not compile_project(classes_dir):
            return 2
        print("Running %d test case(s) ...\n" % len(cases))

        passed = 0
        failures = []
        for case in cases:
            output = run_case(case, classes_dir)
            problems = check_case(case, output)
            if problems:
                failures.append((case, problems, output))
                print("FAIL  TEST %04d: %s" % (case.cid, case.name))
                for p in problems:
                    print("    - " + p)
            else:
                passed += 1

        print("\n" + "=" * 60)
        print("PASSED: %d / %d" % (passed, len(cases)))
        print("FAILED: %d / %d" % (len(failures), len(cases)))
        if failures:
            print("\nConflicting tests (run 'python run_tests.py <id>' to inspect one):")
            for case, problems, _out in failures:
                print("  TEST %04d: %s" % (case.cid, case.name))
            if only is not None:
                case, _problems, out = failures[0]
                print("\n----- full game output for TEST %04d -----" % case.cid)
                print(out)
        return 1 if failures else 0
    finally:
        shutil.rmtree(classes_dir, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
