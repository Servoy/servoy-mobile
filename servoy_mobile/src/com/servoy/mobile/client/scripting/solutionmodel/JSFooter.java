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

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.NoExport;
import org.timepedia.exporter.client.Setter;

import com.google.gwt.core.client.JsArray;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IFieldConstants;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.base.solutionmodel.IBaseSMComponent;
import com.servoy.base.solutionmodel.IBaseSMMethod;
import com.servoy.base.solutionmodel.IBaseSMVariable;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMFooter;

/**
 * Footer part in solution model.
 * 
 * @author rgansevles
 *
 */
@Export
@ExportPackage("")
public class JSFooter extends JSPart implements IMobileSMFooter, Exportable
{
	JSFooter(Part part, JSSolutionModel model, JSForm parent)
	{
		super(part, model, parent);
	}

	@Override
	public JSForm getParent()
	{
		return (JSForm)super.getParent();
	}

	@Override
	@Getter
	public boolean getSticky()
	{
		return getBase().getPartType() == IPartConstants.TITLE_FOOTER;
	}

	@Override
	@Setter
	public void setSticky(boolean sticky)
	{
		cloneIfNeeded();
		getBase().setPartType(sticky ? TITLE_FOOTER : FOOTER);
	}

	@Override
	public JSField newField(IBaseSMVariable dataprovider, int type, int x)
	{
		return newField((JSVariable)dataprovider, type, x);
	}

	public JSField newField(JSVariable dataprovider, int type, int x)
	{
		return newField(dataprovider.getReferenceString(), type, x);
	}

	@Override
	public JSField newField(String dataprovidername, int type, int x)
	{
		return markForFooter(getParent().newField(dataprovidername, type, x, 0, 10, 10));
	}

	@Override
	public JSText newTextField(IBaseSMVariable dataprovider, int x)
	{
		return newTextField((JSVariable)dataprovider, x);
	}

	public JSText newTextField(JSVariable dataprovider, int x)
	{
		return newTextField(dataprovider.getReferenceString(), x);
	}

	public JSText newTextField(String dataprovidername, int x)
	{
		return (JSText)newField(dataprovidername, IFieldConstants.TEXT_FIELD, x);
	}

	@Override
	public JSTextArea newTextArea(IBaseSMVariable dataprovider, int x)
	{
		return newTextArea((JSVariable)dataprovider, x);
	}

	public JSTextArea newTextArea(JSVariable dataprovider, int x)
	{
		return newTextArea(dataprovider.getReferenceString(), x);
	}

	public JSTextArea newTextArea(String dataprovidername, int x)
	{
		return (JSTextArea)newField(dataprovidername, IFieldConstants.TEXT_AREA, x);
	}

	@Override
	public JSCombobox newCombobox(IBaseSMVariable dataprovider, int x)
	{
		return newCombobox((JSVariable)dataprovider, x);
	}

	public JSCombobox newCombobox(JSVariable dataprovider, int x)
	{
		return newCombobox(dataprovider.getReferenceString(), x);
	}

	public JSCombobox newCombobox(String dataprovidername, int x)
	{
		return (JSCombobox)newField(dataprovidername, IFieldConstants.COMBOBOX, x);
	}

	@Override
	public JSRadios newRadios(IBaseSMVariable dataprovider, int x)
	{
		return newRadios((JSVariable)dataprovider, x);
	}

	public JSRadios newRadios(JSVariable dataprovider, int x)
	{
		return newRadios(dataprovider.getReferenceString(), x);
	}

	public JSRadios newRadios(String dataprovidername, int x)
	{
		return (JSRadios)newField(dataprovidername, IFieldConstants.RADIOS, x);
	}

	@Override
	public JSChecks newCheck(IBaseSMVariable dataprovider, int x)
	{
		return newCheck((JSVariable)dataprovider, x);
	}

	public JSChecks newCheck(JSVariable dataprovider, int x)
	{
		return newCheck(dataprovider.getReferenceString(), x);
	}

	public JSChecks newCheck(String dataprovidername, int x)
	{
		return (JSChecks)newField(dataprovidername, IFieldConstants.CHECKS, x);
	}

	@Override
	public JSCalendar newCalendar(IBaseSMVariable dataprovider, int x)
	{
		return newCalendar((JSVariable)dataprovider, x);
	}

	public JSCalendar newCalendar(JSVariable dataprovider, int x)
	{
		return newCalendar(dataprovider.getReferenceString(), x);
	}

	public JSCalendar newCalendar(String dataprovidername, int x)
	{
		return (JSCalendar)newField(dataprovidername, IFieldConstants.CALENDAR, x);
	}


	@Override
	public JSPassword newPassword(IBaseSMVariable dataprovider, int x)
	{
		return newPassword((JSVariable)dataprovider, x);
	}

	public JSPassword newPassword(JSVariable dataprovider, int x)
	{
		return newPassword(dataprovider.getReferenceString(), x);
	}

	public JSPassword newPassword(String dataprovidername, int x)
	{
		return (JSPassword)newField(dataprovidername, IFieldConstants.PASSWORD, x);
	}

	@Override
	public JSButton newButton(String txt, int x, IBaseSMMethod method)
	{
		return newButton(txt, x, (JSMethod)method);
	}

	public JSButton newButton(String txt, int x, JSMethod method)
	{
		return markForFooter(getParent().newButton(txt, x, 0, 10, 10, method));
	}

	@Override
	public JSLabel newLabel(String txt, int x)
	{
		return markForFooter(getParent().newLabel(txt, x, 0, 10, 10));
	}

	@Override
	public boolean removeComponent(String name)
	{
		if (name != null)
		{
			getParent().cloneIfNeeded();
			JsArray<Component> formComponents = getParent().getBase().getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				if (name.equals(component.getName()))
				{
					MobileProperties mp = component.getMobileProperties();
					if (mp != null && Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.FOOTER_ITEM)))
					{
						getParent().getBase().removeChild(i);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public IBaseSMComponent[] getComponents()
	{
		List<JSComponent> footerComponents = new ArrayList<JSComponent>();
		for (JSComponent comp : getParent().getComponents())
		{
			MobileProperties mp = comp.getBase().getMobileProperties();
			if (mp != null && Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.FOOTER_ITEM)))
			{
				footerComponents.add(comp);
			}
		}
		return footerComponents.toArray(new IBaseSMComponent[footerComponents.size()]);
	}

	@NoExport
	private <T extends JSBase> T markForFooter(T comp)
	{
		comp.putMobileProperty(IMobileProperties.FOOTER_ITEM, Boolean.TRUE);
		return comp;
	}

}
