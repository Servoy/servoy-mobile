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

package com.servoy.mobile.client.scripting.solutionmodel;

import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Portal;
import com.servoy.mobile.client.persistence.TabPanel;

/**
 * @author acostescu
 */
public class JSBase
{
	private final AbstractBase ab;
	private final String formName;
	private final JSSolutionModel model;

	public JSBase(AbstractBase ab, String formName, JSSolutionModel model)
	{
		this.ab = ab;
		this.formName = formName;
		this.model = model;
	}

	@NoExport
	public AbstractBase getBase()
	{
		return ab;
	}

	protected JSSolutionModel getSolutionModel()
	{
		return model;
	}

	protected String getFormName()
	{
		return formName;
	}

	protected JSComponent getJSComponent(Component component)
	{
		GraphicalComponent graphicalComponent = component.isGraphicalComponent();
		if (graphicalComponent != null)
		{
			if (graphicalComponent.isButton())
			{
				return new JSButton(graphicalComponent, getFormName(), getSolutionModel());
			}
			return new JSLabel(graphicalComponent, getFormName(), getSolutionModel());
		}

		Field field = component.isField();
		if (field != null)
		{
			return new JSField(field, getFormName(), getSolutionModel());
		}

		Portal portal = component.isPortal();
		if (portal != null)
		{
			return new JSPortal(portal, getFormName(), getSolutionModel());
		}

		TabPanel tabPanel = component.isTabPanel();
		if (tabPanel != null)
		{
			return new JSTabPanel(tabPanel, getFormName(), getSolutionModel());
		}

		return null;
	}
}
