package Cloud.ApacheLog;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

@SuppressWarnings("deprecation")
public class SwapReducer extends MapReduceBase 
	implements Reducer<IntWritable, Text, Text, IntWritable> {

	@Override
	public void reduce(IntWritable count, Iterator<Text> ip,
		OutputCollector<Text, IntWritable> output, Reporter reporter)
		throws IOException {
			// swap order of inputs
		if (ip.hasNext()) {
			output.collect(ip.next(), count);
		}
	}
}
