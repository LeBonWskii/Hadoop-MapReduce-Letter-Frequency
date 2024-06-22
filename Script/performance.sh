#!/bin/bash

# Check if the Hadoop cluster is running
if ! hdfs dfs -test -d /; then
    echo "Hadoop cluster is not running"
    exit 1
fi

# Local project directory
parent_dir=$(dirname $(pwd))

# Array of project names
project_names=("Combiner" "InMapping")

# Array of num_reducers
num_reducers_values=(1 2 3)

performance=true

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven could not be found. Please install Maven to proceed."
    exit 1
fi

# Iterate over each combination of values
for project_name in "${project_names[@]}"; do

    # Compile the Java code
    cd "${parent_dir}/${project_name}" || { echo "Failed to navigate to project directory: ${parent_dir}/${project_name}"; exit 1; }
    if ! mvn clean package; then
        echo "Maven build failed for project: ${project_name}"
        exit 1
    fi

    for num_reducers in "${num_reducers_values[@]}"; do
        cd "${parent_dir}/script" || { echo "Failed to navigate to script directory: ${parent_dir}/script"; exit 1; }

        # Check if the run.sh script is executable
        if [ ! -x ./run.sh ]; then
            chmod +x ./run.sh
        fi

        printf "Executing %s with %d reducers, performance=%s\n" "$project_name" "$num_reducers" "$performance"

        if ! ./run.sh "$project_name" "$num_reducers" "$performance"; then
            echo "Execution of run.sh failed for project: ${project_name} with reducers: ${num_reducers}"
            exit 1
        fi
    done
done

printf "All combinations executed\n"