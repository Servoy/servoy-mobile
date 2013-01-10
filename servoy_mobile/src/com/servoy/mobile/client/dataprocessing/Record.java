package com.servoy.mobile.client.dataprocessing;

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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.j2db.scripting.api.IJSFoundSet;
import com.servoy.j2db.scripting.api.IJSRecord;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile record
 * @author jblok
 */
public class Record extends Scope implements IJSRecord
{
	protected final FoundSet parent;
	protected final RecordDescription recordDescription;
	protected RowDescription rowDescription;
	protected final HashMap<String, FoundSet> relatedFoundSets;
	private final Map<String, Integer> variableTypes;


	//existing record
	public Record(FoundSet p, RecordDescription rd)
	{
		parent = p;
		recordDescription = rd;
		relatedFoundSets = new HashMap<String, FoundSet>();

		variableTypes = p.getFoundSetManager().exportColumns(p.getEntityName(), this, this);
		exportProperty("foundset");
		export();
	}

	//new record
	public Record(FoundSet foundSet, RecordDescription rd, RowDescription rowd)
	{
		this(foundSet, rd);
		rowDescription = rowd;
	}

	public Object getPK()
	{
		return recordDescription.getPK();
	}

	@Override
	public Object getValue(String dataProviderID)
	{
		if (dataProviderID == null || parent == null) return null;
		if ("foundset".equals(dataProviderID)) return parent; //$NON-NLS-1$
		if ("selectedIndex".equals(dataProviderID)) return Integer.valueOf(parent.jsFunction_getSelectedIndex()); //$NON-NLS-1$
		if ("maxRecordIndex".equals(dataProviderID) || "lazyMaxRecordIndex".equals(dataProviderID)) return Integer.valueOf(parent.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
		if ("currentRecordIndex".equals(dataProviderID)) return new Integer(parent.getRecordIndex(this) + 1); //$NON-NLS-1$

		if (!variableTypes.containsKey(dataProviderID))
		{
			FoundSet rfs = getRelatedFoundSet(dataProviderID);
			if (rfs != null)
			{
				return rfs;
			}
		}

		int index = dataProviderID.indexOf('.');
		if (index > 0) //check if is related value request
		{
			String relationName = dataProviderID.substring(0, index);
			String restName = dataProviderID.substring(index + 1);

			FoundSet foundSet = getRelatedFoundSet(relationName);
			if (foundSet != null)
			{
				//related data
				int selected = foundSet.getSelectedIndex();
				if (selected == -1 && foundSet.getSize() > 0) selected = 0;

				Record record = foundSet.getRecord(selected);
				if (record != null)
				{
					return record.getValue(restName);
				}
			}
			return null;
		}

		RowDescription rd = getRowDescription();
		if (rd == null || rd.getValue(dataProviderID) == null) return null;
		int type = getVariableType(dataProviderID);
		if (type == 93)
		{
			double value = rd.getNumberValue(dataProviderID);
			return new Date((long)value);
		}
		else if (type == 8 || type == 4)
		{
			double value = rd.getNumberValue(dataProviderID);
			return new Double(value);
		}
		return rd.getValue(dataProviderID);
	}

	@Override
	public void setValue(String dataProviderID, Object obj)
	{
		RowDescription rd = getRowDescription();
		if (rd == null) return;
		parent.startEdit(this);

		Object oldObj = rd.getValue(dataProviderID);
		rd.setValue(dataProviderID, obj);

		if (!Utils.equalObjects(oldObj, obj)) fireModificationEvent(dataProviderID, obj);
	}

	private RowDescription getRowDescription()
	{
		if (rowDescription == null)
		{
			rowDescription = parent.getRowDescription(getPK());
		}
		return rowDescription;
	}

	public FoundSet getRelatedFoundSet(String relationName)
	{
		FoundSet retval = relatedFoundSets.get(relationName);
		if (retval == null)
		{
			JsArrayString avail = recordDescription.getRFS();
			if (avail != null)
			{
				for (int i = 0; i < avail.length(); i++)
				{
					String key = avail.get(i);
					int relationID = parent.getRelationID(relationName);
					if (key.startsWith(relationID + "|"))
					{
						retval = parent.getRelatedFoundSet(this, relationName, key);
						if (retval != null)
						{
							relatedFoundSets.put(relationName, retval);
							return retval;
						}
					}
				}
			}
			retval = parent.createRelatedFoundSet(relationName, this);
			if (retval != null)
			{
				relatedFoundSets.put(relationName, retval);
				int relationID = parent.getRelationID(relationName);
				recordDescription.getRFS().push(relationID + "|" + retval.getWhereArgsHash());
			}
		}
		return retval;
	}

	public FoundSet getParent()
	{
		return parent;
	}

	public RowDescription getRow()
	{
		return rowDescription;
	}

	void clearRelationCaches()
	{
		relatedFoundSets.clear();
		recordDescription.clearRFS();
	}

	@Override
	public void setVariableType(String variable, int type)
	{
	}

	@Override
	public int getVariableType(String variable)
	{
		Integer type = variableTypes.get(variable);
		if (type != null) return type.intValue();
		return -4; // IColumnTypes.MEDIA;
	}

	public native void export()
	/*-{
		this.getDataSource = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getDataSource()();
		}
		this.getPKs = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getPKs()();
		}
		this.isEditing = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::isEditing()();
		}
		this.isNew = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::isNew()();
		}
		this.revertChanges = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::revertChanges()();
		}
		//		this.getChangedData = function() {
		//			return this.@com.servoy.mobile.client.dataprocessing.Record::getChangedData()();
		//		}
		//		this.hasChangedData = function() {
		//			return this.@com.servoy.mobile.client.dataprocessing.Record::hasChangedData()();
		//		}
	}-*/;

	@Override
	public String getDataSource()
	{
		return parent.getEntityName(); // TODO entity -> datasource??
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#getFoundset()
	 */
	@Override
	public IJSFoundSet getFoundset()
	{
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#getPKs()
	 */
	@Override
	public Object[] getPKs()
	{
		Object object = getPK();
		if (object instanceof Object[]) return (Object[])object;
		return new Object[] { object };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#isEditing()
	 */
	@Override
	public boolean isEditing()
	{
		return parent.getFoundSetManager().getEditRecordList().isEditting(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#isNew()
	 */
	@Override
	public boolean isNew()
	{
		return getRow().isCreatedOnDevice(); // TODO is created on device and is still editing??
	}

	@Override
	public void revertChanges()
	{
		clearRelationCaches();

		RowDescription storageDescription = parent.getRowDescription(getPK());
		if (storageDescription != null)
		{
			rowDescription = storageDescription;
		}
		else
		{
			parent.deleteRecord(this);
		}
		parent.getFoundSetManager().getEditRecordList().stopEditing(this);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Record[");
		EntityDescription entityDescription = parent.getFoundSetManager().getEntityDescription(parent.getEntityName());
		JsArray<DataProviderDescription> dataProviders = entityDescription.getDataProviders();
		for (int i = 0; i < dataProviders.length(); i++)
		{
			DataProviderDescription dp = dataProviders.get(i);
			sb.append(dp.getName());
			sb.append(":");
			sb.append(getValue(dp.getName()));
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}
}
