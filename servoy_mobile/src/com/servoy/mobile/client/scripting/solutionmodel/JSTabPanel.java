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

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel;
import com.servoy.mobile.client.persistence.Field;

/**
 * @author acostescu
 *
 */
@Export
public class JSTabPanel extends JSComponent implements IBaseSMTabPanel, Exportable
{

	/**
	 * @param f
	 * @param model
	 */
	public JSTabPanel(Field f, String formName, JSSolutionModel model)
	{
		super(f, formName, model);
		// TODO ac Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#newTab(java.lang.String, java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm)
	 */
	@Override
	public JSTab newTab(String name, String text, IBaseSMForm form)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#newTab(java.lang.String, java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm, java.lang.Object)
	 */
	@Override
	public JSTab newTab(String name, String text, IBaseSMForm form, Object relation)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#getTab(java.lang.String)
	 */
	@Override
	public JSTab getTab(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#removeTab(java.lang.String)
	 */
	@Override
	public void removeTab(String name)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#getTabs()
	 */
	@Override
	public JSTab[] getTabs()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#getScrollTabs()
	 */
	@Override
	public boolean getScrollTabs()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#getTabOrientation()
	 */
	@Override
	public int getTabOrientation()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#setScrollTabs(boolean)
	 */
	@Override
	public void setScrollTabs(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMTabPanel#setTabOrientation(int)
	 */
	@Override
	public void setTabOrientation(int arg)
	{
		// TODO ac Auto-generated method stub

	}

}
