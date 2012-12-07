package com.servoy.mobile.client.persistence;

import com.servoy.j2db.persistence.constants.IContentSpecConstantsBase;
import com.servoy.j2db.persistence.constants.IFieldConstants;

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
	protected Field()
	{
	}

	public final native String getActionMethodID() /*-{
		return this.onActionMethodID;
	}-*/;

	public final native String getDataChangeMethodID() /*-{
		return this.onDataChangeMethodID;
	}-*/;

	public final native String getText() /*-{
		return this.text;
	}-*/;

	public final native String getValuelistID() /*-{
		return this.valuelistID;
	}-*/;

	public final void setDataProviderID(String dataprovider)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATAPROVIDERID, dataprovider);
	}

	public final String getDataProviderID()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_DATAPROVIDERID, null);
	}

	public final void setDisplayType(int type)
	{
		setAttributeValueInt(IContentSpecConstantsBase.PROPERTY_DISPLAYTYPE, type);
	}

	public final int getDisplayType()
	{
		return getAttributeValueInt(IContentSpecConstantsBase.PROPERTY_DISPLAYTYPE, IFieldConstants.TEXT_FIELD);
	}

}
