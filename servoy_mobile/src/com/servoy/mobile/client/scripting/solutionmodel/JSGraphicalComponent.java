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

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * @author acostescu
 */
@Export
public class JSGraphicalComponent extends JSComponent implements IBaseSMGraphicalComponent, Exportable
{

	public JSGraphicalComponent(GraphicalComponent gc, JSSolutionModel model)
	{
		super(gc, model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getX()
	 */
	@Override
	public int getX()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getY()
	 */
	@Override
	public int getY()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getName()
	 */
	@Override
	public String getName()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getEnabled()
	 */
	@Override
	public boolean getEnabled()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getVisible()
	 */
	@Override
	public boolean getVisible()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getWidth()
	 */
	@Override
	public int getWidth()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#getHeight()
	 */
	@Override
	public int getHeight()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setX(int)
	 */
	@Override
	public void setX(int x)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setY(int)
	 */
	@Override
	public void setY(int y)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setName(java.lang.String)
	 */
	@Override
	public void setName(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setWidth(int)
	 */
	@Override
	public void setWidth(int width)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent#setHeight(int)
	 */
	@Override
	public void setHeight(int height)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#getDesignTimeProperty(java.lang.String)
	 */
	@Override
	public Object getDesignTimeProperty(String key)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#putDesignTimeProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object putDesignTimeProperty(String key, Object value)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#removeDesignTimeProperty(java.lang.String)
	 */
	@Override
	public Object removeDesignTimeProperty(String key)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#getLabelFor()
	 */
	@Override
	public String getLabelFor()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#getDataProviderID()
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
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#getDisplaysTags()
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
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#getText()
	 */
	@Override
	public String getText()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#setLabelFor(java.lang.String)
	 */
	@Override
	public void setLabelFor(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#setDataProviderID(java.lang.String)
	 */
	@Override
	public void setDataProviderID(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#setDisplaysTags(boolean)
	 */
	@Override
	public void setDisplaysTags(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#setText(java.lang.String)
	 */
	@Override
	public void setText(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#setOnAction(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnAction(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMGraphicalComponent#getOnAction()
	 */
	@Override
	public JSMethod getOnAction()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

}
