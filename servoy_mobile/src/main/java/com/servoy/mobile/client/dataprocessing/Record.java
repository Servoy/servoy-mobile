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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.base.scripting.api.IJSRecord;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;

/**
 * The mobile record
 * @author jblok
 */
public class Record extends Scope implements IJSRecord, IRowChangeListener
{
	protected final FoundSet parent;
	protected final RecordDescription recordDescription;
	protected RowDescription rowDescription;
	protected final Map<String, Integer> variableTypes;


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
		rowDescription.addRowChangeListener(this);
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

	protected Object getJavascriptValue(String dataProviderID)
	{
		if ("foundset".equals(dataProviderID)) return parent; //$NON-NLS-1$
		if ("selectedIndex".equals(dataProviderID)) return Integer.valueOf(parent.jsFunction_getSelectedIndex()); //$NON-NLS-1$
		if ("maxRecordIndex".equals(dataProviderID) || "lazyMaxRecordIndex".equals(dataProviderID)) return Integer.valueOf(parent.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
		if ("currentRecordIndex".equals(dataProviderID)) return new Integer(parent.getRecordIndex(this) + 1); //$NON-NLS-1$
		return null;
	}

	@Override
	public Object getValue(String dataProviderID)
	{
		if (dataProviderID == null || parent == null) return null;
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
			else
			{
				// todo this could be (laxy)maxRecordIndex should that return 0 or nothing/null?
				return null;
			}
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
		Record relatedRecord = getRelatedRecord(dataProviderID, true);
		if (relatedRecord != null)
		{
			relatedRecord.setValue(getRelatedDataprovideID(dataProviderID), obj);
		}
		else
		{
			RowDescription rd = getRow();
			if (rd == null) return;
			parent.startEdit(this);

			if (obj != null)
			{
				int type = getVariableType(dataProviderID);
				obj = getValueAsRightType(obj, type, null);
				if (type == IColumnTypeConstants.DATETIME && !(obj instanceof Date || obj instanceof Number))
				{
					Log.error("Can't set value: " + obj + " on dataprovider: " + dataProviderID + " of datasource: " + parent.getEntityName() +
						", not a date or number");
					return;
				}
			}

			if (recordDescription != null)
			{
				recordDescription.clearRFS();
				String pkDataProviderID = parent.getPKDataProviderID();
				if (pkDataProviderID != null && pkDataProviderID.equals(dataProviderID))
				{
					recordDescription.setPK(obj);
				}
			}

			rd.setValue(dataProviderID, obj);
		}
	}

	@Override
	public void notifyChange(String dataProviderID, Object value)
	{
		fireModificationEvent(dataProviderID, value);

		// for things that need to listen to all data changes in a foundset:
		FoundSet parentFoundset = getParent();
		if (parentFoundset != null)
		{
			parentFoundset.fireRecordDataProviderChanged(this, dataProviderID, value);
		}
	}

	protected Record getRelatedRecord(String dataProviderID, boolean create)
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
				if (create && foundSet.getSize() == 0)
				{
					EntityDescription entityDescription = parent.getFoundSetManager().getEntityDescription(parent.getEntityName());
					if (entityDescription != null)
					{
						RelationDescription relationDescription = entityDescription.getPrimaryRelation(relationName);
						if (relationDescription != null && relationDescription.getAllowCreationRelatedRecords())
						{
							foundSet.newRecord(0, true);
							parent.startEdit(foundSet.getRecord(0));
						}
					}
				}
				return foundSet.getRecord(selected);
			}
			return null;
		}
		return null;
	}

	protected String getRelatedDataprovideID(String dataProviderID)
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
			int relationID = parent.getRelationID(relationName);
			for (int i = 0; i < avail.length(); i++)
			{
				String key = avail.get(i);
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
			if (rowDescription == null)
			{
				Log.error("Row description for pk : " + getPK() + " of datasource: " + getDataSource() + " not found.");
				return null;
			}
			rowDescription.addRowChangeListener(this);
		}
		return rowDescription;
	}

	public boolean hasRowDescription()
	{
		return parent.getRowDescription(getPK()) != null;
	}

	void clearRelationCaches()
	{
		if (recordDescription != null) recordDescription.clearRFS();
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
		return parent.getDataSource();
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

	@Override
	public boolean isNew()
	{
		return getRow().isCreatedOnDevice() && parent.getLocalStorageRowDescription(getPK()) == null;
	}

	public boolean isCreatedOnDevice()
	{
		return getRow().isCreatedOnDevice();
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
			flush();
			rowDescription = storageDescription;
		}
		else
		{
			parent.deleteRecord(this);
		}
		parent.getFoundSetManager().getEditRecordList().removeEditedRecord(this);
	}

	public void flush()
	{
		if (rowDescription != null)
		{
			rowDescription.removeRowChangeListener(this);
			rowDescription = null;
		}
		if (recordDescription != null)
		{
			parent.flushRowDescription(getPK());
		}
		clearRelationCaches();
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((recordDescription == null) ? 0 : recordDescription.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Record other = (Record)obj;
		if (parent == null)
		{
			if (other.parent != null) return false;
		}
		else if (!parent.equals(other.parent)) return false;
		if (recordDescription == null)
		{
			if (other.recordDescription != null) return false;
		}
		else if (!recordDescription.equals(other.recordDescription)) return false;
		return true;
	}
}
