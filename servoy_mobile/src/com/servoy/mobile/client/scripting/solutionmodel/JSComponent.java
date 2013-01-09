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

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.ui.PositionComparator;

/**
 * @author acostescu
 */
@Export
public class JSComponent extends JSBase implements IBaseSMComponent, Exportable
{
	public JSComponent(Component c, String formName, JSSolutionModel model)
	{
		super(c, formName, model);
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
		((Component)getBase()).setLocation(x, getY());
	}

	@Override
	@Setter
	public void setY(int y)
	{
		((Component)getBase()).setLocation(getX(), y);

	}

	@Override
	@Setter
	public void setName(String arg)
	{
		getBase().setName(arg);
	}

	@Override
	@Setter
	public void setEnabled(boolean arg)
	{
		((Component)getBase()).setEnabled(arg);
	}

	@Override
	@Setter
	public void setVisible(boolean arg)
	{
		((Component)getBase()).setVisible(arg);
	}

	@Override
	@Setter
	public void setWidth(int width)
	{
		((Component)getBase()).setSize(width, getHeight());
	}

	@Override
	@Setter
	public void setHeight(int height)
	{
		((Component)getBase()).setSize(getWidth(), height);
	}

	@Override
	@Setter
	public void setGroupID(String id)
	{
		((Component)getBase()).setGroupID(id);
	}

	@Override
	@Setter
	public void setStyleClass(String arg)
	{
		((Component)getBase()).setStyleClass(arg);
	}

}
