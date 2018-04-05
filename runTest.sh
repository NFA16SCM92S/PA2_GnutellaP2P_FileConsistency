#!/bin/bash

echo "Running test file"
echo "======================="
ant -buildfile test.xml clean
ant -buildfile test.xml compile
ant -buildfile test.xml jar
ant -buildfile test.xml run 
