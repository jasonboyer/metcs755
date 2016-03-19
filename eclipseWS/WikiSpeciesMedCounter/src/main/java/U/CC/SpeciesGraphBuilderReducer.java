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

 import java.io.IOException; 
 import java.util.Iterator; 
  
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text; 
 import org.apache.hadoop.io.WritableComparable; 
 import org.apache.hadoop.mapred.OutputCollector; 
 import org.apache.hadoop.mapreduce.Reducer;
import org.mortbay.log.Log;
import org.apache.hadoop.mapred.Reporter; 
 import java.lang.StringBuilder; 
 import java.util.*; 
  
public class SpeciesGraphBuilderReducer extends Reducer<Text, Text, Text, Text>
{ 
  
   public void reduce(Text key, Iterator<Text> values, Context context)
                      throws IOException, InterruptedException { 
  
     context.setStatus(key.toString()); 
     String toWrite = ""; 
//     int count = 0;
     Double initPageRank = 0.0;
     while (values.hasNext()) 
     { 
        String page = ((Text)values.next()).toString(); 
        page.replaceAll(" ", "_"); 
        toWrite += " " + page; 
        initPageRank += 0.1;
     } 

     //while (values.hasNext())
     //{
     //   String page = ((Text)values.next()).toString(); 
     //   count = GetNumOutlinks(page);      
     //   page.replaceAll(" ", "_"); 
     //   toWrite += " " + page;
     //} 
  
     // IntWritable i = new IntWritable(count);
     // String num = (i).toString(); 
     String num = initPageRank.toString();
     toWrite = num + ":" + toWrite; 
     context.write(key, new Text(toWrite)); 
     Log.info("Wrote: " + toWrite + " for key " + key);
   } 

    public int GetNumOutlinks(String page)
    {
        if (page.length() == 0)
            return 0;

        int num = 0;
        String line = page;
        int start = line.indexOf(" ");
        while (-1 < start && start < line.length())
        {
            num = num + 1;
            line = line.substring(start+1);
            start = line.indexOf(" ");
        }
        return num;
    }
 } 
 