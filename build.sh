#!/bin/bash

# Compile main class and NNJSON library with proper package structure
javac -cp lib/javapiglerapi.jar:lib/jsr75.jar:lib/midpapi20.jar:lib/nokiaui.jar:lib/cldcapi10.jar \
      -d build \
      Aquarc.java \
      NNJSON/src/cc/nnproject/json/*.java

# Create JAR with proper package structure
mkdir -p build
cd build && jar cfm ../Aquarc.jar ../META-INF/MANIFEST.MF *
