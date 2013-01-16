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

import com.servoy.base.solutionmodel.IBaseSMForm;
import com.servoy.base.solutionmodel.IBaseSMTab;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Tab;

/**
 * @author acostescu
 */
@Export
public class JSTab extends JSBase implements IBaseSMTab, Exportable
{
	public JSTab(Tab tab, JSSolutionModel model, JSBase parent)
	{
		super(tab, model, parent);
	}

	@Getter
	@Override
	public JSForm getContainsForm()
	{
		Form form = getSolutionModel().getApplication().getFlattenedSolution().getFormByUUID(((Tab)getBase()).getContainsFormID());
		if (form != null)
		{
			return new JSForm(form, getSolutionModel());
		}
		return null;
	}

	@Setter
	@Override
	public void setContainsForm(IBaseSMForm form)
	{
		cloneIfNeeded();
		((Tab)getBase()).setContainsFormID(((Form)((JSForm)form).getBase()).getUUID());
	}

	@Override
	public String getRelationName()
	{
		return ((Tab)getBase()).getRelationName();
	}

	@Override
	public void setRelationName(String arg)
	{
		cloneIfNeeded();
		((Tab)getBase()).setRelationName(arg);

	}
}
