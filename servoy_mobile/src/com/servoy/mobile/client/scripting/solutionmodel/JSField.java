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

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMValueList;
import com.servoy.mobile.client.persistence.Field;

/**
 * @author acostescu
 */
@Export
public class JSField extends JSComponent implements IBaseSMField, Exportable
{

	/**
	 * @param f
	 * @param model
	 */
	public JSField(Field f, JSSolutionModel model)
	{
		// TODO ac Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getDisplaysTags()
	 */
	@Override
	public boolean getDisplaysTags()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getDisplayType()
	 */
	@Override
	public int getDisplayType()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getValuelist()
	 */
	@Override
	public JSValueList getValuelist()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setDataProviderID(java.lang.String)
	 */
	@Override
	public void setDataProviderID(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setDisplaysTags(boolean)
	 */
	@Override
	public void setDisplaysTags(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setDisplayType(int)
	 */
	@Override
	public void setDisplayType(int arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setValuelist(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMValueList)
	 */
	@Override
	public void setValuelist(IBaseSMValueList valuelist)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setOnAction(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnAction(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getOnAction()
	 */
	@Override
	public JSMethod getOnAction()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setOnDataChange(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnDataChange(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getOnDataChange()
	 */
	@Override
	public JSMethod getOnDataChange()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setOnFocusGained(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnFocusGained(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getOnFocusGained()
	 */
	@Override
	public JSMethod getOnFocusGained()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#setOnFocusLost(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnFocusLost(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMField#getOnFocusLost()
	 */
	@Override
	public JSMethod getOnFocusLost()
	{
		// TODO ac Auto-generated method stub
		return null;
	}


}
