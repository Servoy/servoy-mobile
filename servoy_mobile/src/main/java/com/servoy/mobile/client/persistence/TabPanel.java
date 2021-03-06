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

import com.google.gwt.core.client.JsArray;
import com.servoy.base.persistence.constants.IRepositoryConstants;

/**
 * @author gboros
 */
public class TabPanel extends Component
{
	public static final int ORIENTATION_TOP = 1;
	public static final int ORIENTATION_BOTTOM = 3;

	protected TabPanel()
	{
	}

	public final static TabPanel castIfPossible(AbstractBase ab)
	{
		return ab.getTypeID() == IRepositoryConstants.TABPANELS ? (TabPanel)ab.cast() : null;
	}

	public final native int getTabOrientation() /*-{
		return this.tabOrientation;
	}-*/;

	public final native JsArray<Tab> getTabs() /*-{
		return this.items;
	}-*/;

	public final native Tab createTab(String name, String text, String formUUID) /*-{
		var nt = {};
		nt.name = name;
		nt.text = text;
		nt.containsFormID = formUUID;
		if (!this.items)
			this.items = [];
		this.items.push(nt);
		return nt;
	}-*/;
}
