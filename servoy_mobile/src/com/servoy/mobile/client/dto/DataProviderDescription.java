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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Description class to hold info about a dataprovider (column mostly)
 * @author jblok
 */
public class DataProviderDescription extends JavaScriptObject
{
	// column flags
	public static final int NORMAL_COLUMN = 0;
	public static final int PK_COLUMN = 1;
	public static final int USER_ROWID_COLUMN = 2;
	public static final int UUID_COLUMN = 4;
	public static final int EXCLUDED_COLUMN = 8;

	protected DataProviderDescription() {}

	public final native String getName() /*-{
		return this.name;
	}-*/;

	private final native void setName(String n) /*-{
		this.name = n;
	}-*/;

	public final native int getFlags() /*-{
		return this.flags;
	}-*/;
	
	public final native int getType() /*-{
		return this.type;
	}-*/;

	private final native void setType(int t) /*-{
		this.type = t;
	}-*/;

	public final native int getLength() /*-{
		return this.dplength;
	}-*/;

	public final boolean isPK() 
	{
		return ((getFlags() & PK_COLUMN) == PK_COLUMN);
	}

	public final boolean isRowIdent() 
	{
		return ((getFlags() & USER_ROWID_COLUMN) == USER_ROWID_COLUMN);
	}
	
	public final boolean isUUID() 
	{
		return ((getFlags() & UUID_COLUMN) == UUID_COLUMN);
	}
	
	public static DataProviderDescription newInstance(String name, int t)
	{
		DataProviderDescription dpd = JavaScriptObject.createObject().cast();
		dpd.setName(name);
		dpd.setType(t);
		return dpd;
	}
}
