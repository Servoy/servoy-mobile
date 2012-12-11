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

import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMRelation;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSRelation implements IMobileSMRelation, Exportable
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getRelationItems()
	 */
	@Override
	public JSRelationItem[] getRelationItems()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#newRelationItem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSRelationItem newRelationItem(String dataprovider, String operator, String foreinColumnName)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#removeRelationItem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void removeRelationItem(String primaryDataProviderID, String operator, String foreignColumnName)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getAllowCreationRelatedRecords()
	 */
	@Override
	public boolean getAllowCreationRelatedRecords()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getAllowParentDeleteWhenHavingRelatedRecords()
	 */
	@Override
	public boolean getAllowParentDeleteWhenHavingRelatedRecords()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getDeleteRelatedRecords()
	 */
	@Override
	public boolean getDeleteRelatedRecords()
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getForeignDataSource()
	 */
	@Override
	public String getForeignDataSource()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getJoinType()
	 */
	@Override
	public int getJoinType()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getName()
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
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#getPrimaryDataSource()
	 */
	@Override
	public String getPrimaryDataSource()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setAllowCreationRelatedRecords(boolean)
	 */
	@Override
	public void setAllowCreationRelatedRecords(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setAllowParentDeleteWhenHavingRelatedRecords(boolean)
	 */
	@Override
	public void setAllowParentDeleteWhenHavingRelatedRecords(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setDeleteRelatedRecords(boolean)
	 */
	@Override
	public void setDeleteRelatedRecords(boolean arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setForeignDataSource(java.lang.String)
	 */
	@Override
	public void setForeignDataSource(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setJoinType(int)
	 */
	@Override
	public void setJoinType(int joinType)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setName(java.lang.String)
	 */
	@Override
	public void setName(String name)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMRelation#setPrimaryDataSource(java.lang.String)
	 */
	@Override
	public void setPrimaryDataSource(String arg)
	{
		// TODO ac Auto-generated method stub

	}

}
