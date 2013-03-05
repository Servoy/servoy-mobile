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
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.dataprocessing.IRowChangeListener;
import com.servoy.mobile.client.util.Utils;

/**
 * @author jblok
 */
@SuppressWarnings("nls")
public class RowDescription extends JavaScriptObject
{
	public static final String MODIFICATION_DATE = "modification_date";
	public static final String CREATED_ON_DEVICE = "created_on_device";


	protected RowDescription()
	{
	}

	public final Object getValue(String dataProviderID)
	{
		if (isNumber(dataProviderID))
		{
			return new Double(getNumberValue(dataProviderID));
		}
		return getValueImpl(dataProviderID);

	}

	public final Object getValue(String dataProviderID, int type)
	{
		// we check if really a number because created record will always have string pk
		if (isNumber(dataProviderID))
		{
			switch (type)
			{
				case IColumnTypeConstants.INTEGER :
					return new Integer((int)getNumberValue(dataProviderID));
				case IColumnTypeConstants.NUMBER :
					return new Double(getNumberValue(dataProviderID));
				case IColumnTypeConstants.DATETIME :
					return new Date((long)getNumberValue(dataProviderID));

			}
		}
		return getValueImpl(dataProviderID);

	}

	private final native Object getValueImpl(String dataProviderID)
	/*-{
		return this[dataProviderID];
	}-*/;

	private final native boolean isNumber(String dataProviderID)
	/*-{
		var value = this[dataProviderID];
		return typeof (value) == 'number'
	}-*/;

	public final void setValueInternal(String dataProviderID, Object obj)
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

	public final void setValue(String dataProviderID, Object obj)
	{
		Object oldObj = getValue(dataProviderID);
		setValueInternal(dataProviderID, obj);
		setModificationDate();//flag as changed
		if (!Utils.equalObjects(oldObj, obj)) fireChanged(dataProviderID, obj);
	}

	public final native void addRowChangeListener(IRowChangeListener listener)
	/*-{
		if (!this.listeners)
			this.listeners = new Array();
		this.listeners.push(listener);
	}-*/;

	public final native void removeRowChangeListener(IRowChangeListener listener)
	/*-{
		if (this.listeners) {
			var index = this.listeners.indexOf(listener);
			this.listeners.splice(index, 1);
		}
	}-*/;

	public final native int listenersCount()
	/*-{
		if (this.listeners)
			return this.listeners.length;
		return 0;
	}-*/;

	public final native IRowChangeListener getListener(int index)
	/*-{
		if (this.listeners)
			return this.listeners[index];
		return null;
	}-*/;

	private final void fireChanged(String dataProviderID, Object obj)
	{
		int count = listenersCount();
		if (count > 0)
		{
			for (int i = 0; i < count; i++)
			{
				getListener(i).notifyChange(dataProviderID, obj);
			}
		}
	}

	private final native void setModificationDate()/*-{
		this[@com.servoy.mobile.client.dto.RowDescription::MODIFICATION_DATE] = new Date()
				.getTime();
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

	/**
	 * For creation from storage values
	 * @param dataProviders
	 * @param values
	 * @return the object instance
	 */
	public static RowDescription newInstance(String[] dataProviders, JSONArray values)
	{
		RowDescription retval = JavaScriptObject.createObject().cast();
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

	public final native void setCreatedOnDevice(boolean createdOnDevice)/*-{
		this[@com.servoy.mobile.client.dto.RowDescription::CREATED_ON_DEVICE] = createdOnDevice;
	}-*/;

	public final native boolean isCreatedOnDevice() /*-{
		if (this[@com.servoy.mobile.client.dto.RowDescription::CREATED_ON_DEVICE] == null
				|| this[@com.servoy.mobile.client.dto.RowDescription::CREATED_ON_DEVICE] == undefined)
			return false;
		return this[@com.servoy.mobile.client.dto.RowDescription::CREATED_ON_DEVICE];
	}-*/;

	/**
	 * For creation for brand new records
	 * @return the object instance
	 */
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
