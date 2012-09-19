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

package com.servoy.mobile.client.request;

/**
 * @author lvostinar
 *
 */
final class StringValidator
{
	/**
	 * Returns true if the string is empty or null.
	 * 
	 * @param string to test if null or empty
	 * 
	 * @return true if the string is empty or null
	 */
	public static boolean isEmptyOrNullString(String string)
	{
		return (string == null) || (0 == string.trim().length());
	}

	/**
	 * Throws if <code>value</code> is <code>null</code> or empty. This method
	 * ignores leading and trailing whitespace.
	 * 
	 * @param name the name of the value, used in error messages
	 * @param value the string value that needs to be validated
	 * 
	 * @throws IllegalArgumentException if the string is empty, or all whitespace
	 * @throws NullPointerException if the string is <code>null</code>
	 */
	public static void throwIfEmptyOrNull(String name, String value)
	{
		assert (name != null);
		assert (name.trim().length() != 0);

		throwIfNull(name, value);

		if (0 == value.trim().length())
		{
			throw new IllegalArgumentException(name + " cannot be empty");
		}
	}

	/**
	 * Throws a {@link NullPointerException} if the value is <code>null</code>.
	 * 
	 * @param name the name of the value, used in error messages
	 * @param value the value that needs to be validated
	 * 
	 * @throws NullPointerException if the value is <code>null</code>
	 */
	public static void throwIfNull(String name, Object value)
	{
		if (null == value)
		{
			throw new NullPointerException(name + " cannot be null");
		}
	}

	private StringValidator()
	{
	}
}