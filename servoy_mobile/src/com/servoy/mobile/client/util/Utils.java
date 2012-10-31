package com.servoy.mobile.client.util;

import java.math.BigDecimal;
import java.util.Date;

import org.timepedia.exporter.client.ExporterBaseActual.JsArrayObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.sksamuel.jqm4gwt.DataIcon;

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
			if (tk != null)
			{
				int[] splitIntegers = new int[tk.length];
				for (int i = 0; i < tk.length; i++)
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
				r = (int)(Math.random() * 16);
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

	/**
	 * Insert an array into another array at a certain position. Both arrays may be null, resulting array will be extended to fit. Element type will be
	 * preserved.
	 * 
	 * @param src
	 * @param toAdd
	 * @param position
	 * @param n
	 * @return the resulting array
	 */
	public static Object[] arrayInsert(Object[] src, Object[] toAdd, int position, int n)
	{
		if (src == null && toAdd == null)
		{
			// nothing to add
			return null;
		}

		Object[] res;
		if (src == null)
		{
			res = new Object[position + n];
			System.arraycopy(toAdd, 0, res, position, Math.min(toAdd.length, n));
		}
		else
		{
			res = new Object[Math.max(src.length, position) + n];
			if (position > 0 && src.length > 0)
			{
				System.arraycopy(src, 0, res, 0, Math.min(src.length, position));
			}
			if (position < src.length)
			{
				System.arraycopy(src, position, res, position + n, src.length - position);
			}
			if (toAdd != null)
			{
				System.arraycopy(toAdd, 0, res, position, Math.min(toAdd.length, n));
			}
		}
		return res;
	}

	/**
	 * Join 2 arrays into 1. Element type will be preserved.
	 * 
	 * @param array1
	 * @param array2
	 * @return the resulting array
	 */
	public static Object[] arrayJoin(Object[] array1, Object[] array2)
	{
		if (array1 == null || (array1.length == 0 && array2 != null))
		{
			return array2;
		}
		if (array2 == null || array2.length == 0)
		{
			return array1;
		}

		return arrayInsert(array1, array2, array1.length, array2.length);
	}

	/**
	 * Add an element to an array. Element type will be preserved.
	 * 
	 * @param array
	 * @param element
	 * @param append
	 * @return the resulting array
	 */
	public static Object[] arrayAdd(Object[] array, Object element, boolean append)
	{
		Object[] res;
		if (array == null)
		{
			res = new Object[1];
		}
		else
		{
			res = new Object[array.length + 1];
			System.arraycopy(array, 0, res, append ? 0 : 1, array.length);
		}
		res[append ? res.length - 1 : 0] = element;
		return res;
	}

	/**
	 * Merge two arrays in 1, the upperArray will be overlaid onto the lowerArray.
	 * 
	 * <p>
	 * For example:
	 * 
	 * <br>
	 * upper = [x, y] lower = [a,b,c] => overlaid = [x, y, c]
	 * 
	 * <br>
	 * upper = [a, b c] lower = [x, y] => overlaid = [a, b, c]
	 * 
	 * @param upperAarray
	 * @param lowerAarray
	 */
	public static Object[] arrayMerge(Object[] upperAarray, Object[] lowerAarray)
	{
		if (upperAarray == null)
		{
			return lowerAarray;
		}
		if (lowerAarray == null || lowerAarray.length <= upperAarray.length)
		{
			return upperAarray;
		}

		// both arrays filled and lowerArray is longer than upperArray
		Object[] mergedArgs = new Object[lowerAarray.length];

		System.arraycopy(upperAarray, 0, mergedArgs, 0, upperAarray.length);
		System.arraycopy(lowerAarray, upperAarray.length, mergedArgs, upperAarray.length, lowerAarray.length - upperAarray.length);
		return mergedArgs;
	}

	/**
	 * Find item in an array of objects
	 * 
	 * @param array of objects to search
	 * @param item to find
	 * @return index of the item in the array or -1 if not found
	 */
	public static int findInArray(Object arrayObj, Object item)
	{
		Object[] array = null;
		JsArrayObject jsArray = null;

		if (arrayObj instanceof Object[])
		{
			array = (Object[])arrayObj;
		}
		else if (arrayObj instanceof JsArrayMixed)
		{
			jsArray = ((JsArrayMixed)arrayObj).cast();
		}
		else if (arrayObj instanceof JsArrayString)
		{
			jsArray = ((JsArrayString)arrayObj).cast();
		}

		if (jsArray != null)
		{
			array = new Object[jsArray.length()];
			for (int i = 0; i < jsArray.length(); i++)
				array[i] = jsArray.getObject(i);
		}

		if (array == null || array.length < 1) return -1;
		for (int i = 0; i < array.length; i++)
		{
			if (Utils.equalObjects(array[i], item)) return i;
		}
		return -1;
	}

	public static DataIcon stringToDataIcon(String dataIcon)
	{
		if (dataIcon != null)
		{
			if (dataIcon.equals("gear")) //$NON-NLS-1$
			return DataIcon.GEAR;
			if (dataIcon.equals("arrow-l")) //$NON-NLS-1$
			return DataIcon.LEFT;
			if (dataIcon.equals("arrow-r")) //$NON-NLS-1$
			return DataIcon.RIGHT;
			if (dataIcon.equals("arrow-u")) //$NON-NLS-1$
			return DataIcon.UP;
			if (dataIcon.equals("arrow-d")) //$NON-NLS-1$
			return DataIcon.DOWN;
			if (dataIcon.equals("delete")) //$NON-NLS-1$
			return DataIcon.DELETE;
			if (dataIcon.equals("plus")) //$NON-NLS-1$
			return DataIcon.PLUS;
			if (dataIcon.equals("minus")) //$NON-NLS-1$
			return DataIcon.MINUS;
			if (dataIcon.equals("check")) //$NON-NLS-1$
			return DataIcon.CHECK;
			if (dataIcon.equals("refresh")) //$NON-NLS-1$
			return DataIcon.REFRESH;
			if (dataIcon.equals("forward")) //$NON-NLS-1$
			return DataIcon.FORWARD;
			if (dataIcon.equals("back")) //$NON-NLS-1$
			return DataIcon.BACK;
			if (dataIcon.equals("grid")) //$NON-NLS-1$
			return DataIcon.GRID;
			if (dataIcon.equals("star")) //$NON-NLS-1$
			return DataIcon.STAR;
			if (dataIcon.equals("alert")) //$NON-NLS-1$
			return DataIcon.ALERT;
			if (dataIcon.equals("info")) //$NON-NLS-1$
			return DataIcon.INFO;
			if (dataIcon.equals("home")) //$NON-NLS-1$
			return DataIcon.HOME;
			if (dataIcon.equals("search")) //$NON-NLS-1$
			return DataIcon.SEARCH;
		}

		return null;
	}
}
