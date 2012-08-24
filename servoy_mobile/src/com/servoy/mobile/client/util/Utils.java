package com.servoy.mobile.client.util;

import java.math.BigDecimal;
import java.util.Date;

import com.google.gwt.core.client.GWT;

/*
This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along
with this program; if not, see http://www.gnu.org/licenses or write to the Free
Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
*/

/**
 * Utils class for various helper functions
 * @author jblok
 */
public class Utils 
{
	/**
	 * Try to parse the given string as an integer
	 * 
	 * @param s the string to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static int getAsInteger(String s)
	{
		if (s == null) return 0;
		try
		{
			return new Double(s.replace(',', '.')).intValue();
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	
	/**
	 * Try to parse the given object as an integer
	 * 
	 * @param o the object (Number, String, ...) to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static int getAsInteger(Object o)
	{
		if (o == null) return 0;
		if (o instanceof Number)
		{
			return ((Number)o).intValue();
		}
		if (o instanceof Boolean)
		{
			return ((Boolean)o).booleanValue() ? 1 : 0;
		}
		return getAsInteger(o.toString());
	}
	
	/**
	 * Try to parse the given string as an double
	 * 
	 * @param s the string to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static double getAsDouble(String s)
	{
		if (s == null) return 0;
		try
		{
			return new Double(s.replace(',', '.')).doubleValue();
		}
		catch (Exception ex)
		{
			return 0;
		}
	}

	/**
	 * Try to parse the given object as an integer
	 * 
	 * @param o the object (Number, String, ...) to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static double getAsDouble(Object o) 
	{
		if (o == null) return 0;
		if (o instanceof Number)
		{
			return ((Number)o).doubleValue();
		}
		return getAsDouble(o.toString());
	}

	/**
	 * Try to parse the given string as an long
	 * 
	 * @param s the string to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static long getAsLong(String s)
	{
		if (s == null) return 0;
		try
		{
			return new Double(s.replace(',', '.')).longValue();
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	
	/**
	 * Try to parse the given object as an long
	 * 
	 * @param o the object (Number, String, ...) to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static long getAsLong(Object o)
	{
		if (o == null) return 0;
		if (o instanceof Number)
		{
			return ((Number)o).longValue();
		}
		if (o instanceof Boolean)
		{
			return ((Boolean)o).booleanValue() ? 1 : 0;
		}
		return getAsLong(o.toString());
	}
	
	public static int[] splitAsIntegers(String d)
	{
		if (d == null) return null;

		try
		{
			String tk[] = d.split(",");
			if(tk != null)
			{
				int[] splitIntegers = new int[tk.length];
				for(int i = 0; i < tk.length; i++)
				{
					splitIntegers[i] = Utils.getAsInteger(tk[i]);
				}
				return splitIntegers;
			}
		}
		catch (Exception ex)
		{
			GWT.log("Error during splitAsIntegers", ex);
		}
		
		return null;
	}	

	private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(); 
	public static String createStringUUID() 
	{
		char[] uuid = new char[36];
		int r;

		// rfc4122 requires these characters
		uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
		uuid[14] = '4';

		// Fill in random data.  At i==19 set the high bits of clock sequence as per rfc4122, sec. 4.1.5
		for (int i = 0; i < 36; i++) 
		{
			if (uuid[i] == 0) 
			{
				r = (int) (Math.random()*16);
				uuid[i] = CHARS[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
			}
		}
		return new String(uuid);
	}
	
	public static String createPKHashKey(Object[] pk)
	{
		StringBuilder sb = new StringBuilder();
		if (pk != null)
		{
			for (Object val : pk)
			{
				String str;
				if (val instanceof String && ((String)val).length() == 36 && ((String)val).split("-").length == 5) //$NON-NLS-1$
				{
					// make sure UUID PKs are matched regardless of casing (MSQ Sqlserver returns uppercase UUID strings for uniqueidentifier columns)
					str = ((String)val).toLowerCase();
				}
				else
				{
					str = convertToString(val);
				}
				if (val != null)
				{
					sb.append(str.length());
				}
				sb.append('.');
				sb.append(str);
				sb.append(';');
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convert to string representation, remove trailing '.0' for numbers.
	 * 
	 * @return the result string
	 */
	public static String convertToString(Object o)
	{
		if (!(o instanceof Number))
		{
			return String.valueOf(o);
		}

		String numberToString = o.toString();
		int i;
		for (i = numberToString.length() - 1; i > 0; i--)
		{
			if (numberToString.charAt(i) != '0')
			{
				break;
			}
		}
		if (numberToString.charAt(i) == '.')
		{
			return numberToString.substring(0, i);
		}
		return numberToString;
	}
	
	public static final double DEFAULT_EQUALS_PRECISION = 1e-7d;

	//null,null == true
	public final static boolean equalObjects(Object oldObj, Object obj)
	{
		return equalObjects(oldObj, obj, DEFAULT_EQUALS_PRECISION, false);
	}

	public final static boolean equalObjects(Object oldObj, Object obj, boolean ignoreCase)
	{
		return equalObjects(oldObj, obj, DEFAULT_EQUALS_PRECISION, ignoreCase);
	}

	public final static boolean equalObjects(Object oldObj, Object obj, double equalsPrecision)
	{
		return equalObjects(oldObj, obj, equalsPrecision, false);
	}

	public final static boolean equalObjects(Object oldObj, Object obj, double equalsPrecision, boolean ignoreCase)
	{
		if (oldObj == obj)
		{
			return true;
		}
		if (oldObj == null && obj != null)
		{
			return false;
		}
		if (oldObj != null && obj == null)
		{
			return false;
		}

		//in case one side is String and other Number -> make both string
		if (oldObj instanceof Number && obj instanceof String)
		{
			try
			{
				obj = new Double((String)obj);
			}
			catch (Exception e)
			{
				oldObj = oldObj.toString();
			}
		}
		else if (obj instanceof Number && oldObj instanceof String)
		{
			try
			{
				oldObj = new Double((String)oldObj);
			}
			catch (Exception e)
			{
				obj = obj.toString();
			}
		}

		// separate tests for BigDecimal and Long, the tests based on Double may give
		// incorrect results for Long values not fitting in a double mantissa.
		// note that 2.0 is not equal to 2.00 according to BigDecimal.equals()
		if ((oldObj instanceof BigDecimal && obj instanceof BigDecimal && (((BigDecimal)oldObj).scale() == ((BigDecimal)obj).scale())) ||
			(oldObj instanceof Long && obj instanceof Long))
		{
			return oldObj.equals(obj);
		}
		if (oldObj instanceof BigDecimal && ((BigDecimal)oldObj).scale() == 0 && obj instanceof Long)
		{
			return ((BigDecimal)oldObj).longValue() == ((Long)obj).longValue();
		}
		if (obj instanceof BigDecimal && ((BigDecimal)obj).scale() == 0 && oldObj instanceof Long)
		{
			return ((BigDecimal)obj).longValue() == ((Long)oldObj).longValue();
		}

		// Always cast to double so we don't lose precision.
		if (oldObj instanceof Number && obj instanceof Number)
		{
			if (oldObj instanceof Float || oldObj instanceof Double || oldObj instanceof BigDecimal || obj instanceof Float || obj instanceof Double ||
				obj instanceof BigDecimal)
			{
				double a = ((Number)oldObj).doubleValue();
				double b = ((Number)obj).doubleValue();
				return a == b || Math.abs(a - b) < equalsPrecision;
			}
			return ((Number)oldObj).longValue() == ((Number)obj).longValue();
		}

		if (oldObj instanceof Date && obj instanceof Date)
		{
			return (((Date)oldObj).getTime() == ((Date)obj).getTime());
		}

		if (ignoreCase && oldObj instanceof String && obj instanceof String)
		{
			return ((String)oldObj).equalsIgnoreCase((String)obj);
		}

		return oldObj.equals(obj);
	}
}
