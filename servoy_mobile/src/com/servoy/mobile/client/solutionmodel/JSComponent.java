package com.servoy.mobile.client.solutionmodel;

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


/**
 * @author gboros
 */
public class JSComponent extends JSItem
{
	private static final int TYPE_ID_GRAPHICALCOMPONENT = 7;
	private static final int TYPE_ID_FIELD = 4;
	private static final int TYPE_ID_TABPANEL = 16;
	
	protected JSComponent() {}

	public final native int getTypeID() /*-{ return this.typeid;	}-*/;
	public final native String getUUID() /*-{ return this.uuid; }-*/;

	public final JSGraphicalComponent isGraphicalComponent()
	{
		return getTypeID() == TYPE_ID_GRAPHICALCOMPONENT ? (JSGraphicalComponent)this.cast() : null;
	}
	
	public final JSField isField()
	{
		return getTypeID() == TYPE_ID_FIELD ? (JSField)this.cast() : null;
	}
	
	public final JSTabPanel isTabPanel()
	{
		return getTypeID() == TYPE_ID_TABPANEL ? (JSTabPanel)this.cast() : null;
	}
}
