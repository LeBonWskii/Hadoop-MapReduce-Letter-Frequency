package it.unipi.hadoop;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
 
public class Utility {
 
    public static String cleanText(String input) {
            if (input == null) {
                return null;
            }
 
            // Normalizzo il testo con scomposizione dei caratteri accentati in carattere base + diacritico
            String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        
            // rimuovo i diacritici
            String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
            //Rimozione di caratteri speciali e trasformazione di tutto il testo in minuscolo
            String clean = withoutDiacritics.replaceAll("[^a-zA-Z]", "").toLowerCase();
        
            return clean;
        }
        

    public static long getLetterCount(Configuration conf, String LetterCountOutput) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path outputPath = new Path(LetterCountOutput);
        long letterCount = 0;

        // lista file nella cartella output
        FileStatus[] status = fs.listStatus(outputPath);
        for (FileStatus fileStatus : status) {
            String fileName = fileStatus.getPath().getName();
            // Ignora _SUCCESS file
            if (fileName.equals("_SUCCESS")) {
                continue;
            }
            try (FSDataInputStream inputStream = fs.open(fileStatus.getPath());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

                String firstLine = bufferedReader.readLine();
                if (firstLine != null) {
                    String[] keyValue = firstLine.split("\t");
                    if (keyValue.length >= 2) {
                        letterCount += Long.parseLong(keyValue[1].trim());  //trim rimuove eventuali spazi bianchi
                    } else {
                        System.err.println("Formato della linea non valido: " + firstLine);
                    }

                    System.err.println("Il file di output è vuoto: " + fileName);
                }

                bufferedReader.close();
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } 
        }
        return letterCount;
    }
}
