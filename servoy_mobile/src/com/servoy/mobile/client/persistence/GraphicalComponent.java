package com.servoy.mobile.client.persistence;

import com.servoy.j2db.persistence.constants.IContentSpecConstantsBase;

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
public class GraphicalComponent extends Component
{

	public static final String VIEW_TYPE_BUTTON = "button"; //$NON-NLS-1$
	public static final String VIEW_TYPE_ATTR = "viewType"; //$NON-NLS-1$

	protected GraphicalComponent()
	{
	}

	public final void setViewType(String viewType)
	{
		setAttributeValueString(VIEW_TYPE_ATTR, viewType);
	}

	public final String getViewType()
	{
		return getAttributeValueString(VIEW_TYPE_ATTR);
	}

	public final void setOnActionMethodID(String id)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONACTIONMETHODID, id);
	}

	public final String getOnActionMethodID()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONACTIONMETHODID);
	}

	public final native String getDataProviderID() /*-{
		return this.dataProviderID;
	}-*/;

	public final void setText(String text)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_TEXT, text);
	}

	public final String getText()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_TEXT);
	}

	public final native String getActionMethodID() /*-{
		return this.onActionMethodID;
	}-*/;

	public final native String getGroupID() /*-{
		return this.groupID;
	}-*/;

	public final native boolean isDisplaysTags() /*-{
		return this.displaysTags ? this.displaysTags : false;
	}-*/;

	public final boolean isButton()
	{
		return VIEW_TYPE_BUTTON.equals(getViewType());
	}
}
