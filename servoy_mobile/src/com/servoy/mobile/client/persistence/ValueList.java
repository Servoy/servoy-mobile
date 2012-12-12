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

package com.servoy.mobile.client.persistence;

import java.util.ArrayList;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.ModificationEvent;

/**
 * @author jcompagner
 * @since 7.0
 */
public class ValueList extends AbstractBase
{
	private final ArrayList<IModificationListener> listeners = new ArrayList<IModificationListener>();

	protected ValueList()
	{
	}

	public final native String getUUID() /*-{
		return this.uuid;
	}-*/;

	public final native JsArrayString getDiplayValues() /*-{
		return this.displayValues;
	}-*/;

	public final native JsArrayMixed getRealValues() /*-{
		return this.realValues;
	}-*/;

	public final native boolean hasRealValues() /*-{
		return (this.realValues && this.realValues.length == this.displayValues.length);
	}-*/;

	private native void setValuesImpl(JsArrayString display, JsArrayMixed real)
	/*-{
		this.displayValues = display;
		this.realValues = real;
	}-*/;

	public final void setValues(JsArrayString display, JsArrayMixed real)
	{
		setValuesImpl(display, real);
		fireChanged();
	}

	public void addModificationListener(IModificationListener listener)
	{
		listeners.add(listener);
	}

	public void removeModificationListener(IModificationListener listener)
	{
		listeners.remove(listener);
	}

	private void fireChanged()
	{
		if (listeners.size() > 0)
		{
			ModificationEvent event = new ModificationEvent(getName(), null, this);
			for (IModificationListener listener : listeners)
			{
				listener.valueChanged(event);
			}
		}
	}
}
