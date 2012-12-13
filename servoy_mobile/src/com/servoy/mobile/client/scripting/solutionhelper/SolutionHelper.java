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

package com.servoy.mobile.client.scripting.solutionhelper;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel;
import com.servoy.j2db.scripting.solutionhelper.BaseSolutionHelper;
import com.servoy.j2db.scripting.solutionhelper.IBaseSHInsetList;
import com.servoy.j2db.scripting.solutionhelper.IBaseSHList;
import com.servoy.j2db.scripting.solutionhelper.IMobileProperties;
import com.servoy.mobile.client.scripting.solutionmodel.JSBase;
import com.servoy.mobile.client.scripting.solutionmodel.JSButton;
import com.servoy.mobile.client.scripting.solutionmodel.JSComponent;
import com.servoy.mobile.client.scripting.solutionmodel.JSForm;
import com.servoy.mobile.client.scripting.solutionmodel.JSLabel;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobilePredefinedIconConstants;
import com.servoy.mobile.client.util.Utils;

/**
 * This class must override all methods from BaseSolutionHelper so as to have them exported to JS.
 * @author acostescu
 */
@Export
//@ExportPackage("plugins.mobile") // this doesn't work because plugins.mobile already exists in some way in other exporters
//@ExportPackage("")
public class SolutionHelper extends BaseSolutionHelper implements IMobilePredefinedIconConstants, Exportable
{

	public SolutionHelper(IBaseSolutionModel solutionModel)
	{
		super(solutionModel);
	}

	@Override
	protected IMobileProperties getMobileProperties(Object jsObject)
	{
		if (jsObject instanceof JSBase) return ((JSBase)jsObject).getBase().getOrCreateMobileProperties();
		return null;
	}

	@Override
	protected String getStringUUID(Object jsObject)
	{
		if (jsObject instanceof JSBase) return ((JSBase)jsObject).getBase().getUUID();
		return null;
	}

	// methods like this one are necessary for exporter (with real class instead of interface)
	public void markLeftHeaderButton(JSButton button)
	{
		super.markLeftHeaderButton(button);
	}

	public void markRightHeaderButton(JSButton button)
	{
		super.markRightHeaderButton(button);
	}

	public void markHeaderText(JSLabel button)
	{
		super.markHeaderText(button);
	}

	public void markFooterItem(JSComponent button)
	{
		super.markFooterItem(button);
	}

	public void groupComponents(JSComponent c1, JSComponent c2)
	{
		super.groupComponents(c1, c2);
	}

	@Override
	protected String createNewGroupId()
	{
		return Utils.createStringUUID();
	}

	public void setIconType(JSButton button, String iconType)
	{
		super.setIconType(button, iconType);
	}

	public String getIconType(JSButton button)
	{
		return super.getIconType(button);
	}

	public JSInsetList createInsetList(JSForm form, int yLocation, String dataSource, String relationName, String headerText, String textDataProviderID)
	{
		return (JSInsetList)super.createInsetList(form, yLocation, dataSource, relationName, headerText, textDataProviderID);
	}

	@Override
	public JSList createListForm(String formName, String dataSource, String textDataProviderID)
	{
		return (JSList)super.createListForm(formName, dataSource, textDataProviderID);
	}

	@Override
	protected IBaseSHInsetList instantiateInsetList(IBaseSMForm form, IBaseSMTabPanel tabPanel, IBaseSMForm listForm, BaseSolutionHelper baseSolutionHelper)
	{
		return new JSInsetList(tabPanel, listForm, baseSolutionHelper);
	}

	@Override
	protected IBaseSHList instantiateList(IBaseSMForm listForm, BaseSolutionHelper baseSolutionHelper)
	{
		return new JSList(listForm, baseSolutionHelper);
	}

}