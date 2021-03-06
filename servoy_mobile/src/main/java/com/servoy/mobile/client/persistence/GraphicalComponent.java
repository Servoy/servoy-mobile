package com.servoy.mobile.client.persistence;

import com.servoy.base.persistence.IBaseGraphicalComponentCommon;
import com.servoy.base.persistence.constants.IComponentConstants;
import com.servoy.base.persistence.constants.IContentSpecConstantsBase;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.mobile.client.util.Utils;

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
public class GraphicalComponent extends Component implements IBaseGraphicalComponentCommon
{
	protected GraphicalComponent()
	{
	}

	public final static GraphicalComponent createNewGraphicalComponent(AbstractBase parent, String viewType)
	{
		GraphicalComponent gc = castIfPossible(createEmptyChildComponent(parent, Utils.createStringUUID(), IRepositoryConstants.GRAPHICALCOMPONENTS));
		gc.setViewType(viewType);
		gc.setStyleClass("c");
		return gc;
	}

	public final static GraphicalComponent castIfPossible(AbstractBase ab)
	{
		return ab.getTypeID() == IRepositoryConstants.GRAPHICALCOMPONENTS ? (GraphicalComponent)ab.cast() : null;
	}

	public final void setViewType(String viewType)
	{
		setAttributeValueString(IComponentConstants.VIEW_TYPE_ATTR, viewType);
	}

	public final String getViewType()
	{
		return getAttributeValueString(IComponentConstants.VIEW_TYPE_ATTR, null);
	}

	public final void setOnActionMethodCall(String methodCall)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONACTIONMETHODID, methodCall);
	}

	public final String getOnActionMethodCall()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONACTIONMETHODID, null);
	}

	public final native String getDataProviderID() /*-{
		return this.dataProviderID;
	}-*/;

	public final void setDataProviderID(String dataProviderID)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATAPROVIDERID, dataProviderID);
	}

	public final void setText(String text)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_TEXT, text);
	}

	public final void setDisplaysTags(boolean displayTags)
	{
		setAttributeValueBoolean(IContentSpecConstantsBase.PROPERTY_DISPLAY_TAGS, displayTags);
	}

	public final String getText()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_TEXT, null);
	}

	public final native boolean getDisplaysTags() /*-{
		return this.displaysTags ? this.displaysTags : false;
	}-*/;

	public final boolean isButton()
	{
		return IComponentConstants.VIEW_TYPE_BUTTON.equals(getViewType());
	}

	public final void setFormat(String format)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_FORMAT, format);
	}

	public final String getFormat()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_FORMAT, null);
	}
}
