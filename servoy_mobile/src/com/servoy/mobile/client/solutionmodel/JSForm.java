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

import com.google.gwt.core.client.JsArray;

/**
 * @author gboros
 */
public class JSForm extends JSItem
{
	public static final int VIEW_TYPE_RECORD = 0;
	public static final int VIEW_TYPE_LIST = 1;
	public static final int VIEW_TYPE_TABLE = 2;
	public static final int VIEW_TYPE_TABLE_LOCKED = 3;
	public static final int VIEW_TYPE_LIST_LOCKED = 4;
	public static final int VIEW_TYPE_RECORD_LOCKED = 5;

	protected JSForm()
	{
	}

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native String getUUID() /*-{
		return this.uuid;
	}-*/;

	public final native String getSize() /*-{
		return this.size;
	}-*/;

	public final native String getBackground() /*-{
		return this.background;
	}-*/;

	public final native int getView() /*-{
		return this.view ? this.view : 0;
	}-*/;

	public final native JsArray<JSComponent> getComponents() /*-{
		return this.items;
	}-*/;

	public final native String getDataSource() /*-{
		return this.dataSource;
	}-*/;
}
