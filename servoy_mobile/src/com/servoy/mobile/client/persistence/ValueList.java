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

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.util.I18NProvider;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.ModificationEvent;

/**
 * @author jcompagner
 * @since 7.0
 */
public class ValueList extends AbstractBase
{
	protected ValueList()
	{
	}

	public final JsArrayString getDiplayValues(I18NProvider i18nProvider)
	{
		JsArrayString rawDisplayValues = getRawDiplayValues();
		if (i18nProvider != null)
		{
			for (int i = 0; i < rawDisplayValues.length(); i++)
				rawDisplayValues.set(i, i18nProvider.getI18NMessageIfPrefixed(rawDisplayValues.get(i)));
		}
		return rawDisplayValues;
	}

	public final native JsArrayString getRawDiplayValues() /*-{
		return this.displayValues;
	}-*/;

	public final native JsArrayMixed getRealValues() /*-{
		return this.realValues;
	}-*/;

	public final native boolean hasRealValues() /*-{
		return (this.realValues && this.realValues.length == this.displayValues.length);
	}-*/;

	private final native void setValuesImpl(JsArrayString display, JsArrayMixed real)
	/*-{
		this.displayValues = display;
		this.realValues = real;
	}-*/;

	public final void setValues(JsArrayString display, JsArrayMixed real)
	{
		setValuesImpl(display, real);
		fireChanged();
	}

	public final native void addModificationListener(IModificationListener listener)
	/*-{
		if (!this.listeners)
			this.listeners = new Array();
		this.listeners.push(listener);
	}-*/;

	public final native void removeModificationListener(IModificationListener listener)
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

	public final native IModificationListener getListener(int index)
	/*-{
		if (this.listeners)
			return this.listeners[index];
		return null;
	}-*/;

	private final void fireChanged()
	{
		int count = listenersCount();
		if (count > 0)
		{
			ModificationEvent event = new ModificationEvent(getName(), null, this);
			for (int i = 0; i < count; i++)
			{
				getListener(i).valueChanged(event);
			}
		}
	}
}
