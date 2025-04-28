#!/bin/bash

# Find the directory where this script resides
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Assume the JAR is in a 'lib' subdirectory relative to the script
CLI_JAR_PATH="$SCRIPT_DIR/lib/docutavern-cli-0.1.0-SNAPSHOT.jar" # Adjust version/path as needed

# Check if JAR exists
if [ ! -f "$CLI_JAR_PATH" ]; then
  echo "[ERROR] Docutavern CLI JAR not found at $CLI_JAR_PATH"
  echo "Please ensure Docutavern is installed correctly."
  exit 1
fi

# Get the User's Current Working Directory
USER_CWD=$(pwd)

# Execute the Java CLI, passing the CWD and all other arguments
java -jar "$CLI_JAR_PATH" --cwd "$USER_CWD" "$@"