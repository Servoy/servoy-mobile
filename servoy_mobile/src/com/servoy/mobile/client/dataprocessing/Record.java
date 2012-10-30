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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.j2db.scripting.api.IJSDataSet;
import com.servoy.j2db.scripting.api.IJSFoundSet;
import com.servoy.j2db.scripting.api.IJSRecord;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile record
 * @author jblok
 */
public class Record extends Scope implements IJSRecord
{
	protected FoundSet parent;
	protected RecordDescription recordDescription;
	protected RowDescription rowDescription;
	protected HashMap<String, FoundSet> relatedFoundSets;

	//existing record
	public Record(FoundSet p, RecordDescription rd)
	{
		parent = p;
		recordDescription = rd;
		relatedFoundSets = new HashMap<String, FoundSet>();
		exportColumns();
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
		if ("foundset".equals(dataProviderID)) return parent;
		RowDescription rd = getRowDescription();
		if (rd == null) return null;
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
					if (key.startsWith(relationName + "|"))
					{
						retval = parent.getRelatedFoundSet(this, relationName, key);
						relatedFoundSets.put(relationName, retval);
						return retval;
					}
				}
			}
			retval = parent.createRelatedFoundSet(relationName, this);
			if (retval != null)
			{
				relatedFoundSets.put(relationName, retval);
				recordDescription.getRFS().push(relationName + "|" + retval.getWhereArgsHash());
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

	void linkupRelatedFoundSets()
	{
		ArrayList<String> pRelNames = parent.getAllPrimaryRelationNames();
		for (String relationName : pRelNames)
		{
			if (!relatedFoundSets.containsKey(relationName))
			{
				getRelatedFoundSet(relationName);
			}
		}
	}

	@Override
	public void setVariableType(String variable, int type)
	{
	}

	private final Map<String, Integer> variableTypes = new HashMap<String, Integer>();

	@Override
	public int getVariableType(String variable)
	{
		Integer type = variableTypes.get(variable);
		if (type != null) return type.intValue();
		return -4; // IColumnTypes.MEDIA;
	}

	private void exportColumns()
	{
		EntityDescription entityDescription = parent.getFoundSetManager().getEntityDescription(parent.getEntityName());
		// export all dataproviders
		JsArray<DataProviderDescription> dataProviders = entityDescription.getDataProviders();
		for (int i = 0; i < dataProviders.length(); i++)
		{
			DataProviderDescription dp = dataProviders.get(i);
			variableTypes.put(dp.getName(), dp.getType());
			exportProperty(dp.getName());
		}
		JsArray<RelationDescription> primaryRelations = entityDescription.getPrimaryRelations();
		// export all relations
		for (int i = 0; i < primaryRelations.length(); i++)
		{
			exportProperty(primaryRelations.get(i).getName());
		}
	}

	public native void export()
	/*-{
		this.getDataSource = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getDataSource()();
		}
		this.getChangedData = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getChangedData()();
		}
		this.getPKs = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getPKs()();
		}
		this.hasChangedData = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::hasChangedData()();
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
		
		this.getSelectedIndex = function() {
			return this.@com.servoy.mobile.client.dataprocessing.Record::getSelectedIndex()();
		}
		this.setSelectedIndex = function(index) {
			return this.@com.servoy.mobile.client.dataprocessing.Record::setSelectedIndex(I)(index);
		}

	}-*/;

	public int getSelectedIndex()
	{
		return 1;
	}

	public void setSelectedIndex(int index)
	{
		GWT.log("index: " + index);
	}

	@Override
	public String getDataSource()
	{
		return parent.getEntityName(); // TODO entity -> datasource??
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#getChangedData()
	 */
	@Override
	public IJSDataSet getChangedData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#getException()
	 */
	@Override
	public Exception getException()
	{
		// TODO Auto-generated method stub
		return null;
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
	 * @see com.servoy.j2db.scripting.api.IJSRecord#hasChangedData()
	 */
	@Override
	public boolean hasChangedData()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#isEditing()
	 */
	@Override
	public boolean isEditing()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#isNew()
	 */
	@Override
	public boolean isNew()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSRecord#revertChanges()
	 */
	@Override
	public void revertChanges()
	{
		// TODO Auto-generated method stub

	}
}
