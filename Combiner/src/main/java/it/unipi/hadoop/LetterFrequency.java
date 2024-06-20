//In questo job sfrutto i risultati precedenti per calcolare la frequenza delle lettere

package it.unipi.hadoop;

import java.util.HashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import org.apache.hadoop.conf.Configuration;

public class LetterFrequency
{
    public static class LetterFrequencyMapper extends Mapper<Object, Text, Text, LongWritable>
    {
        private final static LongWritable ONE = new LongWritable(1);

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            String str = Utility.cleanText(value.toString());

            for(char c : str.toCharArray())
            {
                context.write(new Text(String.valueOf(c)), ONE);
            }
        }
    }

    public static class LetterFrequencyCombiner extends Reducer<Text, LongWritable, Text, LongWritable> {
        private LongWritable result = new LongWritable();

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
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
            double frequency = (double) sum / TOTAL_LETTERS;
            context.write(key, new DoubleWritable(frequency));
        }
    }

    public static Job configureJob(Configuration conf, Map<String, String> argMap, int numReducerTasks) throws IOException {
        Job letterFrequencyJob = Job.getInstance(conf, "Letter Frequency");

        // Classi principali
        letterFrequencyJob.setJarByClass(LetterFrequency.class);
        letterFrequencyJob.setMapperClass(LetterFrequencyMapper.class);
        letterFrequencyJob.setCombinerClass(LetterFrequencyCombiner.class);
        letterFrequencyJob.setReducerClass(LetterFrequencyReducer.class);
        

        // Numero di reducer, per la valutazione delle performance
        if (argMap.containsKey("numReducers")) {
            letterFrequencyJob.setNumReduceTasks(Integer.parseInt(argMap.get("numReducers")));
        } else {
            letterFrequencyJob.setNumReduceTasks(numReducerTasks);
        }

        letterFrequencyJob.setMapOutputKeyClass(Text.class);
        letterFrequencyJob.setMapOutputValueClass(LongWritable.class);

        letterFrequencyJob.setOutputKeyClass(Text.class);
        letterFrequencyJob.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(letterFrequencyJob, new Path(argMap.get("input")));
        FileOutputFormat.setOutputPath(letterFrequencyJob, new Path(argMap.get("letterFrequencyOutput")));

        letterFrequencyJob.setInputFormatClass(TextInputFormat.class);
        letterFrequencyJob.setOutputFormatClass(TextOutputFormat.class);

        return letterFrequencyJob;
    }
}