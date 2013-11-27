package edu.jhu.ugrad.sseo6.util;

public class Utility {
	public static String escapeDoubleQuotes(String msg){
		int c = 0, prev = 0;
		String result = "";
		c = msg.indexOf("\"", c);
		while(c != -1)
		{
			result += msg.substring(prev, c) + "\\" + msg.substring(c, c+1);
			prev = ++c;
			c = msg.indexOf("\"", c);
		}
		result += msg.substring(prev, msg.length());
		
		return result;
		
		//return msg.replaceAll("\"", "\\\"");
	}
}
