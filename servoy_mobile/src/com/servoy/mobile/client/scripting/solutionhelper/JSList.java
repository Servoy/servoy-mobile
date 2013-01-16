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
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.base.scripting.solutionhelper.BaseSHList;
import com.servoy.base.scripting.solutionhelper.BaseSolutionHelper;
import com.servoy.base.scripting.solutionhelper.IBaseSHFormList;
import com.servoy.base.solutionmodel.IBaseSMForm;
import com.servoy.mobile.client.scripting.solutionmodel.JSForm;
import com.servoy.mobile.client.scripting.solutionmodel.JSMethod;

/**
 * @author acostescu
 */
@Export
public class JSList extends BaseSHList implements IBaseSHFormList, Exportable
{

	protected JSList(IBaseSMForm listForm, BaseSolutionHelper baseSolutionHelper)
	{
		super(listForm, baseSolutionHelper);
	}

	public JSForm getForm()
	{
		return (JSForm)super.getContainer();
	}

	@Override
	@Getter
	public String getCountDataProviderID()
	{
		return super.getCountDataProviderID();
	}

	@Override
	@Setter
	public void setCountDataProviderID(String countDataProviderID)
	{
		super.setCountDataProviderID(countDataProviderID);
	}

	@Override
	@Getter
	public String getText()
	{
		return super.getText();
	}

	@Override
	@Setter
	public void setText(String text)
	{
		super.setText(text);
	}

	@Override
	@Getter
	public String getTextDataProviderID()
	{
		return super.getTextDataProviderID();
	}

	@Override
	@Setter
	public void setTextDataProviderID(String textDataPRoviderID)
	{
		super.setTextDataProviderID(textDataPRoviderID);
	}

	@Setter
	public void setOnAction(JSMethod method)
	{
		super.setOnAction(method);
	}

	@Override
	@Getter
	public JSMethod getOnAction()
	{
		return (JSMethod)super.getOnAction();
	}

	@Override
	@Getter
	public String getSubtext()
	{
		return super.getSubtext();
	}

	@Override
	@Setter
	public void setSubtext(String subtext)
	{
		super.setSubtext(subtext);
	}

	@Override
	@Getter
	public String getSubtextDataProviderID()
	{
		return super.getSubtext();
	}

	@Override
	@Setter
	public void setSubtextDataProviderID(String subtextDataProviderID)
	{
		super.setSubtextDataProviderID(subtextDataProviderID);
	}

	@Override
	@Getter
	public String getDataIconType()
	{
		return super.getDataIconType();
	}

	@Override
	@Setter
	public void setDataIconType(String iconType)
	{
		super.setDataIconType(iconType);
	}

	@Override
	@Getter
	public String getDataIconDataProviderID()
	{
		return super.getDataIconDataProviderID();
	}

	@Override
	@Setter
	public void setDataIconDataProviderID(String dataIconDataProviderID)
	{
		super.setDataIconDataProviderID(dataIconDataProviderID);
	}

}
