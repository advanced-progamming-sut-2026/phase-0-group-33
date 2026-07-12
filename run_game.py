import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src")
OUT = os.path.join(ROOT, "out", "production", "AP")


def compile_project():
    java_files = []
    for base, _dirs, files in os.walk(SRC):
        for name in files:
            if name.endswith(".java"):
                java_files.append(os.path.join(base, name))
    os.makedirs(OUT, exist_ok=True)
    proc = subprocess.run(
        ["javac", "-encoding", "UTF-8", "-d", OUT] + java_files,
        capture_output=True, text=True)
    if proc.returncode != 0:
        print("Compilation failed:")
        print(proc.stderr)
        return False
    return True


def main():
    if not compile_project():
        return 1
    return subprocess.run(["java", "-cp", OUT, "Main"], cwd=ROOT).returncode


if __name__ == "__main__":
    sys.exit(main())
