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

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.JsArrayHelper;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.JsPropertyMap;

/**
 * Converter for the "valuelist" property type. It looks up the custom value list referenced by its
 * design UUID and emits the JSON envelope the NGClient valuelist type converter expects:
 * <code>{ hasRealValues, values:[{ displayValue, realValue }] }</code>.
 * <p>
 * Only custom value lists are supported; database / foundset-based value lists are out of scope.
 *
 * @author jcompagner
 *
 */
public class ValuelistConvertor implements IPropertyConverter
{

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller, Record record)
	{
		if (value == null) return null;
		ValueList vl = controller.getApplication().getFlattenedSolution().getValueListByUUID(value.toString());
		if (vl == null) return null;

		JsPropertyMap<Object> map = JsPropertyMap.of();
		map.set("hasRealValues", Boolean.valueOf(vl.hasRealValues()));

		// display values with i18n applied (getDiplayValues handles "i18n:" prefixes)
		JsArrayString display = vl.getDiplayValues(controller.getApplication().getI18nProvider());
		JsArrayMixed real = vl.getRealValues();

		Array<Object> values = JsArrayHelper.createArray();
		if (display != null)
		{
			for (int i = 0; i < display.length(); i++)
			{
				JsPlainObj entry = new JsPlainObj();
				entry.set("displayValue", display.get(i));
				if (real != null && i < real.length())
				{
					entry.set("realValue", getRealValue(real, i));
				}
				else
				{
					// no separate real values: mirror the display value so components reading item.realValue still work
					entry.set("realValue", display.get(i));
				}
				values.push(entry);
			}
		}
		map.set("values", values);

		return map;
	}

	private static native Object getRealValue(JsArrayMixed real, int index) /*-{
		return real[index];
	}-*/;

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller)
	{
		return null; // shouldn't be set from the client
	}

}
