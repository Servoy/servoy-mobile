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
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMValueList;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMField;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSField extends JSComponent implements IMobileSMField, Exportable
{

	public JSField(Field f, JSSolutionModel model, JSBase parent)
	{
		super(f, model, parent);
	}

	@Override
	@Getter
	public String getDataProviderID()
	{
		return ((Field)getBase()).getDataProviderID();
	}

	@Getter
	@Override
	public int getDisplayType()
	{
		return ((Field)getBase()).getDisplayType();
	}

	@Getter
	@Override
	public JSValueList getValuelist()
	{
		return new JSValueList(getSolutionModel().getApplication().getSolution().getValueListByUUID(((Field)getBase()).getValuelistID()));
	}

	@Setter
	@Override
	public void setDataProviderID(String arg)
	{
		cloneIfNeeded();
		((Field)getBase()).setDataProviderID(arg);
	}

	@Setter
	@Override
	public void setDisplayType(int arg)
	{
		cloneIfNeeded();
		((Field)getBase()).setDisplayType(arg);
	}

	@Setter
	@Override
	public void setValuelist(IBaseSMValueList valuelist)
	{
		setValuelist((JSValueList)valuelist);
	}

	public void setValuelist(JSValueList valuelist)
	{
		cloneIfNeeded();
		((Field)getBase()).setValueListID(valuelist.getUUID());
	}

	@Setter
	@Override
	public void setOnAction(IBaseSMMethod method)
	{
		setOnAction((JSMethod)method);
	}

	public void setOnAction(JSMethod method)
	{
		cloneIfNeeded();
		((Field)getBase()).setActionMethodCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnAction()
	{
		return JSMethod.getMethodFromString(((Field)getBase()).getActionMethodCall(), (JSForm)getParent(), getSolutionModel());
	}

	@Setter
	@Override
	public void setOnDataChange(IBaseSMMethod method)
	{
		setOnDataChange((JSMethod)method);
	}

	public void setOnDataChange(JSMethod method)
	{
		cloneIfNeeded();
		((Field)getBase()).setDataChangeMethodCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnDataChange()
	{
		return JSMethod.getMethodFromString(((Field)getBase()).getDataChangeMethodCall(), (JSForm)getParent(), getSolutionModel());
	}
}
