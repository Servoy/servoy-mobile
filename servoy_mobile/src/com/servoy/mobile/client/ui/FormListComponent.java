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

package com.servoy.mobile.client.ui;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.persistence.Portal;
import com.servoy.mobile.client.scripting.RuntimePortal;

/**
 * FormList when used as component (inset list)
 * 
 * @author rgansevles
 *
 */
public class FormListComponent extends FormList implements IPortalComponent
{
	private final RuntimePortal scriptable;

	/**
	 * @param formController
	 * @param formComponents
	 * @param dal
	 * @param relation
	 */
	public FormListComponent(Portal portal, FormController formController, DataAdapterList dal, MobileClient application)
	{
		super(formController, portal.getComponents(), dal, application.getFlattenedSolution().getRelation(portal.getRelationName()));
		this.scriptable = new RuntimePortal(application, formController.getExecutor(), this, portal);
	}

	@Override
	public RuntimePortal getRuntimeComponent()
	{
		return scriptable;
	}
}
