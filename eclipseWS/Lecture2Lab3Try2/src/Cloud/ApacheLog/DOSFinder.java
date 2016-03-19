package Cloud.ApacheLog;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class DOSFinder extends MapReduceBase 
implements Mapper<LongWritable, Text, IntWritable, Text> {

	// Regular expression to match the IP address hit count in
	// the output of the previous reduce operation
	private static final Pattern linePattern = Pattern.compile("^([\\d\\.]+)\\s(\\d+)");

	// Only output hit counts that are greater than this threshold
	private static final int threshold = 100;
	  
	public void map(LongWritable fileOffset, Text lineContents,
	    OutputCollector<IntWritable, Text> output, Reporter reporter)
	    throws IOException {
    
		// apply the regex to the line of the access log
		Matcher matcher = linePattern.matcher(lineContents.toString());
		if(matcher.find()) {
			// grab the IP
			String ip = matcher.group(1);
			// grab the count
			int count = Integer.parseInt(matcher.group(2)); 
			// output it if greater than the threshold
			if (count > threshold) {
				IntWritable attackerHits = new IntWritable(count);
				output.collect(attackerHits, new Text(ip));
			}
		}
	}
}
