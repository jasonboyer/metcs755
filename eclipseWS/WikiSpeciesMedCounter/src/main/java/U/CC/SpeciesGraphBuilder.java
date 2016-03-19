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
// jjboyer@bu.edu
// Some ideas and code taken from these sources:
// 
// http://lintool.github.io/Cloud9/docs/content/counters.html
// http://hadooptutorial.wikispaces.com/Iterative+MapReduce+and+Counters?responseToken=08d21aaa995c0593f1e000955b850443b

package U.CC;

 import org.apache.hadoop.fs.Path; 
 import org.apache.hadoop.io.Text; 
 import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper; 
 import org.apache.hadoop.mapreduce.Reducer; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.mortbay.log.Log;
  
// import org.apache.nutch.parse.Parse; 
// import org.apache.nutch.parse.ParseException; 
// import org.apache.nutch.parse.ParseUtil; 
// import org.apache.nutch.protocol.Content;   
  
public class SpeciesGraphBuilder extends ExampleBaseJob { 

	static enum NumPagesCounter {
		numberOfPages
	}

	public int run(String[] args) throws Exception {
		 Job job = new Job(new Configuration(), "Page-rank Species Graph Builder"); 
	     //JobClient client = new JobClient(); 
	     //JobConf conf = new JobConf(SpeciesGraphBuilder.class); 
	     //conf.setJobName("Page-rank Species Graph Builder"); 
	  
	     //conf.setOutputKeyClass(Text.class); 
	     //conf.setOutputValueClass(Text.class); 
		 job.setJarByClass(SpeciesGraphBuilder.class);
	     job.setMapperClass((Class<? extends Mapper>) SpeciesGraphBuilderMapper.class); 
	  
	     //conf.setInputFormat(org.apache.hadoop.mapred.TextInputFormat.class); 
	     //conf.setOutputFormat(org.apache.hadoop.mapred.SequenceFileOutputFormat.class); 
	
	     job.setReducerClass((Class<? extends Reducer>) SpeciesGraphBuilderReducer.class); 
	     job.setNumReduceTasks(3);

	     job.setOutputKeyClass(Text.class);
	     job.setOutputValueClass(Text.class);
	     
	     //conf.setCombinerClass(SpeciesGraphBuilderReducer.class); 
	  
	     //conf.setInputPath(new Path("graph1")); 
	     //conf.setOutputPath(new Path("graph2")); 
	     // take the input and output from the command line
	     FileInputFormat.setInputPaths(job, new Path(args[0]));
	     FileOutputFormat.setOutputPath(job, new Path(args[1]));
	  
	     job.waitForCompletion(true);
	     Log.info("Job Counters: " + job.getCounters().toString());
	     Counters counters = job.getCounters();
	     for (CounterGroup counter : counters) {
	    	 for (Counter ctr : counter) {
	    		 Log.info(ctr.getName() + " : " + ctr.getValue());
	    	 }
	    	 
	     }

	     // Group name found here: http://lintool.github.io/Cloud9/docs/content/counters.html
	     long totalPages = counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_OUTPUT_RECORDS").getValue();
	     Log.info("Total pages ranked: " + String.valueOf(totalPages));
	     return 0;
	}

	public static void main(String[] args)  throws Exception {
		int res = ToolRunner.run(new Configuration(), new SpeciesGraphBuilder(), args);
		if(args.length != 2) {
			System.err.println("Usage: <in> <output name> ");
		}
		System.exit(res);
	} 
}  