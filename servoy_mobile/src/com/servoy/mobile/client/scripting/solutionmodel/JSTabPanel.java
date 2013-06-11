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
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JsArray;
import com.servoy.base.solutionmodel.IBaseSMForm;
import com.servoy.base.solutionmodel.IBaseSMTabPanel;
import com.servoy.mobile.client.persistence.Tab;
import com.servoy.mobile.client.persistence.TabPanel;

/**
 * @author acostescu
 *
 */

@Export
@ExportPackage("")
public class JSTabPanel extends JSComponent implements IBaseSMTabPanel, Exportable
{

	public JSTabPanel(TabPanel tabPanel, JSSolutionModel model, JSBase form)
	{
		super(tabPanel, model, form);
	}

	@Override
	public JSTab newTab(String name, String text, IBaseSMForm form)
	{
		cloneIfNeeded();
		Tab tab = ((TabPanel)getBase()).createTab(name, text, ((JSForm)form).getBase().getUUID());
		return new JSTab(tab, getSolutionModel(), this);
	}

	@Override
	public JSTab[] getTabs()
	{
		List<JSTab> tabs = new ArrayList<JSTab>();
		JsArray<Tab> tabsArray = ((TabPanel)getBase()).getTabs();
		for (int i = 0; i < tabsArray.length(); i++)
		{
			tabs.add(new JSTab(tabsArray.get(i), getSolutionModel(), this));
		}
		return tabs.toArray(new JSTab[0]);
	}
}
