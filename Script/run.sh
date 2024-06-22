#!/bin/bash

set -e

# Settings
project_name=$1
num_reducers=$2
performance=$3

if [ "$performance" = true ]; then
    save_logs=true
    download_output=true
    resource_dir=resources/performance
else
    save_logs=false
    download_output=true
    resource_dir=resources/statistics
fi

# Local project directory
parent_dir=$(dirname $(pwd))

# Upload the JAR file to the Hadoop cluster
cd ${parent_dir}/${project_name}
scp target/${project_name}-1.0-SNAPSHOT.jar hadoop@10.1.1.67:

# Create a directory in HDFS to store the project files
project_dir=/user/$(whoami)/${project_name}
hdfs dfs -mkdir -p ${project_dir}
printf "HDFS Project directory %s\n" ${project_dir}

# Create a directory in HDFS to store the input file
input_dir=${project_dir}/input
hdfs dfs -mkdir -p ${input_dir}
printf "HDFS Input directory %s\n" ${input_dir}

# Create a directory in HDFS to store the output files
output_dir=${project_dir}/output
hdfs dfs -mkdir -p ${output_dir}
printf "HDFS Output directory %s\n" ${output_dir}

# Upload each input file to HDFS if it does not exist
cd ${parent_dir}
for file in ${resource_dir}/input/*; do
    filename=$(basename $file)
    if ! hdfs dfs -test -e ${input_dir}/${filename}; then
        hdfs dfs -put $file ${input_dir}
    fi
    printf "HDFS Input file %s\n" ${input_dir}/${filename}
done

# Read the current run number from the file, if it exists.
cd ${parent_dir}/script
if [ -f "run_number.txt" ]; then
    run_number=$(cat run_number.txt)
else
    run_number=0
fi
# Format the run number as a 5-digit number
formatted_number=$(printf "%05d" $run_number)
printf "Execution number %s\n" $formatted_number

# Execute Hadoop WorkFlow for each input
cd ${parent_dir}
for file in ${resource_dir}/input/*; do
    # Get the filename
    filename=$(basename $file .txt)

    if [ "$save_logs" = true ]; then
        cd ${parent_dir}
        mkdir -p ${resource_dir}/output/output_${formatted_number}/${filename}
        log_file=${resource_dir}/output/output_${formatted_number}/${filename}/log.txt

        # Execute the Hadoop JobStart
        cd ${parent_dir}/${project_name}
        hadoop jar target/${project_name}-1.0-SNAPSHOT.jar \
        it.unipi.hadoop.JobStart \
        ${input_dir}/${filename}.txt \
        ${output_dir}/output_${formatted_number}/${filename}/count \
        ${output_dir}/output_${formatted_number}/${filename}/frequency \
        ${num_reducers} \
        > ../${log_file} 2>&1
    else
        # Execute the Hadoop JobStart
        cd ${parent_dir}/${project_name}
        hadoop jar target/${project_name}-1.0-SNAPSHOT.jar \
        it.unipi.hadoop.JobStart \
        ${input_dir}/${filename}.txt \
        ${output_dir}/output_${formatted_number}/${filename}/count \
        ${output_dir}/output_${formatted_number}/${filename}/frequency \
        ${num_reducers}
    fi

    printf "Hadoop JobStart executed for %s\n" ${filename}
done

# Download the output files from HDFS
if [ "$download_output" = true ]; then
    cd ${parent_dir}
    for file in ${resource_dir}/input/*; do

        # Get the filename
        filename=$(basename $file .txt)

        # local directory for output files
        local_dir=${resource_dir}/output/output_${formatted_number}/${filename}
        mkdir -p $local_dir

        # output files
        count_output_file=${local_dir}/count.txt
        > $count_output_file
        frequency_output_file=${local_dir}/frequency.txt
        > $frequency_output_file

        count_output_files=$(hadoop fs -ls ${output_dir}/output_${formatted_number}/${filename}/count | grep -v '_SUCCESS' | awk '{print $8}')
        for file in $count_output_files; do
            hadoop fs -text $file >> $count_output_file
        done

        frequency_output_files=$(hadoop fs -ls ${output_dir}/output_${formatted_number}/${filename}/frequency | grep -v '_SUCCESS' | awk '{print $8}')
        for file in $frequency_output_files; do
            hadoop fs -text $file >> $frequency_output_file
        done

        printf "Output files downloaded\n"

        # Print the output file
        printf "Text length: "
        cat $count_output_file

    done
fi

# Increment the run number and save it to the file
cd ${parent_dir}/script
echo $((run_number + 1)) > run_number.txt

# Add file text to specify parameters used
cd ${parent_dir}
filename=$(basename $file .txt)
echo "jobstart=${project_name}" >> ${resource_dir}/output/output_${formatted_number}/parameters.txt
echo "reducers=$num_reducers" >> ${resource_dir}/output/output_${formatted_number}/parameters.txt