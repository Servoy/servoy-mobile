/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2026 Servoy BV

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

import com.google.gwt.core.client.JsArray;
import com.servoy.base.persistence.constants.IRepositoryConstants;

/**
 * @author jcompagner
 *
 */
public class LayoutContainer extends AbstractBase
{
	public final static LayoutContainer castIfPossible(AbstractBase ab)
	{
		return ab.getTypeID() == IRepositoryConstants.LAYOUTCONTAINERS ? (LayoutContainer)ab.cast() : null;
	}

	protected LayoutContainer()
	{
	}

	/**
	 * Same as {@link AbstractBase#getChildren()}, but already cast to Components.
	 */
	public final native JsArray<Component> getComponents() /*-{
		return this.items ? this.items : [];
	}-*/;

}
