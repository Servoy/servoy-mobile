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

package com.servoy.mobile.client.dataprocessing;

import java.util.HashMap;
import java.util.Map;

import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.util.Debug;

/**
 * @author lvostinar
 *
 */
public class FindState extends Record
{
	private final Map<String, Object> columndata = new HashMap<String, Object>();

	public FindState(FoundSet p, RecordDescription rd)
	{
		super(p, rd);
	}

	@Override
	public Object getValue(String dataProviderID)
	{
		Object jsValue = getJavascriptValue(dataProviderID);
		if (jsValue != null)
		{
			return jsValue;
		}
		return columndata.get(dataProviderID);
	}

	@Override
	public void setValue(String dataProviderID, Object obj)
	{
		if (dataProviderID.indexOf('.') > 0)
		{
			Debug.error("Trying to set a related dataprovider in find state, this is not supported yet in mobile client.", null);
			return;
		}
		columndata.put(dataProviderID, obj);
	}

	@Override
	public RowDescription getRow()
	{
		return null;
	}

	@Override
	public Object getPK()
	{
		return null;
	}

	@Override
	public Object[] getPKs()
	{
		return null;
	}

	@Override
	public boolean isEditing()
	{
		return true;
	}

	@Override
	public boolean isNew()
	{
		return false;
	}

	@Override
	public void revertChanges()
	{
	}

	public Map<String, Object> getColumnData()
	{
		return columndata;
	}

}
