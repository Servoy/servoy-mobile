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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;
import org.timepedia.exporter.client.Getter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsDate;
import com.servoy.base.dataprocessing.BaseSQLGenerator;
import com.servoy.base.dataprocessing.ITypeConverter;
import com.servoy.base.dataprocessing.IValueConverter;
import com.servoy.base.persistence.BaseColumn;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.base.query.BaseAndCondition;
import com.servoy.base.query.BaseColumnType;
import com.servoy.base.query.BaseOrCondition;
import com.servoy.base.query.BaseQueryColumn;
import com.servoy.base.query.BaseQueryFactory;
import com.servoy.base.query.BaseQueryTable;
import com.servoy.base.query.IBaseSQLCondition;
import com.servoy.base.scripting.api.IJSFoundSet;
import com.servoy.base.scripting.api.IJSRecord;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.FoundSetDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.util.Debug;
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
	private final ArrayList<IFoundSetDataChangeListener> foundSetDataChangeListeners = new ArrayList<IFoundSetDataChangeListener>();
	private final JavaScriptObject javascriptInstance;
	private Date lastTouched = new Date();
	protected boolean findMode = false;
	// not all records are present in this foundset, records list contains all records
	protected boolean filteredFoundset = false;

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
			if (isInFind())
			{
				return IColumnTypeConstants.TEXT;
			}
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
		if (isInFind() || filteredFoundset)
		{
			return records.size();
		}
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
		return newRecord(integer - 1, Utils.getAsBoolean(changeSelection, true)) + 1;
	}

	@Export("newRecord")
	public int js_newRecord(int index, boolean changeSelection)
	{
		if (index > 0)
		{
			return newRecord(index - 1, changeSelection) + 1;
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

	@Export("deleteRecord")
	public boolean js_deleteRecord(int index) throws Exception
	{
		if (index >= 1 && index <= getSize())
		{
			return deleteRecord(getRecord(index - 1));
		}
		return false;
	}

	@Export("deleteAllRecords")
	@Override
	public boolean js_deleteAllRecords() throws Exception
	{
		for (int i = getSize(); i >= 1; i--)
		{
			js_deleteRecord(i);
		}
		return true;
	}

	@Export
	@Override
	public boolean find()
	{
		if (foundSetManager.getEditRecordList().stopIfEditing(this) == EditRecordList.STOPPED)
		{
			setFindMode();
			return true;
		}
		return false;
	}

	public void setFindMode()
	{
		flushAllRecords();
		findMode = true;
		filteredFoundset = false;
		newRecord(0, true);
		fireContentChanged();
	}

	private void cancelFindMode()
	{
		findMode = false;
		flushAllRecords();
	}

	@Export
	@Override
	public boolean isInFind()
	{
		return findMode;
	}

	@Export
	@Override
	public int search() throws Exception
	{
		if (isInFind())
		{
			IBaseSQLCondition moreWhere = getSearchCondition();
			if (moreWhere != null)
			{
				List<Record> result = new ArrayList<Record>();
				for (int i = 0; i < foundSetDescription.getRecords().length(); i++)
				{
					RecordDescription rd = foundSetDescription.getRecords().get(i);
					if (rd != null)
					{
						Record rec = new Record(this, rd);
						if (Utils.evalCondition(moreWhere, rec))
						{
							result.add(rec);
						}
					}
				}
				filteredFoundset = true;
				cancelFindMode();
				records.addAll(result);
				fireContentChanged();
				return result.size();
			}
			else
			{
				cancelFindMode();
				fireContentChanged();
			}
		}
		return 0;
	}

	IBaseSQLCondition getSearchCondition()
	{
		IBaseSQLCondition moreWhere = null;
		if (isInFind())
		{
			BaseQueryTable table = new BaseQueryTable(getEntityName(), null, null, null);
			for (Record record : records)
			{
				if (record instanceof FindState)
				{
					final FindState state = (FindState)record;
					IBaseSQLCondition and1 = null;
					IBaseSQLCondition and2 = null;
					Iterator<Map.Entry<String, Object>> it = state.getColumnData().entrySet().iterator();
					while (it.hasNext())
					{
						Map.Entry<String, Object> elem = it.next();
						final String dataProviderID = elem.getKey();
						Object raw = elem.getValue();
						final int dpType = state.getVariableType(dataProviderID);
						BaseQueryColumn qCol = new BaseQueryColumn(table, 0, dataProviderID, dataProviderID, new BaseColumnType(dpType, 0, 0), false);
						BaseColumn col = new BaseColumn()
						{

							@Override
							public int getLength()
							{
								return 0;
							}

							@Override
							public int getFlags()
							{
								return 0;
							}

							@Override
							public int getDataProviderType()
							{
								return dpType;
							}

							@Override
							public int getType()
							{
								return 0;
							}

							@Override
							public int getScale()
							{
								return 0;
							}

							@Override
							public String getSQLName()
							{
								return dataProviderID;
							}

							@Override
							public int getID()
							{
								return 0;
							}
						};

						if (raw instanceof JavaScriptObject)
						{
							if (Utils.isDate((JavaScriptObject)raw))
							{
								raw = new Date((long)((JsDate)raw).getTime());
							}
							else if (Utils.isArray((JavaScriptObject)raw))
							{
								JsArray rawJsArray = (JsArray)raw;
								String[] rawArray = new String[rawJsArray.length()];
								for (int i = 0; i < rawJsArray.length(); i++)
								{
									rawArray[i] = rawJsArray.get(i).toString();
								}
								raw = rawArray;
							}
						}

						and2 = BaseSQLGenerator.parseFindExpression(BaseQueryFactory.INSTANCE, raw, qCol, null, dpType, null, col, false, new IValueConverter()
						{

							@Override
							public Object convertFromObject(Object value)
							{
								return value;
							}
						}, new ITypeConverter()
						{

							@Override
							public Object getAsRightType(int type, int flags, Object obj, int l, boolean throwOnFail)
							{
								return Scope.getValueAsRightType(obj, dpType, null);
							}

							@Override
							public Object getAsRightType(int type, int flags, Object obj, String format, int l, boolean throwOnFail)
							{
								return Scope.getValueAsRightType(obj, dpType, format);
							}
						}, null, Debug.LOGGER);
						and1 = BaseAndCondition.and(and1, and2);
					}
					Map<String, FoundSet> relatedStates = state.getRelatedStates();
					for (String relationName : relatedStates.keySet())
					{
						FoundSet relatedFoundset = relatedStates.get(relationName);
						if (relatedFoundset != null)
						{
							IBaseSQLCondition relatedFoundsetCondition = relatedFoundset.getSearchCondition();
							if (relatedFoundsetCondition != null)
							{
								and2 = new RelatedFindCondition(relationName, relatedFoundsetCondition);
								and1 = BaseAndCondition.and(and1, and2);
							}
						}
					}
					moreWhere = BaseOrCondition.or(moreWhere, and1);
				}
			}
		}
		return moreWhere;
	}

	/*
	 * @see com.servoy.j2db.scripting.api.IJSFoundSet#sort(java.lang.Object)
	 */
	@Override
	@Export
	public void sort(Object recordComparisonFunction)
	{
		if (recordComparisonFunction instanceof JavaScriptObject)
		{
			// first load all records;
			for (int i = 0; i < getSize(); i++)
			{
				getRecord(i);
			}

			Collections.sort(records, new RecordComparater((JavaScriptObject)recordComparisonFunction));
			if (!filteredFoundset)
			{
				foundSetDescription.updateRecordDescriptions(records);
			}
			fireContentChanged();
		}
	}

	@Export("loadAllRecords")
	@Override
	public boolean js_loadAllRecords()
	{
		if (foundSetManager.getEditRecordList().stopIfEditing(this) != EditRecordList.STOPPED)
		{
			return false;
		}
		cancelFindMode();
		filteredFoundset = false;
		fireContentChanged();
		return true;
	}

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	public void flagFoundsetFiltered()
	{
		filteredFoundset = true;
	}

	public boolean isFoundsetFiltered()
	{
		return filteredFoundset;
	}

	public int newRecord(int index, boolean changeSelection)
	{
		Record retval = null;
		int size = getSize();
		index = (index < 0) ? 0 : (index > size) ? size : index;
		if (isInFind())
		{
			retval = new FindState(this, null);
		}
		else
		{
			String pk = foundSetManager.getNewPrimaryKey();
			RecordDescription recd = RecordDescription.newInstance(pk);
			RowDescription rowd = foundSetManager.createRowDescription(this, pk);
			retval = new Record(this, recd, rowd);
			foundSetDescription.insertRecord(index, recd);
			needToSaveFoundSetDescription = true;
			fillNotLoadedRecordsWithNull(index);
		}
		records.add(index, retval);
		if (changeSelection)
		{
			selectedIndex = index;
			fireContentChanged();
			fireSelectionChanged();
		}
		else
		{
			adjustSelectionAndContentOnListChange(index, false);
		}
		if (!isInFind())
		{
			startEdit(retval);
		}
		return index;
	}

	public void addRecord(String pk, RowDescription rowDescription)
	{
		RecordDescription recd = RecordDescription.newInstance(pk);
		Record retval = new Record(this, recd, rowDescription);
		int index = getSize();
		foundSetDescription.insertRecord(index, recd);
		fillNotLoadedRecordsWithNull(index);
		records.add(index, retval);
		adjustSelectionAndContentOnListChange(index, false);
	}

	public Record getRecord(int index)
	{
		Record retval = null;
		if (index >= 0)
		{
			if (index < records.size())
			{
				retval = records.get(index);
			}
			if (retval == null && index < getSize())
			{
				RecordDescription rd = foundSetDescription.getRecords().get(index);
				if (rd != null)
				{
					retval = new Record(this, rd);
				}
				fillNotLoadedRecordsWithNull(index);
				if (index == records.size())
				{
					records.add(index, retval);
				}
				else
				{
					records.set(index, retval);
				}
			}
		}
		return retval;
	}

	private void fillNotLoadedRecordsWithNull(int index)
	{
		if (index > records.size() && index < getSize())
		{
			// fill arraylist up to index with null values
			for (int i = records.size(); i <= index - 1; i++)
			{
				records.add(null);
			}
		}
	}

	public int getRecordIndex(Record record)
	{
		return records.indexOf(record);
	}

	public Record getRecordByPk(String pk)
	{
		for (Record record : records)
		{
			if (record != null && pk.equals(record.getPK().toString()))
			{
				return record;
			}
		}
		return null;
	}

	@Export
	public boolean deleteRecord(Record record)
	{
		int recordIndex = getRecordIndex(record);
		if (recordIndex >= 0)
		{
			records.remove(record);
			int listIndex = getRecordIndexInDescription(record.getPK());
			if (listIndex >= 0)
			{
				foundSetDescription.removeRecord(listIndex);
			}
			getFoundSetManager().getEditRecordList().removeEditedRecord(record);
			if (!isInFind()) getFoundSetManager().deleteRowData(getEntityName(), record.getRow(), record.isNew());
			record.flush();
			adjustSelectionAndContentOnListChange(recordIndex, true);
			return true;
		}
		return false;
	}

	/**
	 * @param deleted if it's not deleted then it's added
	 */
	private void adjustSelectionAndContentOnListChange(int recordIndex, boolean deleted)
	{
		fireContentChanged(); // record was added or removed - foundset content has changed

		boolean fireSelectionChanged = false;
		int newSelectedIndex = selectedIndex;
		if (recordIndex < selectedIndex)
		{
			fireSelectionChanged = true;
			if (deleted) newSelectedIndex--;
			else newSelectedIndex++;
		}
		else if (recordIndex == selectedIndex)
		{
			if (!deleted) newSelectedIndex++; // if deleted, the index doesn't change but we should still do a fire cause the selected record did change just the index didn't
			fireSelectionChanged = true;
		}

		if (newSelectedIndex < 0)
		{
			fireSelectionChanged = true;
			newSelectedIndex = 0;
		}

		if (newSelectedIndex >= getSize())
		{
			fireSelectionChanged = true;
			newSelectedIndex = getSize() - 1;
		}

		if (fireSelectionChanged) setSelectedIndexInternal(newSelectedIndex);
	}

	private int getRecordIndexInDescription(Object pk)
	{
		if (pk != null)
		{
			for (int i = 0; i < foundSetDescription.getRecords().length(); i++)
			{
				RecordDescription rd = foundSetDescription.getRecords().get(i);
				if (rd != null && rd.getPK().toString().equals(pk.toString()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	private int getRecordIndexInList(Object pk)
	{
		for (int i = 0; i < records.size(); i++)
		{
			Record r = records.get(i);
			if (r != null)
			{
				RecordDescription rd = r.getRecordDescription();
				if (rd != null && rd.getPK().toString().equals(pk.toString()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	public void removeRecord(Object pk)
	{
		int index = getRecordIndexInDescription(pk);
		if (index >= 0)
		{
			foundSetDescription.removeRecord(index);
		}
		index = getRecordIndexInList(pk);
		if (index >= 0)
		{
			if (index < records.size())
			{
				Record r = records.remove(index);
				if (r != null) r.flush();
			}
			adjustSelectionAndContentOnListChange(index, true);
		}
	}

	public void recordPushedToServer(String pk)
	{
		int index = getRecordIndexInList(pk);
		if (index >= 0 && index < records.size())
		{
			records.get(index).pushedToServer();
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

	public String getDataSource()
	{
		return foundSetManager.getEntityDescription(getEntityName()).getDataSource();
	}

	RowDescription getRowDescription(Object pk)
	{
		return foundSetManager.getRowDescription(getEntityName(), pk);
	}

	void flushRowDescription(Object pk)
	{
		foundSetManager.flushRowDescription(getEntityName(), pk);
	}

	RowDescription getLocalStorageRowDescription(Object pk)
	{
		return foundSetManager.getLocalStorageRowDescription(getEntityName(), pk);
	}


	FoundSet getRelatedFoundSet(Record rec, String name, String key)
	{
		return foundSetManager.getRelatedFoundSet(rec, getEntityName(), name, key);
	}

	public boolean startEdit(Record record)
	{
		boolean started = foundSetManager.getEditRecordList().startEditing(record);
		if (started)
		{
			foundSetManager.storeServerRowDataBeforeChange(getEntityName(), record.rowDescription);
		}
		return started;
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

	public void addFoundSetDataChangeListener(IFoundSetDataChangeListener listener)
	{
		if (foundSetDataChangeListeners.indexOf(listener) == -1) foundSetDataChangeListeners.add(listener);
	}

	public void removeFoundSetDataChangeListener(IFoundSetListener listener)
	{
		foundSetDataChangeListeners.remove(listener);
	}

	/**
	 * Called when the data of a record has changed.
	 *
	 * @param record the record that was changed.
	 * @param dataProviderID the dataprovider that was changed
	 * @param value the new value of that dataprovider.
	 */
	protected void fireRecordDataProviderChanged(Record record, String dataProviderID, Object value)
	{
		for (IFoundSetDataChangeListener l : foundSetDataChangeListeners)
			l.recordDataProviderChanged(record, dataProviderID, value);
	}

	int getRelationID(String relationName)
	{
		return foundSetManager.getRelationID(relationName);
	}


	String getPKDataProviderID()
	{
		return foundSetManager.getPKDataProviderID(getEntityName());
	}

	/**
	 * @return
	 */
	public JavaScriptObject getJavaScriptInstance()
	{
		return javascriptInstance;
	}

	public void touch()
	{
		lastTouched = new Date();
	}

	public void flushIfPossible()
	{
		if (!filteredFoundset && foundSetListeners.size() == 0 && selectionListeners.size() == 0 && foundSetDataChangeListeners.size() == 0)
		{
			Date currentTime = new Date();
			if (currentTime.getTime() - lastTouched.getTime() > 15000)
			{
				// 15 seconds not touched, assume we can flush it
				flushAllRecords();
			}
		}
	}

	private void flushAllRecords()
	{
		if (!isInFind())
		{
			for (Record record : records)
			{
				if (record != null) record.flush();
			}
			records.clear();
		}
	}


	/**
	 * @param currentFSD
	 */
	public void updateFoundSetDescription(FoundSetDescription currentFSD)
	{
		// if this foundset has editing records then we have to make it a filtered foundset
		// so a foundset where the "records" list is leading instead of the FoundSetDescription
		if (!filteredFoundset && !findMode && foundSetManager.getEditRecordList().hasEditedRecords(this))
		{
			// load in all records and then set the filtered to true.
			for (int i = 0; i < getSize(); i++)
			{
				getRecord(i);
			}
			filteredFoundset = true;
		}
		foundSetDescription.setRecordDescriptions(currentFSD.getRecords());
		if (!filteredFoundset && !findMode)
		{
			// if there are outstanding edits then this will delete the records.
			flushAllRecords();
		}

		// (re) export all relations
		JsArray<RelationDescription> primaryRelations = foundSetManager.getEntityDescription(getEntityName()).getPrimaryRelations();
		for (int i = 0; i < primaryRelations.length(); i++)
		{
			String name = primaryRelations.get(i).getName();
			this.exportProperty(javascriptInstance, name);
		}
	}


	@Export("getRecordIndex")
	@Override
	public int js_getRecordIndex(IJSRecord record)
	{
		return js_getRecordIndex((Record)record);
	}

	@Export("getRecordIndex")
	public int js_getRecordIndex(Record record)
	{
		int recordIndex = getRecordIndex(record);
		if (recordIndex == -1) return -1;
		return recordIndex + 1;
	}

	@Getter("alldataproviders")
	public String[] getAlldataproviders()
	{
		return alldataproviders();
	}

	// we can't annotate this with getter - as it will bump into an timepedia exporter bug right now - and do stackoverflow (when calling in JS
	// the method (alldataproviders()) it actually references it's property who's getter tries to do all this again)
	public String[] alldataproviders()
	{
		String[] dp;
		JsArray<DataProviderDescription> dpArray = foundSetManager.getEntityDescription(getEntityName()).getDataProviders();

		if (dpArray != null)
		{
			dp = new String[dpArray.length()];
			for (int i = 0; i < dpArray.length(); i++)
			{
				dp[i] = dpArray.get(i).getDataProviderID();
			}
		}
		else
		{
			dp = new String[0];
		}

		return dp;
	}


	FoundSetDescription getFoundSetDescription()
	{
		return foundSetDescription;
	}

}
