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

import com.servoy.j2db.scripting.api.IJSDatabaseManager;
import com.servoy.j2db.scripting.api.IJSFoundSet;
import com.servoy.j2db.scripting.api.IJSRecord;
import com.servoy.mobile.client.dataprocessing.EditRecordList;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSDatabaseManager#saveData(com.servoy.j2db.scripting.api.IJSFoundSet)
	 */
	@Override
	public boolean saveData(IJSFoundSet foundset) throws Exception
	{
		// TODO just save all record from only that foundset
		return manager.saveData() == EditRecordList.STOPPED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSDatabaseManager#saveData(com.servoy.j2db.scripting.api.IJSRecord)
	 */
	@Override
	public boolean saveData(IJSRecord record) throws Exception
	{
		// TODO just save that record
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


	private native void export(Object object)
	/*-{
		$wnd.databaseManager = object;
	}-*/;


}
