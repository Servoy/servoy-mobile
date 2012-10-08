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

import java.util.Date;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

/**
 * @author jcompagner
 *
 */
@ExportPackage("")
@Export
public class JSEvent implements IMobileJSEvent
{
	public static final String MYTEST = "MYTEST";

	private final Object source;
	private final String type;
	private final Date timestamp;
	private final int y;
	private final int x;
	private final int modifiers;
	private final String formName;
	private final String elementName;

	private Object data;

	public JSEvent(String type, Object source, String formName, String elementName)
	{
		this.type = type;
		this.source = source;
		this.formName = formName;
		this.elementName = elementName;
		this.y = -1;
		this.x = -1;
		this.modifiers = -1;
		this.timestamp = new Date();
	}

	public Object getSource()
	{
		return source;
	}

	public String getType()
	{
		return type;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getFormName()
	 */
	@Override
	public String getFormName()
	{
		return formName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getElementName()
	 */
	@Override
	public String getElementName()
	{
		return elementName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getModifiers()
	 */
	@Override
	public int getModifiers()
	{
		return modifiers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getX()
	 */
	@Override
	public int getX()
	{
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getY()
	 */
	@Override
	public int getY()
	{
		return y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#getData()
	 */
	@Override
	@Getter
	public Object getData()
	{
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSEvent#setData(java.lang.Object)
	 */
	@Override
	@Setter
	public void setData(Object object)
	{
		this.data = object;
	}
}
