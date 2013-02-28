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
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.base.scripting.api.IJSRecord;
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
	private final Map<String, Integer> variableTypes;


	//existing record
	public Record(FoundSet p, RecordDescription rd)
	{
		parent = p;
		recordDescription = rd;

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

	/**
	 * @return the recordDescription
	 */
	public RecordDescription getRecordDescription()
	{
		return recordDescription;
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

		Record relatedRecord = getRelatedRecord(dataProviderID);
		if (relatedRecord != null)
		{
			return relatedRecord.getValue(getRelatedDataprovideID(dataProviderID));
		}

		RowDescription rd = getRow();
		if (rd == null || rd.getValue(dataProviderID) == null) return null;
		int type = getVariableType(dataProviderID);
		if (type == IColumnTypeConstants.DATETIME)
		{
			double value = rd.getNumberValue(dataProviderID);
			return new Date((long)value);
		}
		else if (type == IColumnTypeConstants.NUMBER || type == IColumnTypeConstants.INTEGER)
		{
			double value = rd.getNumberValue(dataProviderID);
			return new Double(value);
		}
		return rd.getValue(dataProviderID);
	}

	@Override
	public void setValue(String dataProviderID, Object obj)
	{
		Record relatedRecord = getRelatedRecord(dataProviderID);
		if (relatedRecord != null)
		{
			relatedRecord.setValue(getRelatedDataprovideID(dataProviderID), obj);
		}
		else
		{
			RowDescription rd = getRow();
			if (rd == null) return;
			parent.startEdit(this);

			Object oldObj = rd.getValue(dataProviderID);
			rd.setValue(dataProviderID, obj);

			if (!Utils.equalObjects(oldObj, obj)) fireModificationEvent(dataProviderID, obj);
		}
	}

	private Record getRelatedRecord(String dataProviderID)
	{
		int index = dataProviderID.indexOf('.');
		if (index > 0) //check if is related value request
		{
			String relationName = dataProviderID.substring(0, index);

			FoundSet foundSet = getRelatedFoundSet(relationName);
			if (foundSet != null)
			{
				//related data
				int selected = foundSet.getSelectedIndex();
				if (selected == -1 && foundSet.getSize() > 0) selected = 0;

				return foundSet.getRecord(selected);
			}
			return null;
		}
		return null;
	}

	private String getRelatedDataprovideID(String dataProviderID)
	{
		int index = dataProviderID.indexOf('.');
		if (index > 0)
		{
			return dataProviderID.substring(index + 1);
		}
		return dataProviderID;
	}

	public FoundSet getRelatedFoundSet(String relationName)
	{
		FoundSet retval = null;
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
						return retval;
					}
				}
			}
		}
		retval = parent.getRelatedFoundSet(this, relationName, null);
		if (retval != null)
		{
			int relationID = parent.getRelationID(relationName);
			recordDescription.getRFS().push(relationID + "|" + retval.getWhereArgsHash());
		}
		return retval;
	}

	public FoundSet getParent()
	{
		return parent;
	}

	public RowDescription getRow()
	{
		if (rowDescription == null)
		{
			rowDescription = parent.getRowDescription(getPK());
		}
		return rowDescription;
	}

	void clearRelationCaches()
	{
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
		return IColumnTypeConstants.MEDIA;
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
	public FoundSet getFoundset()
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

	public void pushedToServer()
	{
		getRow().setCreatedOnDevice(false);
	}

	@Override
	public void revertChanges()
	{
		clearRelationCaches();

		RowDescription storageDescription = parent.getLocalStorageRowDescription(getPK());
		if (storageDescription != null)
		{
			rowDescription = storageDescription;
		}
		else
		{
			parent.deleteRecord(this);
		}
		parent.getFoundSetManager().getEditRecordList().removeEditedRecord(this);
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
			sb.append(dp.getDataProviderID());
			sb.append(":");
			sb.append(getValue(dp.getDataProviderID()));
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}
}
