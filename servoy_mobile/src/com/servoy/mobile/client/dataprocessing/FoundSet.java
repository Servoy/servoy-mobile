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
import java.util.Collections;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.j2db.persistence.constants.IColumnTypeConstants;
import com.servoy.j2db.scripting.api.IJSFoundSet;
import com.servoy.j2db.scripting.api.IJSRecord;
import com.servoy.mobile.client.dto.FoundSetDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile foundset
 * @author jblok
 */
public class FoundSet extends Scope implements Exportable, IJSFoundSet //  extends Scope if we support aggregates on foundset, then we have to drop Exportable
{
	private final FoundSetManager foundSetManager;
	private final FoundSetDescription foundSetDescription;
	private final ArrayList<Record> records = new ArrayList<Record>();
	private final ArrayList<IFoundSetListener> foundSetListeners = new ArrayList<IFoundSetListener>();
	private final JavaScriptObject javascriptInstance;


	private boolean needToSaveFoundSetDescription;

	private int selectedIndex = 0;

	public FoundSet(FoundSetManager fsm, FoundSetDescription fsd)
	{
		foundSetManager = fsm;
		foundSetDescription = fsd;
		javascriptInstance = ExporterUtil.wrap(this);
		getFoundSetManager().exportColumns(getEntityName(), this, javascriptInstance);
	}


	@Override
	public void setVariableType(String variable, int type)
	{
	}

	@Override
	public int getVariableType(String variable)
	{
		// TODO aggregates.

		Record record = getSelectedRecord();
		if (record != null)
		{
			return record.getVariableType(variable);
		}
		return IColumnTypeConstants.MEDIA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.Scope#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String variable)
	{
		Record record = getSelectedRecord();
		if (record != null)
		{
			return record.getValue(variable);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.Scope#setValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(String variable, Object value)
	{
		Record record = getSelectedRecord();
		if (record != null)
		{
			record.setValue(variable, value);
		}
	}

	// 1-based
	@Export("setSelectedIndex")
	public void jsFunction_setSelectedIndex(int index)
	{
		setSelectedIndexInternal(index - 1);
	}

	// 0-based
	public void setSelectedIndexInternal(int index)
	{
		if (index >= 0 && index < getSize())
		{
			selectedIndex = index;
			fireSelectionChanged();
		}
	}

	@Export("getSelectedIndex")
	public int jsFunction_getSelectedIndex()
	{
		return selectedIndex + 1;
	}

	@Export("getRecord")
	public Record js_getRecord(int index)
	{
		return getRecord(index - 1);
	}

	@Export("getSize")
	public int getSize()
	{
		return foundSetDescription.getRecords().length();
	}

	@Export
	public Record getSelectedRecord()
	{
		return getRecord(selectedIndex);
	}

	@Export("newRecord")
	public int js_newRecord()
	{
		return js_newRecord(Integer.valueOf(1), Boolean.TRUE);

	}

	@Export("newRecord")
	public int js_newRecord(Number index, Boolean changeSelection)
	{
		int integer = Utils.getAsInteger(index, 1);
		if (newRecord(integer - 1, Utils.getAsBoolean(changeSelection, true)) != null)
		{
			return integer;
		}
		return -1;
	}

	@Override
	@Export("deleteRecord")
	public boolean js_deleteRecord() throws Exception
	{
		return deleteRecord(getSelectedRecord());
	}

	@Override
	@Export("deleteRecord")
	public boolean js_deleteRecord(IJSRecord record) throws Exception
	{
		return deleteRecord((Record)record);
	}

	@Override
	@Export("deleteRecord")
	public boolean js_deleteRecord(Number index) throws Exception
	{
		if (index != null)
		{
			return deleteRecord(getRecord(index.intValue() - 1));
		}
		return false;
	}

	/*
	 * @see com.servoy.j2db.scripting.api.IJSFoundSet#sort(java.lang.Object)
	 */
	@Override
	@Export
	public void sort(Object comparator)
	{
		if (comparator instanceof JavaScriptObject)
		{
			// first load all records;
			for (int i = 0; i < getSize(); i++)
			{
				getRecord(i);
			}

			Collections.sort(records, new RecordComparater((JavaScriptObject)comparator));
			fireContentChanged();
		}
	}

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	public Record newRecord(int index, boolean changeSelection)
	{
		String pk = foundSetManager.getNewPrimaryKey();
		RecordDescription recd = RecordDescription.newInstance(pk);
		RowDescription rowd = foundSetManager.createRowDescription(this, pk);
		Record retval = new Record(this, recd, rowd);
		foundSetDescription.getRecords().push(recd);
		needToSaveFoundSetDescription = true;
		records.add(index, retval);
		if (changeSelection)
		{
			selectedIndex = index;
			fireSelectionChanged();
		}
		startEdit(retval);
		return retval;
	}

	public Record getRecord(int index)
	{
		Record retval = null;
		if (index < records.size())
		{
			retval = records.get(index);
		}
		else
		{
			RecordDescription rd = foundSetDescription.getRecords().get(index);
			if (rd != null)
			{
				retval = new Record(this, rd);
				if (index == records.size())
				{
					records.add(retval);
				}
				else
				{
					records.set(index, retval);
				}
			}
		}
		return retval;
	}

	public int getRecordIndex(Record record)
	{
		return records.indexOf(record);
	}

	public boolean deleteRecord(Record record)
	{
		int recordIndex = getRecordIndex(record);
		if (recordIndex >= 0)
		{
			records.remove(record);
			foundSetDescription.removeRecord(recordIndex);
			getFoundSetManager().getEditRecordList().removeEditedRecord(record);
			if (!record.isNew())
			{
				getFoundSetManager().deleteRowData(getEntityName(), record.getRow());
			}
			return true;
		}
		return false;
	}

	public void removeRecord(Object pk)
	{
		for (int i = 0; i < getSize(); i++)
		{
			RecordDescription rd = foundSetDescription.getRecords().get(i);
			if (rd != null && rd.getPK().toString().equals(pk.toString()))
			{
				foundSetDescription.removeRecord(i);
				if (i < records.size())
				{
					records.remove(i);
				}
			}
		}
	}

	public FoundSetManager getFoundSetManager()
	{
		return foundSetManager;
	}

	public String getEntityName()
	{
		return foundSetDescription.getEntityName();
	}

	RowDescription getRowDescription(Object pk)
	{
		return foundSetManager.getRowDescription(getEntityName(), pk);
	}

	FoundSet getRelatedFoundSet(Record rec, String name, String key)
	{
		return foundSetManager.getRelatedFoundSet(rec, getEntityName(), name, key);
	}

	public boolean startEdit(Record record)
	{
		return foundSetManager.getEditRecordList().startEditing(record);
	}

	public Object getSelectedRecordValue(String dataProviderID)
	{
		if (getSize() > 0)
		{
			return getSelectedRecord().getValue(dataProviderID);
		}
		return null;
	}

	FoundSetDescription needToSaveFoundSetDescription()
	{
		if (needToSaveFoundSetDescription)
		{
			needToSaveFoundSetDescription = false;
			return foundSetDescription;
		}
		return null;
	}

	FoundSet createRelatedFoundSet(String relationName, Record record)
	{
		return foundSetManager.createRelatedFoundSet(relationName, record);
	}

	String getWhereArgsHash()
	{
		return foundSetDescription.getWhereArgsHash();
	}

	ArrayList<String> getAllPrimaryRelationNames()
	{
		return foundSetManager.getAllPrimaryRelationNames(getEntityName());
	}

	private final ArrayList<IFoundSetSelectionListener> selectionListeners = new ArrayList<IFoundSetSelectionListener>();

	public void addSelectionListener(IFoundSetSelectionListener listener)
	{
		if (selectionListeners.indexOf(listener) == -1) selectionListeners.add(listener);
	}

	public void removeSelectionListener(IFoundSetSelectionListener listener)
	{
		selectionListeners.remove(listener);
	}

	protected void fireSelectionChanged()
	{
		if (selectionListeners.size() > 0)
		{
			IFoundSetSelectionListener[] array = selectionListeners.toArray(new IFoundSetSelectionListener[selectionListeners.size()]);
			for (IFoundSetSelectionListener l : array)
				l.selectionChanged();
		}
	}

	public void addFoundSetListener(IFoundSetListener listener)
	{
		if (foundSetListeners.indexOf(listener) == -1) foundSetListeners.add(listener);
	}

	public void removeFoundSetListener(IFoundSetListener listener)
	{
		foundSetListeners.remove(listener);
	}

	protected void fireContentChanged()
	{
		for (IFoundSetListener l : foundSetListeners)
			l.contentChanged();
	}

	int getRelationID(String relationName)
	{
		return foundSetManager.getRelationID(relationName);
	}


	/**
	 * @return
	 */
	public Object getJavaScriptInstance()
	{
		return javascriptInstance;
	}
}
