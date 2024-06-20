package it.unipi.hadoop;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.conf.Configuration;


public class LetterCount {
    
    public static class LetterCountMapper extends Mapper<Object, Text, Text, LongWritable> {

        //chiave fissa per tutti i contatori 
        private final Text COUNT_KEY = new Text("TotalLetters");
        private LongWritable letterCount;

        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            // Inizializza il conteggio delle lettere a 0
            letterCount = new LongWritable(0); 
        }
    
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String str = Utility.cleanText(value.toString());  //riga documento composta da solo lettere

            //evita l'accesso alla variabile LongWritable durante il ciclo 
            long localCounter = 0; 

            // Itera su ogni carattere nella stringa
            for (char c : str.toCharArray()) {
                localCounter++; 
            }

            letterCount.set(letterCount.get() + localCounter);
        
        }
    
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            //Scrive il risultato nel contesto con una chiave fissa
            context.write(COUNT_KEY, letterCount);
        }
    }



    public static class LetterCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

        private LongWritable total = new LongWritable();
    
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {
            
            long sum = 0;

            // Itera su tutti i valori associati alla chiave
            for (LongWritable value : values) {
                sum += value.get();
            }
            
            // Imposta il totale come somma dei valori
            total.set(sum);
            
            // Scrive il risultato nel contesto di output
            context.write(key, total);
        }
    }

    
    public static Job configureJob(Configuration conf, Map<String, String> argMap) throws IOException {
        Job TotalLetterCountJob = Job.getInstance(conf, "Letter Count");
    
        // Classi principali
        TotalLetterCountJob.setJarByClass(LetterCount.class);
        TotalLetterCountJob.setMapperClass(LetterCountMapper.class);
        TotalLetterCountJob.setReducerClass(LetterCountReducer.class);
    
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


}





