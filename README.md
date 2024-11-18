## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

Names: John Adtinkson, Nicole Chung, Jenna Odom

## Compile and Run Code

Please run the bash file. - Can be found in the run.sh file

#!/bin/bash

# Set the source and output directories
SRC_DIR="src/main/java"
BIN_DIR="bin"
LIB_DIR="lib"

# Create the bin directory if it doesn't exist
mkdir -p $BIN_DIR

# Compile Java files
echo "Compiling Java files..."
javac -d $BIN_DIR -sourcepath $SRC_DIR -cp "$LIB_DIR/*" $SRC_DIR/*.java

# Run TestTupleGenerator
java -cp "$BIN_DIR:$LIB_DIR/*" main.java.TestTupleGenerator

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
