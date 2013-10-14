package com.servoy.mobile.client.util;

import java.math.BigDecimal;
import java.util.Date;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterBaseActual.JsArrayObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.servoy.base.query.BaseAndCondition;
import com.servoy.base.query.BaseCompareCondition;
import com.servoy.base.query.BaseOrCondition;
import com.servoy.base.query.BaseQueryColumn;
import com.servoy.base.query.IBaseSQLCondition;
import com.servoy.mobile.client.dataprocessing.Record;
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
@ExportPackage("internal")
public class Utils implements Exportable
{

	// add more here if more primitive types are used
	@Export
	public static Number wrapIfPrimitive(double i)
	{
		// this will get called for all numeric types (it would be useless to have separate methods for int/byte for example cause in JS all are numbers so they won't map correctly)
		return Double.valueOf(i);
	}

	@Export
	public static Object wrapIfPrimitive(boolean b)
	{
		return Boolean.valueOf(b);
	}

	@Export
	public static Object wrapIfPrimitive(Object o)
	{
		return o;
	}

	/**
	 * Try to parse the given string as an integer
	 * 
	 * @param s the string to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static int getAsInteger(String s)
	{
		return getAsInteger(s, 0);
	}

	/**
	 * Try to parse the given string as an integer
	 * 
	 * @param s the string to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static int getAsInteger(String s, int defaultValue)
	{
		if (s == null) return defaultValue;
		try
		{
			return new Double(s.replace(',', '.')).intValue();
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}


	public static boolean isInteger(String s)
	{
		try
		{
			new Double(s.replace(',', '.')).intValue();
			return true;
		}
		catch (Exception ex)
		{

		}
		return false;
	}

	/**
	 * Try to parse the given object as an integer
	 * 
	 * @param o the object (Number, String, ...) to parse
	 * @return the parsed integer - or 0 (zero) if the parse doesn't succeed
	 */
	public static int getAsInteger(Object o)
	{
		return getAsInteger(o, 0);
	}

	/**
	 * Try to parse the given object as an integer
	 * 
	 * @param o the object (Number, String, ...) to parse
	 * @return the parsed integer or the defaultValue if the parse doesn't succeed
	 */
	public static int getAsInteger(Object o, int defaultValue)
	{
		if (o == null) return defaultValue;
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
	 * helper function to get boolean value of java.lang.Boolean object or default value in case of null
	 * @param booleanObject
	 * @param defaultValue 
	 * @return 
	 */
	public static boolean getAsBoolean(Object object, boolean defaultValue)
	{
		if (object instanceof Boolean)
		{
			return ((Boolean)object).booleanValue();
		}
		else if (object instanceof Number)
		{
			return ((Number)object).intValue() != 0;
		}
		return defaultValue;
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

	public static int[] splitInTwoIntegers(String d)
	{
		if (d == null) return null;

		int[] xy = Utils.splitAsIntegers(d);
		if (xy != null && xy.length == 2) return xy;
		return null;
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

	// it will need a further .cast() to really be useable; function properties are not cloned, but copied, can we clone them as well ?
	public static JavaScriptObject cloneDeep(JavaScriptObject o)
	{
		return copyFunctions(o, JSONParser.parseLenient(new JSONObject(o).toString()).isObject().getJavaScriptObject());
	}

	private static native JavaScriptObject copyFunctions(JavaScriptObject original, JavaScriptObject clone) /*-{
		for ( var key in original) {
			if (typeof original[key] == 'function') {
				clone[key] = original[key];
			}
		}
		return clone;
	}-*/;

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

	public static Object parseJSExpression(Object o)
	{
		if (o instanceof String)
		{
			String s = ((String)o).trim();
			if ("".equals(s)) return null;
			if ("true".equals(s)) return Boolean.TRUE;
			if ("false".equals(s)) return Boolean.FALSE;
			try
			{
				return Double.valueOf(s);
			}
			catch (NumberFormatException e)
			{
				if ((s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'') || (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"'))
				{
					return s.substring(1, s.length() - 1);
				}
			}
			return null;
		}

		// non-string, keep original
		return o;
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
			{
				try
				{
					array[i] = jsArray.getNumberObject(i);
				}
				catch (Exception e)
				{
					array[i] = jsArray.getObject(i);
				}
			}
		}

		if (array == null || array.length < 1) return -1;
		for (int i = 0; i < array.length; i++)
		{
			if (Utils.equalObjects(array[i], item)) return i;
		}
		return -1;
	}

	public static String getJSONString(JavaScriptObject o)
	{
		return new JSONObject(o).toString();
	}

	public static DataIcon stringToDataIcon(String dataIcon)
	{
		if (dataIcon != null)
		{
			for (DataIcon icon : DataIcon.values())
			{
				if (dataIcon.equals(icon.getJqmValue()))
				{
					return icon;
				}
			}
		}

		return null;
	}

	public static String[] unwrapArray(JsArrayString jsArrayString)
	{
		String[] result = new String[jsArrayString.length()];
		for (int i = 0; i < jsArrayString.length(); i++)
		{
			result[i] = jsArrayString.get(i);
		}
		return result;
	}

	public static boolean evalCondition(IBaseSQLCondition condition, Record record)
	{
		if (condition != null)
		{
			if (condition instanceof BaseAndCondition)
			{
				boolean retVal = true;
				for (IBaseSQLCondition andCondition : ((BaseAndCondition)condition).getConditions())
				{
					retVal = retVal && evalCondition(andCondition, record);
					if (!retVal) break;
				}
				return retVal;
			}
			if (condition instanceof BaseOrCondition)
			{
				boolean retVal = false;
				for (IBaseSQLCondition orCondition : ((BaseOrCondition)condition).getConditions())
				{
					retVal = retVal || evalCondition(orCondition, record);
					if (retVal) break;
				}
				return retVal;
			}
			if (condition instanceof BaseCompareCondition)
			{
				BaseCompareCondition compareCondition = (BaseCompareCondition)condition;
				int operator = compareCondition.getOperator();
				String dataprovider = null;
				if (compareCondition.getOperand1() != null)
				{
					dataprovider = ((BaseQueryColumn)compareCondition.getOperand1()).getName();
				}
				if (dataprovider != null)
				{
					Object recordValue = record.getValue(dataprovider);
					Object conditionValue = compareCondition.getOperand2();
					if (operator == IBaseSQLCondition.BETWEEN_OPERATOR)
					{
						return conditionResult(IBaseSQLCondition.GTE_OPERATOR, recordValue, ((Object[])conditionValue)[0]) &&
							conditionResult(IBaseSQLCondition.LTE_OPERATOR, recordValue, ((Object[])conditionValue)[1]);
					}
					return conditionResult(operator, recordValue, conditionValue);
				}
				return false;
			}
		}
		return true;
	}

	private static boolean conditionResult(int operator, Object recordValue, Object conditionValue)
	{
		if ((operator & IBaseSQLCondition.CASEINSENTITIVE_MODIFIER) == IBaseSQLCondition.CASEINSENTITIVE_MODIFIER && recordValue instanceof String &&
			conditionValue instanceof String)
		{
			recordValue = recordValue.toString().toUpperCase();
			conditionValue = conditionValue.toString().toUpperCase();
		}
		operator = operator & IBaseSQLCondition.OPERATOR_MASK;
		if (operator == IBaseSQLCondition.EQUALS_OPERATOR)
		{
			return equalObjects(recordValue, conditionValue);
		}
		else if (operator == IBaseSQLCondition.NOT_OPERATOR)
		{
			return !equalObjects(recordValue, conditionValue);
		}
		else if (operator == IBaseSQLCondition.GT_OPERATOR)
		{
			if (recordValue instanceof Number && conditionValue instanceof Number)
			{
				return ((Number)recordValue).doubleValue() > ((Number)conditionValue).doubleValue();
			}
			else if (recordValue instanceof Date && conditionValue instanceof Date)
			{
				return ((Date)recordValue).getTime() > ((Date)conditionValue).getTime();
			}
			else if (recordValue != null)
			{
				return recordValue.toString().compareTo((String)conditionValue) > 0;
			}
		}
		else if (operator == IBaseSQLCondition.LT_OPERATOR)
		{
			if (recordValue instanceof Number && conditionValue instanceof Number)
			{
				return ((Number)recordValue).doubleValue() < ((Number)conditionValue).doubleValue();
			}
			else if (recordValue instanceof Date && conditionValue instanceof Date)
			{
				return ((Date)recordValue).getTime() < ((Date)conditionValue).getTime();
			}
			else if (recordValue != null)
			{
				return recordValue.toString().compareTo((String)conditionValue) < 0;
			}
		}
		else if (operator == IBaseSQLCondition.LTE_OPERATOR)
		{
			if (recordValue instanceof Number && conditionValue instanceof Number)
			{
				return ((Number)recordValue).doubleValue() <= ((Number)conditionValue).doubleValue();
			}
			else if (recordValue instanceof Date && conditionValue instanceof Date)
			{
				return ((Date)recordValue).getTime() <= ((Date)conditionValue).getTime();
			}
			else if (recordValue != null)
			{
				return recordValue.toString().compareTo((String)conditionValue) <= 0;
			}
		}
		else if (operator == IBaseSQLCondition.GTE_OPERATOR)
		{
			if (recordValue instanceof Number && conditionValue instanceof Number)
			{
				return ((Number)recordValue).doubleValue() >= ((Number)conditionValue).doubleValue();
			}
			else if (recordValue instanceof Date && conditionValue instanceof Date)
			{
				return ((Date)recordValue).getTime() >= ((Date)conditionValue).getTime();
			}
			else if (recordValue != null)
			{
				return recordValue.toString().compareTo((String)conditionValue) >= 0;
			}
		}
		else if (operator == IBaseSQLCondition.LIKE_OPERATOR)
		{
			if (recordValue != null)
			{
				String searchString = recordValue.toString();
				String pattern = conditionValue.toString();
				if (pattern.startsWith("%") && pattern.endsWith("%"))
				{
					return searchString.contains(pattern.substring(1, pattern.length() - 1).replace("\\", ""));
				}
				else if (pattern.endsWith("%"))
				{
					return searchString.startsWith(pattern.substring(0, pattern.length() - 1).replace("\\", ""));
				}
				else if (pattern.startsWith("%"))
				{
					return searchString.endsWith(pattern.substring(1, pattern.length()).replace("\\", ""));
				}
			}
		}
		return false;
	}
}
