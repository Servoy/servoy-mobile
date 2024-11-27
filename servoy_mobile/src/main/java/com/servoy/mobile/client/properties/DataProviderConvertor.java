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

import java.util.Date;

import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.FormView;
import com.servoy.mobile.client.angular.JsDate;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Any;

/**
 * @author jcomp
 *
 */
public class DataProviderConvertor implements IPropertyConverter
{

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller,
		Record record)
	{
		Object returnValue = value;
		if (value instanceof Date)
		{
			JsPlainObj conversionData = new JsPlainObj();
			conversionData.set(FormView.VALUE_KEY, JsDate.create(((Date)value).getTime()).toISOString());
			conversionData.set(FormView.CONVERSION_CL_SIDE_TYPE_KEY, "svy_date");

			returnValue = conversionData;
		}
		return returnValue;
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType, FormController controller)
	{
		if (value == null) return null;
		Any jsonProperty = component.getJSONProperty(key);
		if (jsonProperty != null)
		{

			String dataprovider = jsonProperty.asString();
			if (dataprovider != null && dataprovider.trim().length() > 0)
			{
				int dataproviderType = IColumnTypeConstants.TEXT;
				String[] variableScope = GlobalScope.getVariableScope(dataprovider);
				if (variableScope[0] == null)
				{
					dataproviderType = controller.getFormScope().getVariableType(dataprovider);
				}
				else
				{
					dataproviderType = controller.getApplication().getScriptEngine().getGlobalScope(variableScope[0]).getVariableType(variableScope[1]);
				}

				if (dataproviderType == IColumnTypeConstants.DATETIME)
				{
					return new Date((long)JsDate.create(value.toString()).getTime());
				}
			}
		}
		return value;
	}

}
