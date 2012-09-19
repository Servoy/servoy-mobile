package com.servoy.mobile.client.persistence;

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
public class Field extends Component
{
	public static final int DISPLAY_TYPE_TEXT_FIELD = 0;
	public static final int DISPLAY_TYPE_TEXT_AREA = 1;
	public static final int DISPLAY_TYPE_COMBOBOX = 2;
	public static final int DISPLAY_TYPE_RADIOS = 3;
	public static final int DISPLAY_TYPE_CHECKS = 4;
	public static final int DISPLAY_TYPE_CALENDAR = 5;
	public static final int DISPLAY_TYPE_PASSWORD = 6;
	public static final int DISPLAY_TYPE_LIST_BOX = 11;
	public static final int DISPLAY_TYPE_MULTISELECT_LISTBOX = 12;
	
	protected Field() {}
	
	public final native String getDataProviderID() /*-{ return this.dataProviderID;	}-*/;
	public final native String getSize() /*-{ return this.size;	}-*/;
	public final native String getLocation() /*-{ return this.location;	}-*/;
	public final native int getDisplayType() /*-{ return this.displayType?this.displayType:0;	}-*/;
	public final native String getGroupID() /*-{ return this.groupID;	}-*/;
}
