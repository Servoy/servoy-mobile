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

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JsArray;
import com.servoy.base.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.base.scripting.api.solutionmodel.IBaseSMPortal;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Portal;

/**
 * @author rgansevles
 *
 */

@Export
public class JSPortal extends JSComponent implements IBaseSMPortal, Exportable
{

	public JSPortal(Portal portal, JSSolutionModel model, JSForm form)
	{
		super(portal, model, form);
	}

	protected Portal getPortal()
	{
		return (Portal)getBase();
	}

	@Override
	public JSButton newButton(String txt, int x, int y, int width, int height, Object action)
	{
		return newButton(txt, x, y, width, height, null); // because of how GWT Exporter works, the other method will be most likely called instead anyway for JSMethod actions
	}

	public JSButton newButton(String txt, int x, int y, int width, int height, JSMethod action)
	{
		GraphicalComponent gc = GraphicalComponent.createNewGraphicalComponent(getPortal(), GraphicalComponent.VIEW_TYPE_BUTTON);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		if (action != null) gc.setOnActionMethodCall(action.getReferenceString());
		return new JSButton(gc, getSolutionModel(), this);
	}

	@Override
	public JSField newField(Object dataprovider, int type, int x, int y, int width, int height)
	{
		Field f = Field.createNewField(getPortal(), type);
		if (dataprovider instanceof String) f.setDataProviderID((String)dataprovider);
		f.setSize(width, height);
		f.setLocation(x, y);
		return new JSField(f, getSolutionModel(), this);
	}

	@Override
	public JSLabel newLabel(String txt, int x, int y, int width, int height)
	{
		GraphicalComponent gc = GraphicalComponent.createNewGraphicalComponent(getPortal(), GraphicalComponent.VIEW_TYPE_LABEL);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		return new JSLabel(gc, getSolutionModel(), this);
	}

	@Override
	public String getRelationName()
	{
		return getPortal().getRelationName();
	}

	@Override
	public void setRelationName(String arg)
	{
		getPortal().setRelationName(arg);
	}

	@Override
	public IBaseSMComponent[] getComponents()
	{
		JsArray<Component> portalComponents = getPortal().getComponents();
		List<JSComponent> components = new ArrayList<JSComponent>(portalComponents.length());
		for (int i = 0; i < portalComponents.length(); i++)
		{
			JSComponent jsComponent = JSComponent.getJSComponent(portalComponents.get(i), getSolutionModel(), this);
			if (jsComponent != null)
			{
				components.add(jsComponent);
			}
		}
		return components.toArray(new JSComponent[components.size()]);
	}
}