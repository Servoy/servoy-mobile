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

package com.servoy.mobile.client.scripting.solutionmodel;

import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * Abstract form component with text features.
 * 
 * @author rgansevles
 *
 */
abstract class JSTextComponent extends JSComponent
{
	public JSTextComponent(GraphicalComponent gc, JSSolutionModel model, JSBase parent)
	{
		super(gc, model, parent);
	}

	@Getter
	public String getDataProviderID()
	{
		return ((GraphicalComponent)getBase()).getDataProviderID();
	}

	@Setter
	public void setDataProviderID(String arg)
	{
		cloneIfNeeded();
		((GraphicalComponent)getBase()).setDataProviderID(arg);
	}

	@Getter
	public String getText()
	{
		return ((GraphicalComponent)getBase()).getText();
	}

	@Setter
	public void setText(String arg)
	{
		cloneIfNeeded();
		((GraphicalComponent)getBase()).setText(arg);
	}

	@Getter
	public boolean getDisplaysTags()
	{
		return ((GraphicalComponent)getBase()).getDisplaysTags();
	}

	@Setter
	public void setDisplaysTags(boolean arg)
	{
		cloneIfNeeded();
		((GraphicalComponent)getBase()).setDisplaysTags(arg);
	}
}
