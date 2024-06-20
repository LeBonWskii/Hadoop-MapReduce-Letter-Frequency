//in questo job conto il totale delle Lettere presenti nel file di input

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



public class TotalLetterCount{

    private static class TotalLetterCountMapper extends Mapper<Object, Text, Text, LongWritable>
    {
        private final static LongWritable ONE = new LongWritable(1);
        private final static Text TotalLetter = new Text("TotalLetters");

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            String str = Utility.cleanText(value.toString()); //uso la funzione utilità per pulire il testo
            
            for(char c : str.toCharArray())
            {
                context.write(TotalLetter, ONE);  //contiamo ogni TotalLetter
            }
        }
    }


    public static class TotalLetterCountReducer extends Reducer<Text, LongWritable, Text, LongWritable>
    {
        private LongWritable result = new LongWritable();

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
        {
            int sum = 0;
            for(LongWritable val : values)
            {
                sum += val.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

   public static Job configureJob(Configuration conf, Map<String, String> argMap/*, int numReducerTasks*/) throws IOException {
        Job TotalLetterCountJob = Job.getInstance(conf, "Letter Count");
    
        // Classi principali
        TotalLetterCountJob.setJarByClass(TotalLetterCount.class);
        TotalLetterCountJob.setMapperClass(TotalLetterCountMapper.class);
        TotalLetterCountJob.setReducerClass(TotalLetterCountReducer.class);
    
        // Classe Combiner
        TotalLetterCountJob.setCombinerClass(TotalLetterCountReducer.class);
    
        /*
        In questo script basta che il reducer sia sempre uno
        if (argMap.containsKey("numReducers")) {
            TotalLetterCountJob.setNumReduceTasks(Integer.parseInt(argMap.get("numReducers")));
        } else {
            TotalLetterCountJob.setNumReduceTasks(numReducerTasks);
        }
        */
    
        // classi per l'output del mapper
        TotalLetterCountJob.setMapOutputKeyClass(Text.class);
        TotalLetterCountJob.setMapOutputValueClass(LongWritable.class);
    
        // classi per l'output del reducer
        TotalLetterCountJob.setOutputKeyClass(Text.class);
        TotalLetterCountJob.setOutputValueClass(LongWritable.class);
    
        // Percorsi per l'input e l'output
        FileInputFormat.addInputPath(TotalLetterCountJob, new Path(argMap.get("input")));
        FileOutputFormat.setOutputPath(TotalLetterCountJob, new Path(argMap.get("TotalLetterCountOutput")));
    
        // Formati di input e output
        TotalLetterCountJob.setInputFormatClass(TextInputFormat.class);
        TotalLetterCountJob.setOutputFormatClass(TextOutputFormat.class);
    
        return TotalLetterCountJob;
    }
    
/*
   public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: wordcount <in> [<in>...] <out>");
      System.exit(2);
    }
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(TotalLetterCount.class);
    job.setMapperClass(TotalLetterCountMapper.class);
    job.setCombinerClass(TotalLetterCountReducer.class);
    job.setReducerClass(TotalLetterCountReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,
      new Path(otherArgs[otherArgs.length - 1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
    */
}