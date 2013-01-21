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

import java.util.Arrays;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.scripting.solutionhelper.BaseSolutionHelper;
import com.servoy.base.scripting.solutionhelper.IBaseSHInsetList;
import com.servoy.base.scripting.solutionhelper.IBaseSHList;
import com.servoy.base.solutionmodel.IBaseSMForm;
import com.servoy.base.solutionmodel.IBaseSMPortal;
import com.servoy.base.solutionmodel.IBaseSolutionModel;
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


	@Override
	public JSButton getLeftHeaderButton(IBaseSMForm form)
	{
		return getLeftHeaderButton((JSForm)form);
	}

	public JSButton getLeftHeaderButton(JSForm form)
	{
		return (JSButton)super.getLeftHeaderButton(form);
	}

	@Override
	public JSButton getRightHeaderButton(IBaseSMForm form)
	{
		return getRightHeaderButton((JSForm)form);
	}

	public JSButton getRightHeaderButton(JSForm form)
	{
		return (JSButton)super.getRightHeaderButton(form);
	}

	public void markHeaderText(JSLabel button)
	{
		super.markHeaderText(button);
	}

	@Override
	public JSLabel getHeaderLabel(IBaseSMForm form)
	{
		return getHeaderLabel((JSForm)form);
	}

	public JSLabel getHeaderLabel(JSForm form)
	{
		return (JSLabel)super.getHeaderLabel(form);
	}

	@Override
	public JSComponent[] getAllFooterComponents(IBaseSMForm form)
	{
		return getAllFooterComponents((JSForm)form);
	}

	public JSComponent[] getAllFooterComponents(JSForm form)
	{
		return Arrays.asList(super.getAllFooterComponents(form)).toArray(new JSComponent[0]);
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

	public void setHeaderSize(JSLabel label, int headerSize)
	{
		super.setHeaderSize(label, headerSize);
	}

	public int getHeaderSize(JSLabel label)
	{
		return super.getHeaderSize(label);
	}

	public JSInsetList createInsetList(JSForm form, int yLocation, String relationName, String headerText, String textDataProviderID)
	{
		return (JSInsetList)super.createInsetList(form, yLocation, relationName, headerText, textDataProviderID);
	}

	@Override
	public JSList createListForm(String formName, String dataSource, String textDataProviderID)
	{
		return (JSList)super.createListForm(formName, dataSource, textDataProviderID);
	}

	public JSInsetList getInsetList(JSForm form, String name)
	{
		return (JSInsetList)super.getInsetList(form, name);
	}


	@Override
	public JSInsetList[] getAllInsetLists(IBaseSMForm form)
	{
		return getAllInsetLists((JSForm)form);
	}

	public JSInsetList[] getAllInsetLists(JSForm form)
	{
		return Arrays.asList(super.getAllInsetLists(form)).toArray(new JSInsetList[0]);
	}

	public boolean removeInsetList(JSForm form, String name)
	{
		return super.removeInsetList(form, name);
	}

	@Override
	public JSList getListForm(String formName)
	{
		return (JSList)super.getListForm(formName);
	}

	@Override
	public JSList[] getAllListForms()
	{
		return Arrays.asList(super.getAllListForms()).toArray(new JSList[0]);
	}

	@Override
	protected IBaseSHInsetList instantiateInsetList(IBaseSMPortal portal, BaseSolutionHelper baseSolutionHelper)
	{
		return new JSInsetList(portal, baseSolutionHelper);
	}

	@Override
	protected IBaseSHList instantiateList(IBaseSMForm listForm, BaseSolutionHelper baseSolutionHelper)
	{
		return new JSList(listForm, baseSolutionHelper);
	}

}