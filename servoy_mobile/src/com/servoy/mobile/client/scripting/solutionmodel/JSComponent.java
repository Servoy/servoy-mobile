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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.base.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Portal;
import com.servoy.mobile.client.persistence.TabPanel;
import com.servoy.mobile.client.ui.PositionComparator;

/**
 * @author acostescu
 */
@Export
public class JSComponent extends JSBase implements IBaseSMComponent, Exportable
{
	protected final String formName;

	public JSComponent(Component c, JSSolutionModel model, JSBase parent)
	{
		super(c, model, parent);
		JSBase p = parent;
		while (p != null)
		{
			if (p instanceof JSForm) break;
			p = p.getParent();
		}
		if (p instanceof JSForm) formName = ((JSForm)p).getName();
		else formName = null; // should never happen
	}

	protected static JSComponent getJSComponent(Component component, JSSolutionModel model, JSBase parent)
	{
		GraphicalComponent graphicalComponent = GraphicalComponent.castIfPossible(component);
		if (graphicalComponent != null)
		{
			if (graphicalComponent.isButton())
			{
				return new JSButton(graphicalComponent, model, parent);
			}
			return new JSLabel(graphicalComponent, model, parent);
		}

		Field field = Field.castIfPossible(component);
		if (field != null)
		{
			return new JSField(field, model, parent);
		}

		Portal portal = Portal.castIfPossible(component);
		if (portal != null)
		{
			return new JSPortal(portal, model, (JSForm)parent);
		}

		TabPanel tabPanel = TabPanel.castIfPossible(component);
		if (tabPanel != null)
		{
			return new JSTabPanel(tabPanel, model, (JSForm)parent);
		}

		return null;
	}

	@Override
	@Getter
	public int getX()
	{
		int[] location = PositionComparator.splitIntegers(((Component)getBase()).getLocation());
		if (location != null && location.length == 2)
		{
			return location[0];
		}
		return 0;
	}

	@Override
	@Getter
	public int getY()
	{
		int[] location = PositionComparator.splitIntegers(((Component)getBase()).getLocation());
		if (location != null && location.length == 2)
		{
			return location[1];
		}
		return 0;
	}

	@Override
	@Getter
	public String getName()
	{
		return getBase().getName();
	}

	@Override
	@Getter
	public boolean getEnabled()
	{
		return ((Component)getBase()).isEnabled();
	}

	@Override
	@Getter
	public boolean getVisible()
	{
		return ((Component)getBase()).isVisible();
	}

	@Override
	@Getter
	public int getWidth()
	{
		int[] size = PositionComparator.splitIntegers(((Component)getBase()).getSize());
		if (size != null && size.length == 2)
		{
			return size[0];
		}
		return 0;
	}

	@Override
	@Getter
	public int getHeight()
	{
		int[] size = PositionComparator.splitIntegers(((Component)getBase()).getSize());
		if (size != null && size.length == 2)
		{
			return size[1];
		}
		return 0;
	}

	@Override
	@Getter
	public String getGroupID()
	{
		return ((Component)getBase()).getGroupID();
	}

	@Override
	@Getter
	public String getStyleClass()
	{
		return ((Component)getBase()).getStyleClass();
	}

	@Override
	@Setter
	public void setX(int x)
	{
		cloneIfNeeded();
		((Component)getBase()).setLocation(x, getY());
	}

	@Override
	@Setter
	public void setY(int y)
	{
		cloneIfNeeded();
		((Component)getBase()).setLocation(getX(), y);
	}

	@Override
	@Setter
	public void setName(String arg)
	{
		cloneIfNeeded();
		getBase().setName(arg);
	}

	@Override
	@Setter
	public void setEnabled(boolean arg)
	{
		cloneIfNeeded();
		((Component)getBase()).setEnabled(arg);
	}

	@Override
	@Setter
	public void setVisible(boolean arg)
	{
		cloneIfNeeded();
		((Component)getBase()).setVisible(arg);
	}

	@Override
	@Setter
	public void setWidth(int width)
	{
		cloneIfNeeded();
		((Component)getBase()).setSize(width, getHeight());
	}

	@Override
	@Setter
	public void setHeight(int height)
	{
		cloneIfNeeded();
		((Component)getBase()).setSize(getWidth(), height);
	}

	@Override
	@Setter
	public void setGroupID(String id)
	{
		cloneIfNeeded();
		((Component)getBase()).setGroupID(id);
	}

	@Override
	@Setter
	public void setStyleClass(String arg)
	{
		cloneIfNeeded();
		((Component)getBase()).setStyleClass(arg);
	}

}
