# Letter Frequency Analysis in Multiple Languages using Hadoop and MapReduce

## Project Overview

In this study, we aim to analyze the frequency of letters in various languages using Hadoop and MapReduce. We’ll be diving into two different methods for data combination: the **Combiner** and **In-Mapping Combining** techniques. Additionally, we'll explore how changing the number of reducers affects performance.

We’ll also compare these distributed approaches with a local execution using Python, and in the end, we’ll use the best-performing distribuited configuration to analyze a text in different lenguages. For our text, we've chosen the beloved classic "Pinocchio".

## What’s Inside

### 1. Letter Frequency with Hadoop and MapReduce
- **Combiner Method**: Using a combiner to reduce the data transferred between the mapper and the reducer.
- **In-Mapping Combining**: Combining data directly within the mapper to further reduce network load.

### 2. Varying the Number of Reducers
- Running the letter frequency analysis with different numbers of reducers to see how this configuration impacts performance.

### 3. Performance Analysis
- Comparing the performance of different configurations using metrics like execution time and resource utilization.
- Comparing these results with a local execution in Python to highlight the pros and cons of each approach.

### 4. Text Analysis
- Applying the best configuration to analyze the letter frequency in "Pinocchio", examining the text in multiple languages.

### Running the Project

**Automated Analysis**
- We’ve implemented shell scripts to automate the analysis tasks.

### Best Configuration

With the optimal configuration, we’ve analyzed the letter frequency in "Pinocchio", providing a detailed look at how letters are distributed across different languages.

## Documentation and Presentation

For a deeper dive into the project, check out the documentation and the presentation slides available above ☝🏻.

## Authors
- **Martina Fabiani**
- **Tommaso Falaschi**
- **Rossana Antonella Sacco**


