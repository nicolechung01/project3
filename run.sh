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