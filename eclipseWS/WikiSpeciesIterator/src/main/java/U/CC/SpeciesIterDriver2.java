// 
 // Author - Jack Hebert (jhebert@cs.washington.edu) 
 // Copyright 2007 
 // Distributed under GPLv3 
 // 
// Modified - Dino Konstantopoulos
// Distributed under the "If it works, remolded by Dino Konstantopoulos, 
// otherwise no idea who did! And by the way, you're free to do whatever 
// you want to with it" dinolicense
// 
package U.CC;

import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapred.JobClient; 
import org.apache.hadoop.mapred.JobConf; 
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;

@SuppressWarnings("deprecation")
public class SpeciesIterDriver2 { 

	private static JobConf getJobConf() {
		JobConf conf = new JobConf(SpeciesIterDriver2.class); 
		conf.setJobName("Species Iter"); 
  
		conf.setNumReduceTasks(5); 
   
		conf.setOutputKeyClass(Text.class); 
		conf.setOutputValueClass(Text.class); 
		conf.setMapperClass(SpeciesIterMapper2.class); 
		conf.setReducerClass(SpeciesIterReducer2.class); 
		conf.setCombinerClass(SpeciesIterReducer2.class); 
		return conf;
	}
	
	public static void main(String[] args) { 
		JobClient client = new JobClient(); 
  
		if (args.length < 2) { 
			System.out.println("Usage: PageRankIter <input path> <output path>"); 
			System.exit(0); 
		} 

		//~dk
		//conf.setInputPath(new Path(args[0])); 
		//conf.setOutputPath(new Path(args[1])); 
 
		int iterationCount = 0;
		String input;
		String output;
		// Iteration counter from 
		// http://hadooptutorial.wikispaces.com/Iterative+MapReduce+and+Counters
		while (iterationCount < 20) {
			if (iterationCount  == 0) // for the first iteration the input will be the first input argument
			     input = args[0];
			else
			    // for the remaining iterations, the input will be the output of the previous iteration
			    input = args[1] + iterationCount;
			
			output = args[1] + (iterationCount + 1); // setting the output file
			
			JobConf conf = getJobConf();
			conf.set("seedValue", args[2]);
			
			FileInputFormat.setInputPaths(conf, new Path(input));
			FileOutputFormat.setOutputPath(conf, new Path(output));
		  
			client.setConf(conf); 
			try { 
				JobClient.runJob(conf); 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
			iterationCount++;
		}
	 } 
 } 
 