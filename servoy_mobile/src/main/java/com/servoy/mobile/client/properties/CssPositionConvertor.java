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

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;
import com.servoy.mobile.client.util.Utils;

import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
@SuppressWarnings("nls")
public class CssPositionConvertor implements IPropertyConverter
{

	@Override
	public JsPropertyMap<Object> convertJS(Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller,
		Record record)
	{
		if (value == null) return null;

		JsPlainObj object = Js.cast(value);
		String top = object.getStr("top");
		String bottom = object.getStr("bottom;");


		JsPropertyMap<Object> jsValue = JsPropertyMap.of();
		jsValue.set("position", "absolute");
		if (isSet(top)) jsValue.set("top", addPixels(top));
		if (isSet(object.getStr("left"))) jsValue.set("left", addPixels(object.getStr("left")));
		if (isSet(bottom)) jsValue.set("bottom", addPixels(bottom));
		if (isSet(object.getStr("right"))) jsValue.set("right", addPixels(object.getStr("right")));
		if (isSet(object.getStr("height")))
		{
			if (isSet(top) && isSet(object.getStr("bottom")))
			{
				jsValue.set("min-height", addPixels(object.getStr("height")));
			}
			else jsValue.set("height", addPixels(object.getStr("height")));
		}
		if (isSet(object.getStr("width")))
		{
			if (isSet(object.getStr("left")) && isSet(object.getStr("right")))
			{
				jsValue.set("min-width", addPixels(object.getStr("width")));
			}
			else jsValue.set("width", addPixels(object.getStr("width")));
		}
		return jsValue;
	}

	private boolean isSet(String value)
	{
		return value != null && !value.equals("-1") && !value.trim().isEmpty();
	}

	private String addPixels(String value)
	{
		if (Utils.getAsInteger(value, -1) != -1) return value + "px";
		return value;
	}
}
