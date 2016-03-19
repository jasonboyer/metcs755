package Cloud.ApacheLog;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

@SuppressWarnings("deprecation")
public class Runner {

  /**
   * @param args
   */
public static void main(String[] args) throws Exception
  {
        JobConf conf = new JobConf(Runner.class);
        conf.setJobName("ip-count");
        
        conf.setMapperClass(IpMapper.class);
        
        conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(IntWritable.class);
        
        conf.setReducerClass(IpReducer.class);

        JobConf sorter = new JobConf(Runner.class);
        sorter.setJobName("ip-sort");
        
        sorter.setMapperClass(DOSFinder.class);
        sorter.setMapOutputKeyClass(IntWritable.class);
        sorter.setMapOutputValueClass(Text.class);

//        sorter.setReducerClass(SwapReducer.class);
        sorter.setOutputKeyComparatorClass(LongWritable.DecreasingComparator.class);
        
        // take the input and output from the command line
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        // Use output of first operation as input to the second operation
        FileInputFormat.setInputPaths(sorter, new Path(args[1]));
        FileOutputFormat.setOutputPath(sorter, new Path(args[2]));

        JobClient.runJob(conf);

        JobClient.runJob(sorter);
	}

}
