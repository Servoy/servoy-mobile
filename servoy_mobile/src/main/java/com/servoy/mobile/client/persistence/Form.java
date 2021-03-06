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
import com.servoy.base.persistence.constants.IContentSpecConstantsBase;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.mobile.client.util.Utils;

/**
 * @author gboros
 */
public class Form extends AbstractBase
{
	protected Form()
	{
	}

	public final Form cloneDeep()
	{
		Form f = (Form)Utils.cloneDeep(this).cast();
		f.markAsCopy();
		return f;
	}

	/**
	 * Same as {@link AbstractBase#getChildren()}, but already cast to Components.
	 */
	public final native JsArray<Component> getComponents() /*-{
		return this.items ? this.items : [];
	}-*/;

	public final TabPanel createNewTabPanel()
	{
		return TabPanel.castIfPossible(Component.createEmptyChildComponent(this, Utils.createStringUUID(), IRepositoryConstants.TABPANELS));
	}

	public final Portal createNewPortal(String name)
	{
		Portal portal = Portal.castIfPossible(Component.createEmptyChildComponent(this, Utils.createStringUUID(), IRepositoryConstants.PORTALS));
		portal.setName(name);
		return portal;
	}

	public final Part createNewPart(int partType)
	{
		Part part = Part.castIfPossible(Component.createEmptyChildComponent(this, Utils.createStringUUID(), IRepositoryConstants.PARTS));
		part.setPartType(partType);
		return part;
	}

	public final void setDataSource(String dataSource)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATASOURCE, dataSource);
	}

	public final String getDataSource()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATASOURCE, null);
	}

	public final void setView(int viewType)
	{
		setAttributeValueInt(IContentSpecConstantsBase.PROPERTY_VIEW, viewType);
	}

	public final int getView()
	{
		return getAttributeValueInt(IContentSpecConstantsBase.PROPERTY_VIEW, 0);
	}

	public final native String getSize() /*-{
		return this.size;
	}-*/;

	public final native String setSize(String size) /*-{
		this.size = size;
	}-*/;

	public final native String getBackground() /*-{
		return this.background;
	}-*/;

	public final String getOnShowCall()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONSHOWMETHODID, null);
	}

	public final void setOnShowCall(String onShowCall)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONSHOWMETHODID, onShowCall);
	}

	public final String getOnLoadCall()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONLOADMETHODID, null);
	}

	public final void setOnLoadCall(String onLoadCall)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONLOADMETHODID, onLoadCall);
	}

	public final String getOnHideCall()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONHIDEMETHODID, null);
	}

	public final void setOnHideCall(String onHideCall)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONHIDEMETHODID, onHideCall);
	}

	public final String getOnRecordSelectionCall()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONRECORDSELECTIONMETHODID, null);
	}

	public final void setOnRecordSelectionCall(String onRecordSelectionCall)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_ONRECORDSELECTIONMETHODID, onRecordSelectionCall);
	}

	public final String getNavigatorID()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_NAVIGATORID, null);
	}

	public final void setNavigatorID(String navigatorID)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_NAVIGATORID, navigatorID);
	}
}
