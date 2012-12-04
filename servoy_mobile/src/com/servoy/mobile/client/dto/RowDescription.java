package com.servoy.mobile.client.dto;

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

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * @author jblok
 */
public class RowDescription extends JavaScriptObject
{
	protected RowDescription()
	{
	}

	public final native Object getValue(String dataProviderID) /*-{
		var value = this[dataProviderID];
		if (typeof (value) == 'number') {
			//do manual boxing
			return new Number(value);
		}
		return value;
	}-*/;

	public final void setValue(String dataProviderID, Object obj)
	{
		if (obj instanceof Number)
		{
			setNumberValue(dataProviderID, ((Number)obj).doubleValue());
		}
		else if (obj instanceof Boolean)
		{
			setBooleanValue(dataProviderID, ((Boolean)obj).booleanValue());
		}
		else if (obj instanceof Date)
		{
			setNumberValue(dataProviderID, ((Date)obj).getTime());
		}
		else
		{
			setValueObject(dataProviderID, obj);
		}

		setModificationDate();
	}

	private final native void setModificationDate()/*-{
		this['modification_date'] = new Date().getTime();
	}-*/;

	private final native void setBooleanValue(String dataProviderID, boolean val)/*-{
		this[dataProviderID] = val;
	}-*/;

	private final native void setNumberValue(String dataProviderID, double val)/*-{
		this[dataProviderID] = val;
	}-*/;

	private final native void setValueObject(String dataProviderID, Object obj) /*-{
		this[dataProviderID] = obj;
	}-*/;

	public final native double getNumberValue(String dataProviderID)/*-{
		return this[dataProviderID];
	}-*/;

	public final String toJSONObject()
	{
		return new JSONObject(this).toString();
	}

	public final String toJSONArray(String[] dataProviders)
	{
		JSONObject jsthis = new JSONObject(this);

		JSONArray retval = new JSONArray();
		for (int i = 0; i < dataProviders.length; i++)
		{
			retval.set(i, jsthis.get(dataProviders[i]));
		}
		return retval.toString();
	}

	public static RowDescription newInstance(String[] dataProviders, JSONArray values)
	{
		RowDescription retval = newInstance();
		for (int i = 0; i < dataProviders.length; i++)
		{
			JSONValue val = values.get(i);
			if (val.isBoolean() != null)
			{
				retval.setBooleanValue(dataProviders[i], val.isBoolean().booleanValue());
			}
			else if (val.isNumber() != null)
			{
				retval.setNumberValue(dataProviders[i], val.isNumber().doubleValue());
			}
			else if (val.isString() != null)
			{
				retval.setValueObject(dataProviders[i], val.isString().stringValue());
			}
		}
		return retval;
	}

	private final native void setCreatedOnDevice(boolean createdOnDevice)/*-{
		this['created_on_device'] = createdOnDevice;
	}-*/;

	public final native boolean isCreatedOnDevice()
	/*-{
		if (this['created_on_device'] == null
				|| this['createdOnDevice'] == undefined)
			return false;
		return this['created_on_device'];
	}-*/;

	public static RowDescription newInstance()
	{
		RowDescription rd = JavaScriptObject.createObject().cast();
		rd.setCreatedOnDevice(true);
		return rd;
	}

	public final RowDescription cloneRowDescription()
	{
		return JSONParser.parseStrict(toJSONObject()).isObject().getJavaScriptObject().cast();
	}
}
