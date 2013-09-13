/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.mobile.client.persistence;

import com.servoy.base.persistence.constants.IContentSpecConstantsBase;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.mobile.client.util.Utils;

/**
 * @author gboros
 *
 */
public class Bean extends Component
{
	protected Bean()
	{
	}

	public final static Bean createNewBeanComponent(AbstractBase parent)
	{
		return castIfPossible(createEmptyChildComponent(parent, Utils.createStringUUID(), IRepositoryConstants.BEANS));
	}

	public final static Bean castIfPossible(AbstractBase ab)
	{
		return ab.getTypeID() == IRepositoryConstants.BEANS ? (Bean)ab.cast() : null;
	}

	public final void setInnerHTML(String innerHTML)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_BEANXML, innerHTML);
	}

	public final String getInnerHTML()
	{
		return getAttributeValueString(IContentSpecConstantsBase.PROPERTY_BEANXML, null);
	}
}
