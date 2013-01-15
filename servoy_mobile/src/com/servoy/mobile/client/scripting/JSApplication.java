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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;
import org.timepedia.exporter.client.NoExport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.scripting.api.IJSApplication;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.ValueList;

/**
 * @author jcompagner
 *
 */
@Export
public class JSApplication implements Exportable, IJSApplication
{
	private final MobileClient application;

	public JSApplication(MobileClient application)
	{
		this.application = application;
		GWT.create(JSApplication.class);
		export(ExporterUtil.wrap(this));
	}

	public void output(Object output)
	{
		GWT.log(output == null ? "<null>" : output.toString()); //$NON-NLS-1$
	}

	@Override
	public boolean isInDeveloper()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSApplication#setValueListItems(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void setValueListItems(String name, Object[] displayValues)
	{
		setValueListItems(name, displayValues, (JsArrayMixed)null);

	}

	public void setValueListItems(String name, Object[] displayValues, JsArrayMixed realValues)
	{
		ValueList list = application.getFlattenedSolution().getValueList(name);
		JsArrayString display = JavaScriptObject.createArray().cast();
		if (displayValues != null)
		{
			for (Object object : displayValues)
			{
				display.push(object.toString());
			}
		}

		list.setValues(display, realValues == null ? (JsArrayMixed)JavaScriptObject.createArray().cast() : realValues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSApplication#setValueListItems(java.lang.String, java.lang.Object[], java.lang.Object[])
	 */
	@Override
	@NoExport
	public void setValueListItems(String name, Object[] displayValues, Object[] realValues)
	{
	}

	private native void export(Object object)
	/*-{
		$wnd.application = object;
	}-*/;

}
