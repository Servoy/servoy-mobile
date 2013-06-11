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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.NoExport;
import org.timepedia.exporter.client.Setter;

import com.google.gwt.core.client.JsArray;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.IMobileProperties.MobileProperty;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.base.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMHeader;

/**
 * header part in solution model.
 * 
 * @author rgansevles
 *
 */
@Export
@ExportPackage("")
public class JSHeader extends JSPart implements IMobileSMHeader, Exportable
{
	JSHeader(Part part, JSSolutionModel model, JSForm parent)
	{
		super(part, model, parent);
	}

	@NoExport
	@Override
	public JSForm getParent()
	{
		return (JSForm)super.getParent();
	}

	@Override
	@Getter
	public boolean getSticky()
	{
		return getBase().getPartType() == IPartConstants.TITLE_HEADER;
	}

	@Override
	@Setter
	public void setSticky(boolean sticky)
	{
		cloneIfNeeded();
		getBase().setPartType(sticky ? TITLE_HEADER : HEADER);
	}

	@Override
	public JSButton newLeftButton(String txt, IBaseSMMethod method)
	{
		return newLeftButton(txt, (JSMethod)method);
	}

	public JSButton newLeftButton(String txt, JSMethod method)
	{
		return newButtonImpl(true, txt, method);
	}

	@Override
	public JSButton getLeftButton()
	{
		return getButtonImpl(true);
	}

	@Override
	public boolean removeLeftButton()
	{
		return removeComponent(IMobileProperties.HEADER_LEFT_BUTTON);
	}

	@Override
	public JSButton newRightButton(String txt, IBaseSMMethod method)
	{
		return newRightButton(txt, (JSMethod)method);
	}

	public JSButton newRightButton(String txt, JSMethod method)
	{
		return newButtonImpl(false, txt, method);
	}

	@Override
	public JSButton getRightButton()
	{
		return getButtonImpl(false);
	}

	@Override
	public boolean removeRightButton()
	{
		return removeComponent(IMobileProperties.HEADER_RIGHT_BUTTON);
	}

	@Override
	public JSTitle newHeaderText(String txt)
	{
		JSLabel label = getParent().newLabel(txt, 0, 0, 10, 10);
		label.putCustomProperty(IMobileProperties.HEADER_ITEM, Boolean.TRUE);
		label.putCustomProperty(IMobileProperties.HEADER_TEXT, Boolean.TRUE);
		return new JSTitle((GraphicalComponent)label.getBase(), getSolutionModel(), getParent());
	}

	@Override
	public JSTitle getHeaderText()
	{
		Component headerText = getParent().selectComponent(IMobileProperties.HEADER_TEXT);
		if (headerText instanceof GraphicalComponent)
		{
			return new JSTitle((GraphicalComponent)headerText, getSolutionModel(), getParent());
		}
		return null;
	}

	@Override
	public boolean removeHeaderText()
	{
		return removeComponent(IMobileProperties.HEADER_TEXT);
	}

	@NoExport
	private JSButton newButtonImpl(boolean left, String txt, JSMethod method)
	{
		JSButton button = getParent().newButton(txt, 0, 0, 10, 10, method);
		button.putCustomProperty(IMobileProperties.HEADER_ITEM, Boolean.TRUE);
		button.putCustomProperty(left ? IMobileProperties.HEADER_LEFT_BUTTON : IMobileProperties.HEADER_RIGHT_BUTTON, Boolean.TRUE);
		return button;
	}

	@NoExport
	private JSButton getButtonImpl(boolean left)
	{
		for (JSButton button : getParent().getButtons())
		{
			MobileProperties mp = button.getBase().getMobileProperties();
			if (mp != null && Boolean.TRUE.equals(mp.getPropertyValue(left ? IMobileProperties.HEADER_LEFT_BUTTON : IMobileProperties.HEADER_RIGHT_BUTTON)))
			{
				return button;
			}
		}
		return null;
	}

	@NoExport
	private boolean removeComponent(MobileProperty<Boolean> property)
	{
		getParent().cloneIfNeeded();
		Form form = getParent().getBase();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			MobileProperties mp = component.getMobileProperties();
			if (mp != null && Boolean.TRUE.equals(mp.getPropertyValue(property)))
			{
				form.removeChild(i);
				return true;
			}
		}
		return false;
	}
}
