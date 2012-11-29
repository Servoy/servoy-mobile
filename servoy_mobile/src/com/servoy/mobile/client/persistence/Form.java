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
import com.servoy.j2db.persistence.constants.IContentSpecConstantsBase;
import com.servoy.j2db.persistence.constants.IRepositoryConstants;
import com.servoy.mobile.client.util.Utils;

/**
 * @author gboros
 */
public class Form extends BaseComponent
{

	protected Form()
	{
	}

	public final GraphicalComponent createNewGraphicalComponent(String viewType)
	{
		Component c = createEmptyChildComponent(Utils.createStringUUID(), IRepositoryConstants.GRAPHICALCOMPONENTS);
		GraphicalComponent gc = c.isGraphicalComponent();
		if (gc != null)
		{
			gc.setViewType(viewType);
		}
		return gc;
	}

	public final Field createNewField(int type)
	{
		Component c = createEmptyChildComponent(Utils.createStringUUID(), IRepositoryConstants.FIELDS);
		Field f = c.isField();
		if (f != null)
		{
			f.setDisplayType(type);
		}
		return f;
	}

	public final native Component createEmptyChildComponent(String uuid, int type) /*-{
		var ei = {};
		if (!this.items)
			this.items = [];
		ei.uuid = uuid;
		ei.typeid = type;
		this.items.push(ei);
		return ei;
	}-*/;

	public final void setDataSource(String dataSource)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATASOURCE, dataSource);
	}

	public final String getDataSource()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATASOURCE, null);
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

	public final native String setSize(String size) /*-{
		this.size = size;
	}-*/;

	public final native String getBackground() /*-{
		return this.background;
	}-*/;

	public final native int getView() /*-{
		return this.view ? this.view : 0;
	}-*/;

	public final native JsArray<Component> getComponents() /*-{
		return this.items;
	}-*/;

}
