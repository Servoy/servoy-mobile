package com.servoy.mobile.client.scripting;

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

import java.util.ArrayList;
import java.util.Date;

import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.util.Utils;


public abstract class Scope
{

	public static Object getValueAsRightType(Object value, int type, String format)
	{
		if (value == null) return null;
		if (type == IColumnTypeConstants.INTEGER && !(value instanceof Number))
		{
			return Integer.valueOf((int)Utils.getAsDouble(value));
		}
		else if (type == IColumnTypeConstants.NUMBER && !(value instanceof Number))
		{
			return Double.valueOf(Utils.getAsDouble(value));
		}
		else if (type == IColumnTypeConstants.DATETIME && value instanceof String && format != null)
		{
			DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(format);
			return dateTimeFormat.parse(value.toString());
		}
		else if (type == IColumnTypeConstants.TEXT && !(value instanceof String))
		{
			return value.toString();
		}
		return value;
	}


	private final ArrayList<IModificationListener> modificationListeners = new ArrayList<IModificationListener>();

	public Scope()
	{
		super();
	}

	public JsDate getVariableDateValue(String variable)
	{
		Object value = getValue(variable);
		if (value instanceof Date) return JsDate.create(((Date)value).getTime());
		return null;
	}

	public void setVariableDateValue(String variable, JsDate value)
	{
		if (value != null)
		{
			setValue(variable, new Date((long)value.getTime()));
		}
		else
		{
			setValue(variable, value);
		}
	}

	public boolean getVariableBooleanValue(String variable)
	{
		Object value = getValue(variable);
		if (value instanceof Boolean) return ((Boolean)value).booleanValue();
		return false;
	}

	public void setVariableBooleanValue(String variable, boolean value)
	{
		setValue(variable, Boolean.valueOf(value));
	}

	public void setVariableNumberValue(String variable, double value)
	{
		setValue(variable, Double.valueOf(value));
	}

	public double getVariableNumberValue(String variable)
	{
		Object value = getValue(variable);
		if (value instanceof Number) return ((Number)value).doubleValue();
		return Double.NaN;
	}

	public Object getVariableValue(String variable)
	{
		Object value = getValue(variable);
		if (value instanceof FoundSet)
		{
			return ((FoundSet)value).getJavaScriptInstance();
		}
		else if (value instanceof Exportable)
		{
			return ExporterUtil.wrap(value);
		}
		return value;
	}

	public void setVariableValue(String variable, Object value)
	{
		setValue(variable, value);
	}

	protected native void exportProperty(String name) /*-{
		$wnd._ServoyUtils_.defineVariable(this, name);
	}-*/;

	public native void exportProperty(Object javascriptObject, String name) /*-{
		$wnd._ServoyUtils_.defineRedirectVariable(this, javascriptObject, name);
	}-*/;

	public abstract void setVariableType(String variable, int type);

	public abstract int getVariableType(String variable);

	public abstract Object getValue(String variable);

	public abstract void setValue(String variable, Object value);

	public void addModificationListener(IModificationListener listener)
	{
		modificationListeners.add(listener);
	}

	public void removeModificationListener(IModificationListener listener)
	{
		modificationListeners.remove(listener);
	}

	public void fireModificationEvent(String name, Object value)
	{
		for (IModificationListener listener : modificationListeners)
			listener.valueChanged(new ModificationEvent(name, value, this));
	}
}
