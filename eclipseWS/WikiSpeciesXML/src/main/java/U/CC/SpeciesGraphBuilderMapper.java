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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.util.*; 
import java.lang.StringBuilder; 
  
 /* 
  * This class reads in a serialized download of wikispecies, extracts out the links, and 
  * foreach link: 
  *   emits (currPage, (linkedPage, 1)) 
  * 
  * 
  */ 
public class SpeciesGraphBuilderMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> { 
  
	private static final String NAME_MARKER = "== Name ==";
	private static final String NAME_MARKER_NOSPACE = "==Name==";

	private static java.util.regex.Pattern regex1 = Pattern.compile("(== Taxonavigation ==|==Taxonavigation==)([^(==)]+)");
	private static java.util.regex.Pattern regex2 = Pattern.compile("("+NAME_MARKER+"|"+NAME_MARKER_NOSPACE+")([^(==)]+)");
	private static int countTitles = 0;
	
	private synchronized void addTitle() {
		countTitles++;
		System.out.println("Map(): " + countTitles);
	}

	private int skipCrap(int start, String page) {
		// Skip crap
		while (start < page.length() && 
				(page.charAt(start) == '\'' || 
				 page.charAt(start) == ' ' || 
				 page.charAt(start) == '\t' || 
				 page.charAt(start) == '\n' ||
				 page.charAt(start) == '*'
				 ))
			start++;
		return start;
	}
	
	@SuppressWarnings("unchecked")
	public void map(LongWritable key, Text value, 
	                   OutputCollector output, Reporter reporter) throws IOException
	{
	  
		try {
			//===============================================
			//              XML Code
			//===============================================
	
			ArrayList<String> outlinks = new ArrayList<String>(); 
			String title = null;
			boolean found = false;
			
			Matcher match = regex1.matcher(value.toString());
			while (match.find()) {
				String page = match.group();
				System.out.println("Page:" + page); 
				getLinks(page, outlinks);
			}
		
			match = regex2.matcher(value.toString());
			while (match.find()) {
				String page = match.group();
				title = GetTitle(page);
				addTitle();
				
				// TODO: why is this here?
				// found = true;
				//getLinks(page, outlinks);
			}
	
			if (title != null && title.length() > 0) { 
				reporter.setStatus(title); 
			} else {
				// Don't continue without a name for this species
				return;
			}
		  
			//ArrayList<String> outlinks = this.GetOutlinks(page); 
			StringBuilder builder = new StringBuilder(); 
			for (String link : outlinks) { 
				link = link.replace(" ", "_"); 
				link = link.replace(":", "_");
				builder.append(" "); 
				builder.append(link); 
			} 
			output.collect(new Text(title), new Text(builder.toString())); 
		} catch (Exception e) {
			System.out.println("Mapper caught exception, count = " + String.valueOf(countTitles) + ", text = " + value.toString() + ", exception: " + e);
		}
	} 
	  
	public String GetTitle(String page) throws IOException{ 
		String filtered = null;
		int start = page.indexOf(NAME_MARKER);
		if (start >= 0)
				start += NAME_MARKER.length();
		else {
			start = page.indexOf(NAME_MARKER_NOSPACE);
			if (start >= 0)
				start += NAME_MARKER_NOSPACE.length();
		}
		if (start == -1)
			return "";

		start = skipCrap(start, page);
		if (start == page.length())
			return "";
		
		int end = page.indexOf("[[");
		if (start >= 0 && end > 0)
			filtered = page.substring(start, end).trim();
		else
			filtered = page.substring(start).trim();
		if (filtered.length() == 0) {
			// Possible anomaly: ==Name== [[Vertebrata]] for example
			start = page.indexOf("[[");
			if (start >= 0) {
				start += 2;
				end = page.indexOf("]]", start);
				if (end >= start)
					filtered = page.substring(start, end);
			}
			if (filtered.length() == 0)
				return "";
		} else if (filtered.length() == 2) {
			// Possible anomaly: ==Name== ''[[Trichoplax]]'' for example
			start = page.indexOf("''");
			if (start >= 0 ) {
				start += 2;
				end = page.indexOf("''", start);
				if (end > start)
					filtered = page.substring(start, end);
			}
		}
		
		start = 0;
		while (start < filtered.length() && (filtered.charAt(start) == '\'' || filtered.charAt(start) == '['))
			start++;
		end = start;
		while (end < filtered.length() && (filtered.charAt(end) != '\'' && filtered.charAt(end) != ']'))
			end++;
		return filtered.substring(start, end).trim();
	} 
	  
	public ArrayList<String> GetOutlinks(String page){ 
		int end; 
		ArrayList<String> outlinks = new ArrayList<String>();
		String taxo = GetTaxo(page);
		int start=taxo.indexOf("[["); 
		while (start > 0) { 
			start = start+2; 
			end = taxo.indexOf("]]", start); 
			//if((end==-1)||(end-start<0)) 
			if (end == -1) { 
				break; 
			} 
	  
			String toAdd = taxo.substring(start); 
			toAdd = toAdd.substring(0, end-start); 
			outlinks.add(toAdd); 
			start = taxo.indexOf("[[", end+1); 
		} 
		return outlinks; 
	}
	
	private String GetTaxo(String page) {
		int start = page.indexOf("== Taxonavigation ==");
		if (start > 0) {
			int end = page.indexOf("== Name ==");
			return page.substring(start, end);
		}
		return "";
	} 
	
	private void getLinks(String page, List<String> links) {
		int start = page.indexOf("[[");
		int end;
		while (start > 0) {
			start = start + 2;
			end = page.indexOf("]]", start);
			if (end == -1) {
				break;
			}
			String toAdd = page.substring(start);
			toAdd = toAdd.substring(0, (end - start));
			links.add(toAdd);
			start = page.indexOf("[[", end + 1);
		}
		return;
	}

}

