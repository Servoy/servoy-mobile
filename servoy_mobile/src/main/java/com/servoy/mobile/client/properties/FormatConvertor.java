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

import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;
import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
public class FormatConvertor implements IPropertyConverter
{
	@SuppressWarnings("nls")
	@Override
	public JsPropertyMap<Object> convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller,
		Record record)
	{
		final int[] dataproviderType = { IColumnTypeConstants.TEXT };

		JsArrayLike<String> forProperty = propertyType.getFor();

		forProperty.asList().forEach(property -> {
			String dataprovider = component.getJSONProperty(property).asString();
			if (dataprovider != null && dataprovider.trim().length() > 0)
			{
				String[] variableScope = GlobalScope.getVariableScope(dataprovider);
				if (variableScope[0] == null)
				{
					dataproviderType[0] = controller.getFormScope().getVariableType(dataprovider);
				}
				else
				{
					dataproviderType[0] = controller.getApplication().getScriptEngine().getGlobalScope(variableScope[0]).getVariableType(variableScope[1]);
				}
			}
		});
		return FormatParser.parseFormatProperty(Js.asAny(value).asString()).toJsObject(dataproviderType[0]);
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller)
	{
		return null; // shouldn't be set from the client
	}
}
