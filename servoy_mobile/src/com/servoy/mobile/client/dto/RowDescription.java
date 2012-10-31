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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

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
	}

	/**
	 * @param dataProviderID
	 */
	public final native double getNumberValue(String dataProviderID)/*-{
		return this[dataProviderID];
	}-*/;


	private final native void setBooleanValue(String dataProviderID, boolean val)/*-{
		this['modification_date'] = new Date().getTime();
		this[dataProviderID] = val;
	}-*/;

	private final native void setNumberValue(String dataProviderID, double val)/*-{
		this['modification_date'] = new Date().getTime();
		this[dataProviderID] = val;
	}-*/;

	private final native void setValueObject(String dataProviderID, Object obj) /*-{
		this['modification_date'] = new Date().getTime();
		this[dataProviderID] = obj;
	}-*/;

	public final native void setCreatedOnDevice(boolean createdOnDevice)/*-{
		this['createdOnDevice'] = createdOnDevice;
	}-*/;

	public final native boolean isCreatedOnDevice()
/*-{
		if (this['createdOnDevice'] == null
				|| this['createdOnDevice'] == undefined)
			return false;
		return this['createdOnDevice'];
	}-*/;


	public final String toJSON()
	{
		return new JSONObject(this).toString();
	}

	public static RowDescription newInstance()
	{
		RowDescription desc = JSONParser.parseStrict("{}").isObject().getJavaScriptObject().cast();
		desc.setCreatedOnDevice(true);
		return desc;
	}
}
