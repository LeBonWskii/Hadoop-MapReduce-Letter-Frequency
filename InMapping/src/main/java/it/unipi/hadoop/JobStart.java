package it.unipi.hadoop;

import java.util.HashMap;

import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;


public class JobStart {
    static int DEFAULT_REDUCERS = 1;

    public static void main(String[] args) throws Exception {
        try {
            Configuration conf = new Configuration();
            if (args.length < 4) {
                System.err.println("Usage: Main <input path> <total count output path> <frequency output path> <num reducers>");
                System.exit(1);
            }

            String inputPath = args[0];
            String totalLetterCountOutputPath = args[1];
            String letterFrequencyOutputPath = args[2];
            int numReducers = Integer.parseInt(args[3]);

            System.out.println("Input Path: " + inputPath);
            System.out.println("Total Letter Count Output Path: " + totalLetterCountOutputPath);
            System.out.println("Letter Frequency Output Path: " + letterFrequencyOutputPath);
            System.out.println("Number of Reducers: " + numReducers);

            Map<String, String> argMap = new HashMap<>();
            argMap.put("input", inputPath);
            argMap.put("TotalLetterCountOutput", totalLetterCountOutputPath);

            // Job 1: Contare il numero totale di lettere
            Job totalLetterCountJob = LetterCount.configureJob(conf, argMap);
            totalLetterCountJob.setNumReduceTasks(DEFAULT_REDUCERS);
            if (!totalLetterCountJob.waitForCompletion(true)) {
                System.exit(1);
            }

            // Leggi il numero totale di lettere dal risultato del primo job
            long totalLetters = Utility.getLetterCount(conf, totalLetterCountOutputPath);
            // Imposta il numero totale di lettere nella configurazione per il secondo job
            conf.set("totalLetters", String.valueOf(totalLetters));

            Map<String, String> argMap2 = new HashMap<>();
            argMap2.put("input", inputPath);
            argMap2.put("letterFrequenceOtput", letterFrequencyOutputPath);

            // Job 2: Calcolare la frequenza delle lettere
            Job letterFrequencyJob = LetterFrequency.configureJob(conf, argMap2, numReducers);
            System.exit(letterFrequencyJob.waitForCompletion(true) ? 0 : 1);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    
}
