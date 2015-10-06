package mapdemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
 
public class MapReduce extends Configured implements Tool {
 
    // where to put the data in hdfs when we're done
    //private static final String OUTPUT_PATH = "1-word-freq";
 
    // where to read the data from.
    //private static final String INPUT_PATH = "input";
 
    public int run(String[] args) throws Exception {

    	Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        if (args[0] == null || args[1] == null) {
            System.out.println("You need to provide the arguments of the input and output");
            System.out.println(MapReduce.class.getSimpleName() + " prot:///path/to/input prot:///path/output");
            System.out.println(MapReduce.class.getSimpleName() + " -conf  /path/to/input /path/to/output");
        }

        Path userInputPath = new Path(args[0]);

        // Remove the user's output path
        Path userOutputPath = new Path(args[1]);
        if (fs.exists(userOutputPath)) {
            fs.delete(userOutputPath, true);
        }

        // Remove the phrase of word frequency path
        Path wordFreqPath = new Path(args[2]);
        if (fs.exists(wordFreqPath)) {
            fs.delete(wordFreqPath, true);
        }

        // Remove the phase of word counts path
        Path wordCountsPath = new Path(args[3]);
        if (fs.exists(wordCountsPath)) {
            fs.delete(wordCountsPath, true);
        }

        //Getting the number of documents from the user's input directory.
        FileStatus[] userFilesStatusList = fs.listStatus(userInputPath);
        final int numberOfUserInputFiles = userFilesStatusList.length;
        String[] fileNames = new String[numberOfUserInputFiles];
        for (int i = 0; i < numberOfUserInputFiles; i++) {
            fileNames[i] = userFilesStatusList[i].getPath().getName(); 
        }

        Job job = new Job(conf, "Word Frequence In Document");
        job.setJarByClass(MapReduce.class);
        job.setMapperClass(Mapper1.class);
        job.setReducerClass(Reducer1.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        TextInputFormat.addInputPath(job, userInputPath);
        TextOutputFormat.setOutputPath(job, userOutputPath);

        job.waitForCompletion(true);

        Configuration conf2 = getConf();
        conf2.setStrings("documentsInCorpusList", fileNames);
        Job job2 = new Job(conf2, "Words Counts");
        job2.setJarByClass(MapReduce.class);
        job2.setMapperClass(Mapper2.class);
        job2.setReducerClass(Reducer2.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        TextInputFormat.addInputPath(job2, userOutputPath);
        TextOutputFormat.setOutputPath(job2, wordFreqPath);

        job2.waitForCompletion(true);

        Configuration conf3 = getConf();
        conf3.setInt("numberOfDocsInCorpus", numberOfUserInputFiles);
        Job job3 = new Job(conf3, "TF-IDF of Words in Corpus");
        job3.setJarByClass(MapReduce.class);
        job3.setMapperClass(Mapper3.class);
        job3.setReducerClass(Reducer3.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);
        job3.setInputFormatClass(TextInputFormat.class);
        job3.setOutputFormatClass(TextOutputFormat.class);
        TextInputFormat.addInputPath(job3, wordFreqPath);
        TextOutputFormat.setOutputPath(job3, wordCountsPath);

        return job3.waitForCompletion(true) ? 0 : 1;
    }
 
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MapReduce(), args);
        System.exit(res);
    }
}
