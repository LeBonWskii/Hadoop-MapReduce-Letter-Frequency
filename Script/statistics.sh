#!/bin/bash

# Check if the Hadoop cluster is running
if ! hdfs dfs -test -d /; then
    echo "Hadoop cluster is not running"
    exit 1
fi

# Local project directory
parent_dir=$(dirname $(pwd))

# Project name
project_name="Combiner"

# Number of reducers
num_reducers_value=1

# Performance mode
performance=false

# Compile the Java code
cd ${parent_dir}/${project_name}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven could not be found. Please install Maven to proceed."
    exit 1
fi

# Run Maven package and check for errors
if ! mvn clean package; then
    echo "Maven build failed"
    exit 1
fi

# Execute the project with the specified number of reducers
cd ${parent_dir}/script

# Check if the run.sh script is executable
if [ ! -x ./run.sh ]; then
    chmod +x ./run.sh
fi

printf "Executing %s with %d reducers, performance=%s\n" $project_name $num_reducers_value $performance

if ! ./run.sh $project_name $num_reducers_value $performance; then
    echo "Execution of run.sh failed"
    exit 1
fi

printf "All combinations executed\n"