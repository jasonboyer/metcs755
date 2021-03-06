/****************************************************************************/
/************************** WikiLengths.java *******************************/
/****************************************************************************/

package com.github.jasonboyer.cs755; 

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class WikiEditHist {
	/* this is intended as a beginners task... read the wikipedia dump file (43GB non zipped) and output the feeder
	 * a histogram of number of pages vs number of bytes/page   (each wikipedia article is one <page> </page>)
 	 * 	 	 
 	 */
	private static final transient Logger logger = org.apache.logging.log4j.LogManager.getLogger("app");

	public static void runJob(String input, String output) throws IOException {
		logger.debug("cme@ runJob with input="+input+"  output="+output);
		Configuration conf = new Configuration();
		conf.set("xmlinput.start", "<page>");
		conf.set("xmlinput.end", "</page>");
		conf.set(
			"io.serializations",
			"org.apache.hadoop.io.serializer.JavaSerialization,org.apache.hadoop.io.serializer.WritableSerialization"
			);
		logger.debug("cme@ runJob done configuring");

		/* these two lines enable bzip output from the reducer */
		//conf.setBoolean("mapred.output.compress", true);
		//		//conf.setClass  ("mapred.output.compression.codec", BZip2Codec.class,CompressionCodec.class);
		SimpleDateFormat ymdhms=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); /* ISO 8601 format */
		Job job = new Job(conf, "wikPageLengths "+ymdhms.format(new Date()));
		logger.debug("cme@ runJob created job");

		FileInputFormat.setInputPaths(job, input);
		job.setJarByClass(WikiEditHist.class);
		logger.debug("cme@ runJob setJarByClass");
				
		job.setMapperClass  (WikiEditHistMapper .class);
		/*job.setCombinerClass(WikiLengthsReducer.class); This is how to get a mapper which never completes... specify a reducer with different outputs from it's inputs as a combiner */
		job.setReducerClass (WikiEditHistReducer.class);
		//job.setNumReduceTasks(0);

		job.setInputFormatClass(TextInputFormat.class);

		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(NullWritable.class);

		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(LongWritable.class);
		logger.debug("cme@ runJob done setting classes");

		Path outPath = new Path(output);
		FileOutputFormat.setOutputPath(job, outPath);
		FileSystem dfs = FileSystem.get(outPath.toUri(), conf);
		if (dfs.exists(outPath)) {
			dfs.delete(outPath, true);
		}
		logger.debug("cme@ runJob done setting paths");

		try {
			job.waitForCompletion(true);
		} catch (InterruptedException ex) {
			//Logger.getLogger(WikiSee2.class.getName()).log(Level.SEVERE, null, ex);
			logger.fatal("InterruptedException "+ WikiEditHist.class.getName()+" "+ex);
		} catch (ClassNotFoundException ex) {
			logger.fatal("ClassNotFoundException "+ WikiEditHist.class.getName()+" "+ex);
		}

	}
	public static void main(String[] args) {
		logger.debug("cme@ main");
		try {
			runJob(args[0], args[1]);
		} catch (IOException ex) {
			logger.fatal("IOException "+ WikiEditHist.class.getName()+" "+ex);
		}
	}
}

