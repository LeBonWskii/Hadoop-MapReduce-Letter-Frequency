package it.unipi.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.conf.Configuration;


public class LetterFrequency {

    public static class LetterFrequencyMapper extends Mapper<Object, Text, Text, LongWritable>{

        private Map<Text, LongWritable> map;        //mappa in cui salviamo le coppie k,v

        @Override
        public void setup(Context context) {
            //inizializzo mappa
            map = new HashMap<>();
        }

        @Override
        public void map(Object key, Text value, Context context) {
           
            String str = Utility.cleanText(value.toString()); //riga documento composta da solo lettere 

            for (char ch : str.toCharArray()) {
               
                Text charText = new Text(String.valueOf(ch));      //settiamo charText con il carattere corrente 
                //Aggiungiamo o aggiorniamo la lettera nella mappa. 
                //getOrDefault otteniene il valore corrente (0 se la chiave non esiste) e incrementiamo il conteggio.
                map.put(charText, new LongWritable(map.getOrDefault(charText, new LongWritable(0)).get() + 1));
                
            }
        }

        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            //iteriamo ogni coppia k,v della mappa 
            for (Map.Entry<Text, LongWritable> entry : map.entrySet()) {
                context.write(entry.getKey(), entry.getValue());
            }
        }

    }


    public static class LetterFrequencyReducer extends Reducer<Text, LongWritable, Text, DoubleWritable> {
        
        private static long TOTAL_LETTERS = 0;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            //otteniamo il valore di config associato alla chiave 'totalLetters' e lo convertiamo in Long
            TOTAL_LETTERS = Long.parseLong(conf.get("totalLetters"));
        }

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            double frequency = (double) sum / (double) TOTAL_LETTERS;
            context.write(key, new DoubleWritable(frequency));
        }
    }

    public static Job configureJob(Configuration conf, Map<String, String> argMap, int numReducerTasks) throws IOException {
        Job letterFrequencyJob = Job.getInstance(conf, "Letter Frequency");

        letterFrequencyJob.setJarByClass(LetterFrequency.class);
        letterFrequencyJob.setMapperClass(LetterFrequencyMapper.class);
        letterFrequencyJob.setReducerClass(LetterFrequencyReducer.class);
        
        // Numero di reducer, per la valutazione delle performance
        if (argMap.containsKey("numReducers")) {
            letterFrequencyJob.setNumReduceTasks(Integer.parseInt(argMap.get("numReducers")));
        } else {
            letterFrequencyJob.setNumReduceTasks(numReducerTasks);
        }

        // classi per l'output del mapper
        letterFrequencyJob.setMapOutputKeyClass(Text.class);
        letterFrequencyJob.setMapOutputValueClass(LongWritable.class);

        // classi per l'output del reducer
        letterFrequencyJob.setOutputKeyClass(Text.class);
        letterFrequencyJob.setOutputValueClass(DoubleWritable.class);

        // Percorsi per l'input e l'output
        FileInputFormat.addInputPath(letterFrequencyJob, new Path(argMap.get("input")));
        FileOutputFormat.setOutputPath(letterFrequencyJob, new Path(argMap.get("letterFrequenceOtput")));
        
        // Formati di input e output
        letterFrequencyJob.setInputFormatClass(TextInputFormat.class);
        letterFrequencyJob.setOutputFormatClass(TextOutputFormat.class);

        return letterFrequencyJob;
    }

}

