package it.unipi.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

public class JobStarter {

    static int DEFAULT_REDUCERS = 1;

    public static void main(String[] args){
        try {
            if (args.length < 4) {
                System.err.println("Usage: Main <input path> <total count output path> <frequency output path> <num reducers>");
                System.exit(-1);
            }

            String inputPath = args[0];
            String totalLetterCountOutputPath = args[1];
            String letterFrequencyOutputPath = args[2];
            int numReducers = Integer.parseInt(args[3]);

            System.out.println("Input Path: " + inputPath);
            System.out.println("Total Letter Count Output Path: " + totalLetterCountOutputPath);
            System.out.println("Letter Frequency Output Path: " + letterFrequencyOutputPath);
            System.out.println("Number of Reducers: " + numReducers);
            
            Configuration conf = new Configuration();

            // Configurazione del primo job per contare il numero totale di lettere
            Map<String, String> argMap = new HashMap<>();
            argMap.put("input", inputPath);
            argMap.put("TotalLetterCountOutput", totalLetterCountOutputPath);

            Job totalLetterCountJob = TotalLetterCount.configureJob(conf, argMap);
            totalLetterCountJob.setNumReduceTasks(DEFAULT_REDUCERS);
            if (!totalLetterCountJob.waitForCompletion(true)) {
                System.exit(1);
            }

            // Lettura del numero totale di lettere dal risultato del primo job
            Path resultFilePath = new Path(totalLetterCountOutputPath + "/part-r-00000");
            FileSystem fs = FileSystem.get(conf);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(resultFilePath)));
            String line = br.readLine();
            br.close();
            String[] keyValue = line.split("\t");
            long totalLetters = Long.parseLong(keyValue[1]);

            // Imposta il numero totale di lettere nella configurazione per il secondo job
            conf.set("totalLetters", String.valueOf(totalLetters));

            // Configurazione del secondo job per calcolare la frequenza delle lettere
            Map<String, String> argMap2 = new HashMap<>();
            argMap2.put("input", inputPath);
            argMap2.put("letterFrequencyOutput", letterFrequencyOutputPath);

            Job letterFrequencyJob = LetterFrequency.configureJob(conf, argMap2, numReducers);
            System.exit(letterFrequencyJob.waitForCompletion(true) ? 0 : 1);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
