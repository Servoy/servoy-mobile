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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;
import org.timepedia.exporter.client.NoExport;

import com.servoy.base.scripting.api.IJSDatabaseManager;
import com.servoy.base.scripting.api.IJSFoundSet;
import com.servoy.base.scripting.api.IJSRecord;
import com.servoy.base.util.DataSourceUtilsBase;
import com.servoy.mobile.client.dataprocessing.EditRecordList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.Record;

/**
 * @author jcompagner
 *
 */
@Export
public class JSDatabaseManager implements Exportable, IJSDatabaseManager
{
	private final FoundSetManager manager;

	public JSDatabaseManager(FoundSetManager manager)
	{
		this.manager = manager;
		export(ExporterUtil.wrap(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSDatabaseManager#saveData()
	 */
	@Override
	public boolean saveData() throws Exception
	{
		return manager.saveData() == EditRecordList.STOPPED;
	}

	public boolean setAutoSave(boolean b)
	{
		return manager.setAutoSave(b);
	}

	public boolean getAutoSave()
	{
		return manager.getAutoSave();
	}

	public String getDataSourceServerName(String dataSource)
	{
		String[] retval = DataSourceUtilsBase.getDBServernameTablename(dataSource);
		if (retval == null) return null;
		return retval[0];
	}

	public String getDataSourceTableName(String dataSource)
	{
		String[] retval = DataSourceUtilsBase.getDBServernameTablename(dataSource);
		if (retval == null) return null;
		return retval[1];
	}

	public String getDataSource(String serverName, String tableName)
	{
		return DataSourceUtilsBase.createDBTableDataSource(serverName, tableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSDatabaseManager#hasRecords(com.servoy.j2db.scripting.api.IJSFoundSet)
	 */
	@Override
	public boolean hasRecords(IJSFoundSet foundset)
	{
		if (foundset != null)
		{
			return foundset.getSize() > 0;
		}
		return false;
	}


	public boolean hasRecords(FoundSet foundset)
	{
		return hasRecords((IJSFoundSet)foundset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSDatabaseManager#hasRecords(com.servoy.j2db.scripting.api.IJSRecord, java.lang.String)
	 */
	@Override
	public boolean hasRecords(IJSRecord record, String relationString)
	{
		return JSDatabaseManager.hasRelatedRecords(record, relationString);
	}


	public boolean hasRecords(Record record, String relationString)
	{
		return JSDatabaseManager.hasRelatedRecords(record, relationString);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.base.scripting.api.IJSDatabaseManager#getFoundSet(java.lang.String)
	 */
	@Override
	public FoundSet getFoundSet(String dataSource) throws Exception
	{
		FoundSet foundset = manager.getFoundSet(FoundSetManager.getEntityFromDataSource(dataSource), false);
		foundset.flagFoundsetFiltered();
		return foundset;
	}

	private native void export(Object object)
	/*-{
		$wnd.databaseManager = object;
	}-*/;


	@NoExport
	public static boolean hasRelatedRecords(IJSRecord jsRecord, String relationString)
	{
		if (jsRecord instanceof Record)
		{
			Record record = (Record)jsRecord;
			boolean retval = false;
			String relatedFoundSets = relationString;
			String[] relations = relatedFoundSets.split("\\."); //$NON-NLS-1$
			for (String relationName : relations)
			{
				FoundSet rfs = record.getRelatedFoundSet(relationName);
				if (rfs != null && rfs.getSize() > 0)
				{
					retval = true;
					record = rfs.getRecord(0);
				}
				else
				{
					retval = false;
					break;
				}
			}
			return retval;
		}
		return false;
	}
}
