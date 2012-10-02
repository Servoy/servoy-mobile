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
import org.timepedia.exporter.client.Exportable;

/**
 * @author jcompagner
 *
 * TODO should this one just extends the JSEvent from our self to have the same constants?
 */
@ExportPackage("")
@Export
public class JSEvent implements Exportable
{
	public static final String ACTION = "onaction";


	private final Object source;
	private final String type;
	private final Date timestamp;

	public JSEvent(String type, Object source)
	{
		this.type = type;
		this.source = source;
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
}
