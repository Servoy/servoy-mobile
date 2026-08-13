/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2024 Servoy BV

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

package com.servoy.mobile.client.properties;

import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.JsArrayHelper;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Js;

/**
 * Mobile-client equivalent of sablo's CustomVariableArgsType.
 *
 * Converts the collapsed varargs JS array produced by
 * {@link com.servoy.mobile.client.ui.ApiSpec#processVarArgsIfNeeded} into the
 * {@code {"vEr":1,"v":[...]}} envelope that the Angular client-side
 * {@code json_array_converter} expects — exactly matching what the server's
 * {@code CustomVariableArgsType.toJSON()} emits.
 *
 * An optional {@code elementConverter} handles per-element type conversion for
 * varargs whose element type needs conversion (e.g. {@code "svy_date..."}).
 *
 * @see org.sablo.specification.property.CustomVariableArgsType
 */
@SuppressWarnings("nls")
public class VarArgsConvertor implements IPropertyConverter
{
	/** Wire key for the content version — matches CustomJSONArrayType.CONTENT_VERSION. */
	public static final String CONTENT_VERSION = "vEr";

	/** Wire key for the value array — matches CustomJSONArrayType.VALUE. */
	public static final String VALUE = "v";

	/**
	 * Version sent for a fresh (non-incremental) full array value.
	 * The server always uses 1 for a new varargs call; the client accepts any positive value.
	 */
	private static final int INITIAL_CONTENT_VERSION = 1;

	private final IPropertyConverter elementConverter;

	/**
	 * @param elementConverter converter for each individual element, or {@code null} for
	 *                         primitive element types (String, Number, Boolean) that need
	 *                         no conversion.
	 */
	public VarArgsConvertor(IPropertyConverter elementConverter)
	{
		this.elementConverter = elementConverter;
	}

	/**
	 * Converts a raw {@code Array<Object>} (the varargs sub-array produced by
	 * {@link com.servoy.mobile.client.ui.ApiSpec#processVarArgsIfNeeded}) into the
	 * wire envelope {@code {"vEr":1,"v":[element0, element1, ...]}} expected by the
	 * Angular {@code json_array_converter}, applying the element converter to each entry.
	 */
	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		Array<Object> rawArray = Js.uncheckedCast(value);

		Array<Object> convertedElements = JsArrayHelper.createArray();
		if (rawArray != null)
		{
			for (int i = 0; i < rawArray.getLength(); i++)
			{
				Object element = rawArray.getAt(i);
				if (elementConverter != null)
				{
					element = elementConverter.convertForClient(element, component, propertyType);
				}
				convertedElements.push(element);
			}
		}

		JsPlainObj envelope = new JsPlainObj();
		envelope.set(CONTENT_VERSION, INITIAL_CONTENT_VERSION);
		envelope.set(VALUE, convertedElements);
		return envelope;
	}

	/**
	 * Unwraps a {@code {"vEr":N,"v":[...]}} envelope received from Angular back into a
	 * raw {@code Array<Object>}, applying the element converter in the fromClient direction
	 * to each entry.
	 */
	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		if (value == null) return null;

		JsPlainObj envelope = Js.uncheckedCast(value);
		Array<Object> rawArray = envelope.getObj(VALUE);
		if (rawArray == null || elementConverter == null) return rawArray;

		Array<Object> convertedElements = JsArrayHelper.createArray();
		for (int i = 0; i < rawArray.getLength(); i++)
		{
			convertedElements.push(elementConverter.convertFromClient(key, rawArray.getAt(i), component, propertyType));
		}
		return convertedElements;
	}
}
