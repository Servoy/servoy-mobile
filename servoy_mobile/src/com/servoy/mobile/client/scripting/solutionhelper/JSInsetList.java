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

import com.servoy.base.scripting.solutionhelper.BaseSHInsetList;
import com.servoy.base.scripting.solutionhelper.IBaseSMFormInternal;
import com.servoy.base.solutionmodel.IBaseSMPortal;
import com.servoy.mobile.client.scripting.solutionmodel.JSMethod;

/**
 * This class is the representation of a mobile list component/form. 
 * 
 * @author acostescu
 */
@Export
public class JSInsetList extends BaseSHInsetList implements Exportable
{
	public JSInsetList(IBaseSMPortal portal, IBaseSMFormInternal contextForm)
	{
		super(portal, contextForm);
	}

	@Override
	@Getter
	public String getRelationName()
	{
		return super.getRelationName();
	}

	@Override
	@Setter
	public void setRelationName(String relationName)
	{
		super.setRelationName(relationName);
	}

	@Override
	@Getter
	public String getHeaderText()
	{
		return super.getHeaderText();
	}

	@Override
	@Setter
	public void setHeaderText(String headerText)
	{
		super.setHeaderText(headerText);
	}

	@Override
	@Getter
	public String getHeaderDataProviderID()
	{
		return super.getHeaderDataProviderID();
	}

	@Override
	@Setter
	public void setHeaderDataProviderID(String headerDataProviderID)
	{
		super.setHeaderDataProviderID(headerDataProviderID);
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
		return super.getSubtextDataProviderID();
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

	@Override
	@Getter
	public String getName()
	{
		return super.getName();
	}

	@Override
	@Setter
	public void setName(String name)
	{
		super.setName(name);
	}

	@Setter
	@Override
	public void setHeaderStyleClass(String styleClass)
	{
		super.setHeaderStyleClass(styleClass);
	}

	@Getter
	@Override
	public String getHeaderStyleClass()
	{
		return super.getHeaderStyleClass();
	}

	@Getter
	@Override
	public String getListStyleClass()
	{
		return super.getListStyleClass();
	}

	@Setter
	@Override
	public void setY(int y)
	{
		super.setY(y);
	}

	@Getter
	@Override
	public int getY()
	{
		return super.getY();
	}
}
