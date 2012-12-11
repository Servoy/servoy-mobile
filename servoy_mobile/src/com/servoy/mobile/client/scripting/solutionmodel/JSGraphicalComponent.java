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

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.ScriptEngine;

/**
 * @author acostescu
 */
@Export
public class JSGraphicalComponent extends JSComponent implements IBaseSMGraphicalComponent, Exportable
{

	public JSGraphicalComponent(GraphicalComponent gc, String formName, JSSolutionModel model)
	{
		super(gc, formName, model);
	}

	@Override
	@Getter
	public String getDataProviderID()
	{
		return ((GraphicalComponent)getBase()).getDataProviderID();
	}

	@Override
	@Getter
	public boolean getDisplaysTags()
	{
		return ((GraphicalComponent)getBase()).isDisplaysTags();
	}

	@Override
	@Getter
	public String getText()
	{
		return ((GraphicalComponent)getBase()).getText();
	}

	@Override
	@Setter
	public void setDataProviderID(String arg)
	{
		((GraphicalComponent)getBase()).setDataProviderID(arg);

	}

	@Override
	@Setter
	public void setDisplaysTags(boolean arg)
	{
		((GraphicalComponent)getBase()).setDisplayTags(arg);

	}

	@Override
	@Setter
	public void setText(String arg)
	{
		((GraphicalComponent)getBase()).setText(arg);

	}

	@Override
	@Setter
	public void setOnAction(IBaseSMMethod method)
	{
		((GraphicalComponent)getBase()).setOnActionMethodCall(((JSMethod)method).getReferenceString());
	}

	public void setOnAction(JSMethod method)
	{
		// workaround for classcast exception in javascript
		setOnAction((IBaseSMMethod)method);
	}

	@Override
	@Getter
	public JSMethod getOnAction()
	{
		String methodCall = ((GraphicalComponent)getBase()).getOnActionMethodCall();
		if (methodCall != null && methodCall.contains("("))
		{
			methodCall = methodCall.substring(0, methodCall.indexOf("(")).trim();
			String[] callParts = methodCall.split("\\.");
			if (callParts.length == 1)
			{
				return new JSMethod(ScriptEngine.FORMS, formName, callParts[0], getSolutionModel());
			}
			else
			{
				return new JSMethod(ScriptEngine.SCOPES, callParts[callParts.length - 2], callParts[callParts.length - 1], getSolutionModel());
			}
		}
		return null;
	}

}
