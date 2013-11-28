package edu.jhu.ugrad.sseo6.util;

public class Utility {
	
	/**
	 * This may appear like some self-foot-shooting, but
	 * it is required to send the query to SQLManager's
	 * updateQuery method as it takes a string as an argument.
	 * This escapes the string before it is passed as the argument.
	 * @param msg The string to escape.
	 * @return The escaped string.
	 */
	public static String escapeSQLQuery(String msg){
		return escapeDoubleQuotes(escapeBackslashes(msg));
	}
	
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
	}
	
	public static String escapeBackslashes(String msg){
		int c = 0, prev = 0;
		String result = "";
		c = msg.indexOf("\\", c);
		while(c != -1)
		{
			result += msg.substring(prev, c) + "\\" + msg.substring(c, c+1);
			prev = ++c;
			c = msg.indexOf("\\", c);
		}
		result += msg.substring(prev, msg.length());
		
		return result;
	}
}
