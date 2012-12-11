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
import org.timepedia.exporter.client.ExporterUtil;

import com.servoy.j2db.scripting.BaseSolutionHelper;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMButton;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMLabel;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.AbstractBase.MobilePropertiesWrapper;
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

	@Override
	public void markLeftHeaderButton(IBaseSMButton button)
	{
		AbstractBase persist = ((JSButton)ExporterUtil.gwtInstance(button)).getBase();

		MobilePropertiesWrapper mpc = persist.getOrCreateMobilePropertiesCopy();
		mpc.get().setHeaderLeftButton();
		persist.setMobileProperties(mpc);
	}

	public void markLeftHeaderButton(JSButton button)
	{
		markLeftHeaderButton((IBaseSMButton)button);
	}

	@Override
	public void markRightHeaderButton(IBaseSMButton button)
	{
		AbstractBase persist = ((JSBase)button).getBase();

		MobilePropertiesWrapper mpc = persist.getOrCreateMobilePropertiesCopy();
		mpc.get().setHeaderRightButton();
		persist.setMobileProperties(mpc);
	}

	public void markRightHeaderButton(JSButton button)
	{
		markRightHeaderButton((IBaseSMButton)button);
	}

	@Override
	public void markHeaderText(IBaseSMLabel label)
	{
		AbstractBase persist = ((JSBase)label).getBase();

		MobilePropertiesWrapper mpc = persist.getOrCreateMobilePropertiesCopy();
		mpc.get().setHeaderText();
		persist.setMobileProperties(mpc);
	}

	public void markHeaderText(JSLabel button)
	{
		markHeaderText((IBaseSMLabel)button);
	}

	@Override
	public void markFooterItem(IBaseSMComponent component)
	{
		AbstractBase persist = ((JSBase)component).getBase();

		MobilePropertiesWrapper mpc = persist.getOrCreateMobilePropertiesCopy();
		mpc.get().setFooterItem();
		persist.setMobileProperties(mpc);
	}

	public void markFooterItem(JSComponent button)
	{
		markFooterItem((IBaseSMComponent)button);
	}

	@Override
	public void groupComponents(IBaseSMComponent c1, IBaseSMComponent c2)
	{
		super.groupComponents(c1, c2);
	}

	public void groupComponents(JSComponent c1, JSComponent c2)
	{
		groupComponents((IBaseSMComponent)c1, (IBaseSMComponent)c2);
	}

	@Override
	protected String createNewGroupId()
	{
		return Utils.createStringUUID();
	}

	@Override
	public void setIconType(IBaseSMButton button, String iconType)
	{
		AbstractBase persist = ((JSBase)button).getBase();

		MobilePropertiesWrapper mpc = persist.getOrCreateMobilePropertiesCopy();
		mpc.get().setDataIcon(iconType);
		persist.setMobileProperties(mpc);
	}

	public void setIconType(JSButton button, String iconType)
	{
		setIconType((IBaseSMButton)button, iconType);
	}

}