#!/bin/bash

echo "Cleaning and Compiling"
echo "======================="
ant clean; ant init; ant compile; ant jar
