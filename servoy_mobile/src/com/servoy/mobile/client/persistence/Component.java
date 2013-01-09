package com.servoy.mobile.client.persistence;

import com.servoy.j2db.persistence.constants.IContentSpecConstantsBase;
import com.servoy.j2db.persistence.constants.IRepositoryConstants;


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
public class Component extends AbstractBase
{
	protected Component()
	{
	}

	public final native int getTypeID() /*-{
		return this.typeid;
	}-*/;

	public final native String getGroupID() /*-{
		return this.groupID ? this.groupID : null;
	}-*/;

	public final native void setGroupID(String id) /*-{
		this.groupID = id;
	}-*/;

	public final GraphicalComponent isGraphicalComponent()
	{
		return getTypeID() == IRepositoryConstants.GRAPHICALCOMPONENTS ? (GraphicalComponent)this.cast() : null;
	}

	public final Field isField()
	{
		return getTypeID() == IRepositoryConstants.FIELDS ? (Field)this.cast() : null;
	}

	public final TabPanel isTabPanel()
	{
		return getTypeID() == IRepositoryConstants.TABPANELS ? (TabPanel)this.cast() : null;
	}

	public final Portal isPortal()
	{
		return getTypeID() == IRepositoryConstants.PORTALS ? (Portal)this.cast() : null;
	}

	public final String getSize()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_SIZE, null);
	}

	public final void setSize(int w, int h)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_SIZE, w + "," + h); //$NON-NLS-1$
	}

	public final String getLocation()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_LOCATION, null);
	}

	public final void setLocation(int x, int y)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_LOCATION, x + "," + y); //$NON-NLS-1$
	}

	public final boolean isVisible()
	{
		return getAttributeValueBoolean(IContentSpecConstantsBase.PROPERTY_VISIBLE, true);
	}

	public final void setVisible(boolean visible)
	{
		setAttributeValueBoolean(IContentSpecConstantsBase.PROPERTY_VISIBLE, visible);
	}

	public final boolean isEnabled()
	{
		return getAttributeValueBoolean(IContentSpecConstantsBase.PROPERTY_ENABLED, true);
	}

	public final void setEnabled(boolean enabled)
	{
		setAttributeValueBoolean(IContentSpecConstantsBase.PROPERTY_ENABLED, enabled);
	}

	public final void setStyleClass(String styleClass)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_STYLECLASS, styleClass);
	}

	public final String getStyleClass()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_STYLECLASS, null);
	}

}
