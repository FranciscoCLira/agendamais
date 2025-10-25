#!/usr/bin/env bash
# Run Checkstyle only on changed Java files between HEAD and origin/main.
# Exit code 0 = no new violations; non-zero = violations found.

set -euo pipefail

# Determine base ref (merge-base or origin/main)
BASE_REF="origin/main"

# Fetch origin to ensure origin/main exists
git fetch origin main --quiet

# Get list of changed Java files
changed_files=$(git diff --name-only ${BASE_REF}...HEAD -- '*.java' || true)

if [ -z "${changed_files}" ]; then
  echo "No Java files changed relative to ${BASE_REF}."
  exit 0
fi

echo "Running Checkstyle on changed files:"
echo "${changed_files}"

# Create a temporary copy of the checkstyle config
config_location="config/checkstyle/google_checks.xml"

# Run Checkstyle via Maven, limiting to the changed files using the suppressions filter hack
# We'll generate a temporary suppression XML that excludes everything except changed files.

suppressions_file=$(mktemp)
cat > "${suppressions_file}" <<EOF
<?xml version="1.0"?>
<!DOCTYPE suppressions PUBLIC
    "-//Puppy Crawl//DTD Suppressions X.X//EN"
    "http://checkstyle.sourceforge.net/dtds/suppressions_1_1.dtd">
<suppressions>
EOF

# Allow only changed files by adding a suppression for everything else
# We'll use a negative lookahead not supported, so instead list explicit files to check in the Filter
for f in ${changed_files}; do
  # Escape slashes for the regex and append a suppression entry safely
  esc=${f//\//\\/}
  # Use printf to avoid problems with characters like () in the regex
  printf '  <suppress files="^((?!%s).)*$"/>\n' "$esc" >> "${suppressions_file}"
done

cat >> "${suppressions_file}" <<EOF
</suppressions>
EOF

# Run Maven Checkstyle with the suppression file active
mvn -B org.apache.maven.plugins:maven-checkstyle-plugin:checkstyle -Dcheckstyle.suppressions.file="${suppressions_file}"
ret=$?
rm -f "${suppressions_file}"
exit $ret
