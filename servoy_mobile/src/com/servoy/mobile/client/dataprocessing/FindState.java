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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.FoundSetDescription;
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
	private final Map<String, FoundSet> relatedStates = new HashMap<String, FoundSet>();

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
		if (!variableTypes.containsKey(dataProviderID))
		{
			Record relatedRecord = getRelatedRecord(dataProviderID, false);
			if (relatedRecord != null)
			{
				return relatedRecord.getValue(getRelatedDataprovideID(dataProviderID));
			}
			if (dataProviderID.indexOf('.') == -1)
			{
				FoundSet rfs = getRelatedFoundSet(dataProviderID);
				if (rfs != null)
				{
					return rfs;
				}
			}
		}
		return columndata.get(dataProviderID);
	}

	@Override
	public void setValue(String dataProviderID, Object obj)
	{
		if (variableTypes.containsKey(dataProviderID))
		{
			columndata.put(dataProviderID, obj);
		}
		else
		{
			Record relatedRecord = getRelatedRecord(dataProviderID, true);
			if (relatedRecord != null)
			{
				relatedRecord.setValue(getRelatedDataprovideID(dataProviderID), obj);
			}
			else
			{
				Debug.log("Ignoring unknown data provider '" + dataProviderID + "' in find mode");
			}
		}
	}

	@Override
	public RowDescription getRow()
	{
		return null;
	}

	@Override
	public FoundSet getRelatedFoundSet(String relationName)
	{
		if (relationName != null)
		{
			if (relatedStates.containsKey(relationName))
			{
				return relatedStates.get(relationName);
			}
			EntityDescription entityDescription = getFoundset().getFoundSetManager().getRelatedEntityDescription(getFoundset().getEntityName(), relationName);
			if (entityDescription != null)
			{
				FoundSet relatedFoundset = new RelatedFoundSet(getFoundset().getFoundSetManager(), this, FoundSetDescription.newInstance(
					entityDescription.getEntityName(), relationName, null), relationName);
				relatedFoundset.setFindMode();
				relatedStates.put(relationName, relatedFoundset);
				return relatedFoundset;
			}
		}
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

	public Map<String, Object> getAllData()
	{
		Map<String, Object> data = new HashMap<String, Object>(columndata);
		if (relatedStates.size() > 0)
		{
			for (String relationName : relatedStates.keySet())
			{
				FoundSet relatedFoundset = relatedStates.get(relationName);
				if (relatedFoundset != null && relatedFoundset.getSize() > 0)
				{
					List<Map<String, Object>> relatedRecordsList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < relatedFoundset.getSize(); i++)
					{
						FindState relatedFindState = (FindState)relatedFoundset.getRecord(i);
						if (relatedFindState != null)
						{
							Map<String, Object> relatedFindStateData = relatedFindState.getAllData();
							if (relatedFindStateData != null && relatedFindStateData.size() > 0)
							{
								relatedRecordsList.add(relatedFindStateData);
							}
						}
					}
					data.put(relationName, relatedRecordsList);
				}
			}
		}
		return data;
	}

	public Map<String, FoundSet> getRelatedStates()
	{
		return relatedStates;
	}

}
