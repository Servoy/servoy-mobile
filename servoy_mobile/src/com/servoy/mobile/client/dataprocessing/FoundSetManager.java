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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.storage.client.Storage;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.FoundSetDescription;
import com.servoy.mobile.client.dto.OfflineDataDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.util.DataproviderIdAndTypeHolder;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile foundset manager
 * @author jblok
 */
@SuppressWarnings("nls")
public class FoundSetManager
{
	private static final String CHANGES_KEY = "changes";
	private static final String DELETES_KEY = "deletes";
	private static final String ENTITY_PREFIX_KEY = "entityPrefix";
	private static final String ENTITIES_KEY = "entities";
	private static final String STORAGE_VERSION_KEY = "storage_version";
	private static final int STORAGE_VERSION = 1;

	private final Storage localStorage = Storage.getLocalStorageIfSupported();
	private final ValueStore valueStore = new ValueStore(localStorage);
	private final MobileClient application;
	private EditRecordList editRecordList;

	//fields mapped to local storage
	private Entities entities;
	private String entityPrefix;
	private final int storage_version;
	private ArrayList<String> changes;
	private ArrayList<String> deletes;
	HashMap<String, HashSet<FoundSet>> entitiesToFoundsets;

	public FoundSetManager(MobileClient mc)
	{
		application = mc;
		editRecordList = new EditRecordList(this);

		//check storage version, if not 1 do clear storage
		storage_version = Utils.getAsInteger(localStorage.getItem(STORAGE_VERSION_KEY));
		if (storage_version != STORAGE_VERSION)
		{
			localStorage.clear();
		}

		String ejson = localStorage.getItem(ENTITIES_KEY);
		if (ejson != null)
		{
			JSONArray ea = JSONParser.parseStrict(ejson).isArray();
			if (ea != null)
			{
				JsArray<EntityDescription> e = ea.getJavaScriptObject().cast();
				entities = new Entities(e, null);
			}
		}
		entityPrefix = localStorage.getItem(ENTITY_PREFIX_KEY);
		changes = new ArrayList<String>();
		addItems(localStorage.getItem(CHANGES_KEY), changes);

		deletes = new ArrayList<String>();
		addItems(localStorage.getItem(DELETES_KEY), deletes);

		entitiesToFoundsets = new HashMap<String, HashSet<FoundSet>>();

	}

	private void addItems(String cjson, List<String> list)
	{
		if (cjson != null)
		{
			JSONArray ca = JSONParser.parseStrict(cjson).isArray();
			if (ca != null)
			{
				JsArrayString jca = ca.getJavaScriptObject().cast();
				for (int i = 0; i < jca.length(); i++)
				{
					list.add(jca.get(i));
				}
			}
		}
	}

	public boolean hasContent()
	{
		return (localStorage.getItem(ENTITIES_KEY) != null);
	}

	public void exportDataproviders()
	{
		if (entities != null)
		{
			Set<String> exported = new HashSet<String>();
			JsArray<EntityDescription> eds = entities.getEntityDescriptions();
			for (int i = 0; i < eds.length(); i++)
			{
				EntityDescription ed = eds.get(i);
				JsArray<DataProviderDescription> dataProviders = ed.getDataProviders();
				for (int k = 0; k < dataProviders.length(); k++)
				{
					String name = dataProviders.get(k).getName();
					if (exported.add(name)) export(name);
				}
				JsArray<RelationDescription> primaryRelations = ed.getPrimaryRelations();
				for (int k = 0; k < primaryRelations.length(); k++)
				{
					String name = primaryRelations.get(k).getName();
					if (exported.add(name)) export(name);
				}
			}
		}
	}

	private native void export(String name) /*-{
		$wnd._ServoyUtils_.defineWindowVariable(name);
	}-*/;

	public EntityDescription getEntityDescription(String entityName)
	{
		return entities.getDescription(entityName);
	}

	RowDescription getRowDescription(String entityName, Object pk)
	{
		//load data from offline db
		String json = localStorage.getItem(entityName + '|' + pk);
		if (json == null) return null;

		JSONArray values = JSONParser.parseStrict(json).isArray();
		String[] dataProviders = entities.getDataProviders(entityName);
		return RowDescription.newInstance(dataProviders, values);
	}

	FoundSet getRelatedFoundSet(Record rec, String entityName, String relationName, String key)
	{
		//load data from offline db
		String json = localStorage.getItem(key);
		if (json == null) return null;

		FoundSetDescription fsd = JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
		RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
		if (fsd.needsInfoFromKey()) fsd.setInfoFromKey(key, relationName, rd.getForeignEntityName());
		removeDeletedRecords(fsd);
		FoundSet foundset = new RelatedFoundSet(this, rec, fsd, relationName);
		addFoundsetInList(foundset);
		return foundset;
	}


	String getEntityPrefix()
	{
		return (entityPrefix == null ? "" : entityPrefix);
	}

	void storeOfflineData(OfflineDataProxy offlineDataProxy, String fsname, OfflineDataDescription offlineData)
	{
		HashMap<String, HashSet<Object>> entitiesToPKs = new HashMap<String, HashSet<Object>>();

		localStorage.setItem(STORAGE_VERSION_KEY, String.valueOf(STORAGE_VERSION));

		//store data in offline db
		entities = new Entities(offlineData.getEntities(), valueStore);
		localStorage.setItem(ENTITIES_KEY, entities.toJSONArray());

		entityPrefix = offlineData.getEntityPrefix();
		if (entityPrefix != null)
		{
			localStorage.setItem(ENTITY_PREFIX_KEY, entityPrefix);
		}

		JsArray<FoundSetDescription> fsds = offlineData.getFoundSets();
		if (fsds != null)
		{
			for (int i = 0; i < fsds.length(); i++)
			{
				FoundSetDescription fd = fsds.get(i);
				String entityName = fd.getEntityName();

				//fill entitiesToPKs
				HashSet<Object> set = entitiesToPKs.get(entityName);
				if (set == null)
				{
					set = new HashSet<Object>();
					entitiesToPKs.put(entityName, set);
				}
				set.addAll(fd.getPKs());

				boolean uuidPK = entities.isPKUUID(entityName);
				JsArray<RecordDescription> recs = fd.getRecords();
				for (int j = 0; j < recs.length(); j++)
				{
					RecordDescription rd = recs.get(j);
					//replace UUIDs, to save local storage space
					if (uuidPK)
					{
						String uuid = (String)rd.getPK();
						int newPK = valueStore.putUUID(uuid);
						rd.setPK(String.valueOf(newPK));
					}
					//replace relation names with relation ids, to save local storage space
					JsArrayString rfs = rd.getRFS();
					for (int k = 0; k < rfs.length(); k++)
					{
						String key = rfs.get(k);
						int idx = key.indexOf('|');
						int id = entities.getRelationID(key.substring(0, idx));
						String hash = key.substring(idx + 1);
						hash = replaceUUIDHash(hash);
						rfs.set(k, id + "|" + hash);
					}
				}

				//store data in offline db
				storeFoundSetDescription(fd);
			}
		}
		//initiate load of all row data
		offlineDataProxy.requestRowData(entitiesToPKs);
	}

	private void storeFoundSetDescription(FoundSetDescription fd)
	{
		boolean omitForKeyinfo = false;
		String key = fd.getEntityName();
		if (fd.getRelationName() != null)
		{
			int rid = entities.getRelationID(fd.getRelationName());
			if (rid > 0)
			{
				key = String.valueOf(rid);
				if (fd.getWhereArgsHash() != null) //if global/constant relation at server we did omit the argshash
				{
					omitForKeyinfo = true;
					String hash = fd.getWhereArgsHash();
					hash = replaceUUIDHash(hash);
					key += '|' + hash;
				}
			}
		}
		localStorage.setItem(key, fd.toJSON(omitForKeyinfo));
	}

	private String replaceUUIDHash(String hash)
	{
		if (!hash.startsWith("36.") && !hash.contains(";36.")) return hash;

		StringBuilder retval = new StringBuilder();
		String[] parts = hash.split(";");
		for (String part : parts)
		{
			if (part.startsWith("36."))
			{
				//replace
				retval.append(Utils.createPKHashKey(new Object[] { valueStore.putUUID(part.substring(3)) }));
			}
			else
			{
				//put back
				retval.append(part);
				retval.append(';');
			}
		}
		return retval.toString();
	}

	void storeRowData(String entityName, JsArray<RowDescription> rowData)
	{
		String[] uuidCols = entities.getUUIDDataProviderNames(entityName);

		ArrayList<RowDescription> list = new ArrayList<RowDescription>();
		for (int i = 0; i < rowData.length(); i++)
		{
			RowDescription row = rowData.get(i);

			//replace UUID to save local storage space
			if (uuidCols != null)
			{
				for (String dataProviderID : uuidCols)
				{
					String val = (String)row.getValue(dataProviderID);
					if (val != null) row.setValueInternal(dataProviderID, String.valueOf(valueStore.putUUID(val)));
				}
			}

			list.add(row);
		}
		storeRowData(entityName, list, false);
	}

	void storeRowData(String entityName, ArrayList<RowDescription> rowData, boolean local)
	{
		DataproviderIdAndTypeHolder dataProviderID = entities.getPKDataProviderID(entityName);
		if (dataProviderID == null) throw new IllegalStateException(application.getMessages().cannotWorkWithoutPK());

		int oldSize = changes.size();

		//store data in offline db
		for (RowDescription row : rowData)
		{
			Object pk = row.getValue(dataProviderID.getDataproviderId(), dataProviderID.getType());
			String key = entityName + '|' + pk;
			if (local)
			{
				if (!changes.contains(key)) changes.add(key);
			}
			localStorage.setItem(key, row.toJSONArray(entities.getDataProviders(entityName)));
		}

		if (changes.size() != oldSize)
		{
			updateChangesInLocalStorage();
		}
	}

	void deleteRowData(String entityName, RowDescription rowData, boolean createdOnClient)
	{
		DataproviderIdAndTypeHolder dataProviderID = entities.getPKDataProviderID(entityName);
		if (dataProviderID == null) throw new IllegalStateException(application.getMessages().cannotWorkWithoutPK());

		Object pk = rowData.getValue(dataProviderID.getDataproviderId(), dataProviderID.getType());
		String key = entityName + '|' + pk;

		if (localStorage.getItem(key) != null)
		{
			localStorage.removeItem(key);

			if (!createdOnClient)
			{
				if (!deletes.contains(key)) deletes.add(key);

				updateDeletesInLocalStorage();
			}
		}

		HashSet<FoundSet> set = entitiesToFoundsets.get(entityName);
		if (set != null)
		{
			for (FoundSet foundset : set)
			{
				foundset.removeRecord(pk);
			}
		}

		if (changes.contains(key))
		{
			changes.remove(key);
			updateChangesInLocalStorage();
		}

	}

	public void recordPushedToServer(String entityName, String pk)
	{
		HashSet<FoundSet> set = entitiesToFoundsets.get(entityName);
		if (set != null)
		{
			for (FoundSet foundset : set)
			{
				foundset.recordPushedToServer(pk);
			}
		}

	}

	private void removeDeletedRecords(FoundSetDescription desc)
	{
		if (deletes != null && deletes.size() > 0)
		{
			for (String deletedRecord : deletes)
			{
				int idx = deletedRecord.indexOf('|');
				String entityName = deletedRecord.substring(0, idx);
				if (entityName.equals(desc.getEntityName()))
				{
					String pk = deletedRecord.substring(idx + 1);
					JsArray<RecordDescription> recs = desc.getRecords();
					for (int i = recs.length() - 1; i >= 0; i--)
					{
						RecordDescription rec = recs.get(i);
						if (rec.getPK() != null && rec.getPK().toString().equals(pk))
						{
							desc.removeRecord(i);
						}
					}
				}
			}
		}
	}

	void updateChangesInLocalStorage()
	{
		updateListInLocalStorage(CHANGES_KEY, changes);
	}

	void updateDeletesInLocalStorage()
	{
		updateListInLocalStorage(DELETES_KEY, deletes);
	}

	private void updateListInLocalStorage(String key, List<String> list)
	{
		JSONArray jsona = new JSONArray();
		for (int i = 0; i < list.size(); i++)
		{
			String item = list.get(i);
			jsona.set(i, new JSONString(item));
		}
		localStorage.setItem(key, jsona.toString());
	}

	public static String getEntityFromDataSource(String dataSource)
	{
		return dataSource.substring(dataSource.lastIndexOf('/') + 1);
	}

	public FoundSet getFoundSet(String entityName)
	{
		//load data from offline db
		String json = localStorage.getItem(entityName);
		if (json == null) return null;
		FoundSetDescription fsd = JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
		removeDeletedRecords(fsd);
		FoundSet foundset = new FoundSet(this, fsd);
		addFoundsetInList(foundset);
		return foundset;
	}

	private void addFoundsetInList(FoundSet foundset)
	{
		HashSet<FoundSet> set = entitiesToFoundsets.get(foundset.getEntityName());
		if (set == null)
		{
			set = new HashSet<FoundSet>();
			entitiesToFoundsets.put(foundset.getEntityName(), set);
		}
		set.add(foundset);
	}

	//delete all local data
	public void clearLocalStorage()
	{
		localStorage.clear();
		entities = null;

		editRecordList = new EditRecordList(this);
		changes = new ArrayList<String>();
		deletes = new ArrayList<String>();
	}

	//from mem/obj to store
	public int saveData()
	{
		return editRecordList.stopEditing(true);
	}

	public EditRecordList getEditRecordList()
	{
		return editRecordList;
	}

	//has changes for the server
	public boolean hasChanges()
	{
		return (changes.size() != 0 || deletes.size() > 0);
	}

	ArrayList<String> getChanges()
	{
		return changes;
	}

	ArrayList<String> getDeletes()
	{
		return deletes;
	}

	public int getFoundSetSize(FoundSet fs)
	{
		if (fs == null) return 0;
		return fs.getSize();
	}

	public MobileClient getApplication()
	{
		return application;
	}

	public static boolean hasRecords(FoundSet fs)
	{
		return (fs != null && fs.getSize() > 0);
	}

	RowDescription createRowDescription(FoundSet fs, Object pkval)
	{
		RowDescription retval = RowDescription.newInstance();
		retval.setValue(entities.getPKDataProviderID(fs.getEntityName()).getDataproviderId(), pkval);
		if (fs instanceof RelatedFoundSet)
		{
			RelatedFoundSet rfs = (RelatedFoundSet)fs;
			String relationName = rfs.getRelationName();
			Record[] parentRecords = rfs.getParents();
			String entityName = parentRecords[0].getParent().getEntityName();
			RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
			if (rd != null)
			{
				JsArrayString pdp = rd.getPrimaryDataProviders();
				JsArrayString fdp = rd.getForeignColumns();
				Object[] coldata = new Object[pdp.length()];
				for (int j = 0; j < coldata.length; j++)
				{
					Object obj = parentRecords[0].getValue(pdp.get(j));
					retval.setValue(fdp.get(j), obj);
				}
			}
		}
		return retval;
	}

	void checkForPKStorage(FoundSet fs)
	{
		FoundSetDescription fd = fs.needToSaveFoundSetDescription();
		if (fd != null)
		{
			storeFoundSetDescription(fd);
		}
	}

	FoundSet createRelatedFoundSet(String relationName, Record record)
	{
		String entityName = record.getParent().getEntityName();
		RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
		if (rd != null)
		{
			JsArrayString pdp = rd.getPrimaryDataProviders();
			Object[] coldata = new Object[pdp.length()];
			for (int j = 0; j < coldata.length; j++)
			{
				Object obj = record.getValue(pdp.get(j));
				if (obj == null) return null;//we cannot relate based on null
				coldata[j] = obj;
			}
			String whereArgsHash = Utils.createPKHashKey(coldata);
			String foreignEntityName = rd.getForeignEntityName();
			FoundSetDescription fsd = FoundSetDescription.newInstance(foreignEntityName, relationName, whereArgsHash);
			JsArray<RecordDescription> rds = fsd.getRecords();
			seek(foreignEntityName, rd.getForeignColumns(), coldata, rds);
			return new RelatedFoundSet(this, record, fsd, relationName);
		}
		return null;
	}

	//seeks trough all data for matching rhs/foreign records and add these to rds arg
	private void seek(String entityName, JsArrayString foreignColumns, Object[] coldata, JsArray<RecordDescription> rds)
	{
		String entityNamePlusPipe = entityName + '|';
		int length = localStorage.getLength();
		for (int j = 0; j < length; j++)
		{
			String key = localStorage.key(j);
			if (key.startsWith(entityNamePlusPipe))
			{
				Object pk = key.substring(entityNamePlusPipe.length());
				RowDescription rowd = getRowDescription(entityName, pk);
				boolean foundMatch = false;
				for (int i = 0; i < coldata.length; i++)
				{
					if (coldata[i].equals(rowd.getValue(foreignColumns.get(i))))
					{
						foundMatch = true;
					}
					else
					{
						foundMatch = false;
						break;
					}
				}
				if (foundMatch)
				{
					RecordDescription rd = RecordDescription.newInstance(pk);
					rds.push(rd);
				}
			}
		}
	}

	ArrayList<String> getAllPrimaryRelationNames(String entityName)
	{
		ArrayList<String> retval = new ArrayList<String>();
		EntityDescription ed = entities.getDescription(entityName);
		if (ed != null)
		{
			JsArray<RelationDescription> rels = ed.getPrimaryRelations();
			if (rels != null)
			{
				for (int i = 0; i < rels.length(); i++)
				{
					RelationDescription rd = rels.get(i);
					retval.add(rd.getName());
				}
			}
		}
		return retval;
	}

	String getRemotePK(String entityName, String pk, RowDescription row)
	{
		if (entities.isPKUUID(entityName) || row.isCreatedOnDevice())
		{
			pk = valueStore.getUUIDValue(Utils.getAsInteger(pk));
		}
		return pk;
	}

	String toRemoteJSON(String entityName, RowDescription row)
	{
		String[] uuidCols = entities.getUUIDDataProviderNames(entityName);

		RowDescription clone = row.cloneRowDescription();
		if (uuidCols != null)
		{
			for (String dataProviderID : uuidCols)
			{
				Object val = clone.getValue(dataProviderID);
				if (val != null) clone.setValueInternal(dataProviderID, valueStore.getUUIDValue(Utils.getAsInteger(val)));
			}
		}

		return clone.toJSONObject();
	}

	int getRelationID(String relationName)
	{
		return entities.getRelationID(relationName);
	}

	String getNewPrimaryKey()
	{
		String uuid = Utils.createStringUUID();
		return String.valueOf(valueStore.putUUID(uuid));
	}

	/**
	 * @param b
	 * @return
	 */
	public boolean setAutoSave(boolean b)
	{
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @return
	 */
	public boolean getAutoSave()
	{
		// TODO Auto-generated method stub
		return true;
	}


	public Map<String, Integer> exportColumns(String entityName, Scope scope, Object javascriptObject)
	{
		Map<String, Integer> variableTypes = new HashMap<String, Integer>();
		EntityDescription entityDescription = getEntityDescription(entityName);
		if (entityDescription != null)
		{
			// export all dataproviders
			Set<String> exported = new HashSet<String>();
			JsArray<DataProviderDescription> dataProviders = entityDescription.getDataProviders();
			for (int i = 0; i < dataProviders.length(); i++)
			{
				DataProviderDescription dp = dataProviders.get(i);
				variableTypes.put(dp.getName(), Integer.valueOf(dp.getType()));
				if (exported.add(dp.getName())) scope.exportProperty(javascriptObject, dp.getName());
			}
			JsArray<RelationDescription> primaryRelations = entityDescription.getPrimaryRelations();
			// export all relations
			for (int i = 0; i < primaryRelations.length(); i++)
			{
				String name = primaryRelations.get(i).getName();
				if (exported.add(name)) scope.exportProperty(javascriptObject, name);
			}
		}
		return variableTypes;
	}

}
