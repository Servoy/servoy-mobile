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
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.JsArrayHelper;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class ValuelistConvertor implements IPropertyConverter
{
	private final MobileClient application;

	public ValuelistConvertor(MobileClient application)
	{
		this.application = application;
	}

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent compdonent, PropertySpec propertyType)
	{
		if (value == null) return null;

		ValueList vl = application.getFlattenedSolution().getValueListByUUID(value.toString());
		if (vl == null) return null;

		JsPropertyMap<Object> map = JsPropertyMap.of();
		map.set("hasRealValues", Boolean.valueOf(vl.hasRealValues()));

		JsArrayString display = vl.getDiplayValues(application.getI18nProvider());
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
					entry.set("realValue", display.get(i));
				}
				values.push(entry);
			}
		}
		map.set("values", values);

		return map;
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		if (component == null) return null;
		if (value == null) return getStoredUUID(key, component);

		FormController controller = component.getController();
		JsPropertyMap<Object> request = Js.uncheckedCast(value);
		if (request.has("filter"))
		{
			String filter = Js.cast(request.get("filter"));
			int id = (int)Js.asDouble(request.get("id"));

			String uuid = getStoredUUID(key, component);
			if (uuid != null)
			{
				ValueList vl = controller.getApplication().getFlattenedSolution().getValueListByUUID(uuid);
				if (vl != null)
				{
					JsPlainObj response = buildFilteredResponse(vl, filter, id, controller);
					JsPlainObj componentData = new JsPlainObj();
					componentData.set(key, response);
					JsPlainObj formData = new JsPlainObj();
					formData.set(component.getName(), componentData);
					controller.getView().sendComponentData(formData);
				}
			}
			return uuid;
		}

		return getStoredUUID(key, component);
	}

	private String getStoredUUID(String key, WebRuntimeComponent component)
	{
		Object current = component.getProperty(key);
		if (current instanceof String)
		{
			return (String)current;
		}
		Object jsonProp = component.getJSONProperty(key);
		return jsonProp != null ? jsonProp.toString() : null;
	}

	private JsPlainObj buildFilteredResponse(ValueList vl, String filter, int id, FormController controller)
	{
		JsArrayString display = vl.getDiplayValues(controller.getApplication().getI18nProvider());
		JsArrayMixed real = vl.getRealValues();

		Array<Object> values = JsArrayHelper.createArray();
		if (display != null)
		{
			String lowerFilter = filter != null ? filter.toLowerCase() : "";
			for (int i = 0; i < display.length(); i++)
			{
				String displayVal = display.get(i);
				if (lowerFilter.isEmpty() || (displayVal != null && displayVal.toLowerCase().startsWith(lowerFilter)))
				{
					JsPlainObj entry = new JsPlainObj();
					entry.set("displayValue", displayVal);
					if (real != null && i < real.length())
					{
						entry.set("realValue", getRealValue(real, i));
					}
					else
					{
						entry.set("realValue", displayVal);
					}
					values.push(entry);
				}
			}
		}

		JsPlainObj response = new JsPlainObj();
		response.set("hasRealValues", vl.hasRealValues());
		response.set("values", values);

		JsPlainObj handledID = new JsPlainObj();
		handledID.set("id", id);
		handledID.set("value", true);
		response.set("handledID", handledID);

		return response;
	}

	private static native Object getRealValue(JsArrayMixed real, int index) /*-{
		return real[index];
	}-*/;

}
