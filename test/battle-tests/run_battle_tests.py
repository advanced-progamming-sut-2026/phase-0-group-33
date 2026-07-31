import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
TEST_ROOT = os.path.dirname(HERE)
PROJECT_ROOT = os.path.dirname(TEST_ROOT)
SRC_DIR = os.path.join(PROJECT_ROOT, "src")

PREAMBLE = [
    "register -u {user} -p Abcd123! Abcd123! -n Tester -e {user}@mail.com -g male",
    "pick question -q 1 -a apple -c apple",
    "login -u {user} -p Abcd123!",
    "menu cheat unlock-chapters",
    "menu cheat unlock-plants",
    "menu cheat rich",
]

HEADER = re.compile(r"^=== TEST (BT-[0-9]+): (.*)$")


class Case:
    def __init__(self, tid, title, group, path):
        self.tid = tid
        self.title = title
        self.group = group
        self.path = path
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


def parse_case(path, group):
    case = None
    with open(path, encoding="utf-8") as f:
        for raw in f:
            line = raw.rstrip("\n")
            m = HEADER.match(line)
            if m:
                case = Case(m.group(1), m.group(2), group, path)
            elif line.startswith("> ") and case is not None:
                case.add_command(line[2:])
            elif line.startswith("? ") and case is not None:
                text = line[2:].strip()
                if text:
                    case.add_expect(text)
    return case


def collect(group_filter, id_filter):
    cases = []
    for group in sorted(os.listdir(HERE)):
        gdir = os.path.join(HERE, group)
        if not os.path.isdir(gdir):
            continue
        if group_filter and group_filter.lower() not in group.lower():
            continue
        for name in sorted(os.listdir(gdir)):
            if not name.endswith(".txt"):
                continue
            case = parse_case(os.path.join(gdir, name), group)
            if case is None:
                continue
            if id_filter and id_filter.lower() not in case.tid.lower():
                continue
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
    listing = os.path.join(classes_dir, "sources.txt")
    with open(listing, "w", encoding="utf-8") as f:
        f.write("\n".join(java_files))
    proc = subprocess.run(
        ["javac", "-encoding", "UTF-8", "-d", classes_dir, "@" + listing],
        capture_output=True, text=True)
    if proc.returncode != 0:
        print("ERROR: project failed to compile:")
        print(proc.stderr)
        return False
    return True


def run_case(case, classes_dir, index, timeout):
    workdir = tempfile.mkdtemp(prefix="pvz_battle_")
    user = "b%05d" % index
    lines = [tpl.format(user=user) for tpl in PREAMBLE]
    lines.extend(case.commands)
    lines.append("quit")
    stdin = "\n".join(lines) + "\n"
    try:
        proc = subprocess.run(
            ["java", "-cp", classes_dir, "Main"],
            input=stdin, capture_output=True, text=True,
            cwd=workdir, timeout=timeout)
        return proc.stdout + proc.stderr
    except subprocess.TimeoutExpired:
        return "__TIMEOUT__"
    finally:
        shutil.rmtree(workdir, ignore_errors=True)


def check_case(case, output):
    if output == "__TIMEOUT__":
        return ["the game did not terminate in time"]
    lines = output.splitlines()
    problems = []
    cursor = 0
    for cmd, expects in zip(case.commands, case.expects):
        for needle in expects:
            found = -1
            for i in range(cursor, len(lines)):
                if needle in lines[i]:
                    found = i
                    break
            if found < 0:
                earlier = any(needle in lines[i] for i in range(0, cursor))
                detail = "expected output was not found"
                if earlier:
                    detail = "output appeared earlier than expected (wrong order)"
                problems.append("after '%s': %s\n        expected: %s" % (cmd, detail, needle))
            else:
                cursor = found
    return problems


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--group", default=None)
    ap.add_argument("--id", default=None)
    ap.add_argument("--timeout", type=int, default=90)
    ap.add_argument("--list", action="store_true")
    ap.add_argument("--verbose", action="store_true")
    args = ap.parse_args()

    cases = collect(args.group, args.id)
    if not cases:
        print("No battle test matched the given filters.")
        return 2

    if args.list:
        current = None
        for case in cases:
            if case.group != current:
                current = case.group
                print("\n" + current)
            print("  %-10s %s" % (case.tid, case.title))
        print("\n%d battle test(s)." % len(cases))
        return 0

    classes_dir = tempfile.mkdtemp(prefix="pvz_battle_classes_")
    try:
        print("Compiling project ...")
        if not compile_project(classes_dir):
            return 2
        print("Running %d battle test(s) ...\n" % len(cases))

        per_group = {}
        failures = []
        passed = 0
        for index, case in enumerate(cases, 1):
            output = run_case(case, classes_dir, index, args.timeout)
            problems = check_case(case, output)
            stats = per_group.setdefault(case.group, [0, 0])
            if problems:
                stats[1] += 1
                failures.append((case, problems, output))
                print("FAIL  %s  %s" % (case.tid, case.title))
                if args.verbose:
                    for p in problems:
                        print("      - " + p)
            else:
                stats[0] += 1
                passed += 1
                if args.verbose:
                    print("ok    %s  %s" % (case.tid, case.title))

        print("\n" + "=" * 72)
        print("PER INTERACTION GROUP")
        print("=" * 72)
        for group, (ok, bad) in sorted(per_group.items()):
            mark = "OK  " if bad == 0 else "FAIL"
            print("%s %-34s %2d passed  %2d failed" % (mark, group, ok, bad))

        print("\n" + "=" * 72)
        print("PASSED: %d / %d" % (passed, len(cases)))
        print("FAILED: %d / %d" % (len(failures), len(cases)))
        if failures and not args.verbose:
            print("\nRe-run one case for details, for example:")
            print("  python run_battle_tests.py --id %s --verbose" % failures[0][0].tid)
        if failures and args.id and args.verbose:
            print("\n----- full game output -----")
            print(failures[0][2])
        return 1 if failures else 0
    finally:
        shutil.rmtree(classes_dir, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
