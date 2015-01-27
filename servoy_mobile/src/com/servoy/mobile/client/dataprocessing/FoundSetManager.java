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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
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
import com.servoy.mobile.client.persistence.Relation;
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
	public static final String LITERAL_PREFIX = "LITERAL:"; //$NON-NLS-1$
	private static final String CHANGES_KEY = "_svy_changes";
	private static final String DELETES_KEY = "_svy_deletes";
	private static final String CLIENT_DELETES_KEY = "_svy_client_deletes";
	private static final String NEW_RECORDS_KEY = "_svy_client_new_records";
	private static final String ENTITY_PREFIX_KEY = "_svy_entityPrefix";
	private static final String ENTITIES_KEY = "_svy_entities";
	private static final String STORAGE_VERSION_KEY = "_svy_storage_version";
	private static final String SERVER_DATA_KEY = "|srv";
	private static final String USER_PROPERTY_PREFIX = "_svy_user_property_";

	private static final int STORAGE_VERSION = 1;

	private final Storage localStorage = Storage.getLocalStorageIfSupported();
	private final ValueStore valueStore = new ValueStore(localStorage);
	private final MobileClient application;
	private EditRecordList editRecordList;
	private Map<String, List<String>> entityNameToPK = new HashMap<String, List<String>>();

	//fields mapped to local storage
	private Entities entities;
	private String entityPrefix;
	private final int storage_version;
	private ArrayList<String> changes;
	// deletes of records that exist server side
	private ArrayList<String> serverRecordsDeletes;
	// deletes of records that were created client side, then deleted without existing on server;
	// we have to keep track of these also as not all references to these records are deleted, we filter them out at foundset creation
	private ArrayList<String> clientDeletes;

	// list of records created on client, we need to keep this because not all foundset description are modified at save
	private ArrayList<String> newRecords;

	// entityName->foundset
	HashMap<String, FoundSet> sharedFoundsets;
	//entityName->relationName|foundsetHash->foundset
	HashMap<String, HashMap<String, FoundSet>> relatedFoundsets;
	// entityName->foundsets, not shared or separate, created via databaseManager.getFoundset
	HashMap<String, HashSet<FoundSet>> foundsets;

	HashMap<String, RowDescription> keyToRowDescription;

	public FoundSetManager(MobileClient mc)
	{
		application = mc;
		editRecordList = new EditRecordList(this);

		//check storage version, if not 1 do clear storage
		storage_version = Utils.getAsInteger(localStorage.getItem(STORAGE_VERSION_KEY));
		if (storage_version != STORAGE_VERSION)
		{
			clearLocalStorage();
		}

		loadEntitiesInMemory(null);

		changes = new ArrayList<String>();
		addItems(localStorage.getItem(CHANGES_KEY), changes);

		serverRecordsDeletes = new ArrayList<String>();
		addItems(localStorage.getItem(DELETES_KEY), serverRecordsDeletes);

		clientDeletes = new ArrayList<String>();
		addItems(localStorage.getItem(CLIENT_DELETES_KEY), clientDeletes);

		newRecords = new ArrayList<String>();
		addItems(localStorage.getItem(NEW_RECORDS_KEY), newRecords);

		sharedFoundsets = new HashMap<String, FoundSet>();
		relatedFoundsets = new HashMap<String, HashMap<String, FoundSet>>();
		foundsets = new HashMap<String, HashSet<FoundSet>>();
		keyToRowDescription = new HashMap<String, RowDescription>();
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
			clearDataproviders();
			Set<String> exported = new HashSet<String>();
			JsArray<EntityDescription> eds = entities.getEntityDescriptions();
			for (int i = 0; i < eds.length(); i++)
			{
				EntityDescription ed = eds.get(i);
				JsArray<DataProviderDescription> dataProviders = ed.getDataProviders();
				for (int k = 0; k < dataProviders.length(); k++)
				{
					String name = dataProviders.get(k).getDataProviderID();
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

	private void export(String name)
	{
		try
		{
			exportImpl(name);
		}
		catch (Exception e)
		{
			Log.error("cannot export property/dataprovider: " + name + " on the window object", e);
		}
	}

	private native void exportImpl(String name)
	/*-{
		$wnd._ServoyUtils_.defineWindowVariable(name, false);
	}-*/;

	private native void clearDataproviders()
	/*-{
		$wnd._ServoyUtils_.clearWindowVariables();
	}-*/;

	public EntityDescription getEntityDescription(String entityName)
	{
		if (entities != null)
		{
			return entities.getDescription(entityName);
		}
		return null;
	}

	public EntityDescription getRelatedEntityDescription(String entityStartName, String relationString)
	{
		String[] relations = relationString.split("\\.");
		String entityName = entityStartName;
		for (String relationName : relations)
		{
			EntityDescription entityDescription = getEntityDescription(entityName);
			entityName = null;
			if (entityDescription != null)
			{
				RelationDescription primaryRelation = entityDescription.getPrimaryRelation(relationName);
				if (primaryRelation != null)
				{
					entityName = primaryRelation.getForeignEntityName();
				}
			}
			if (entityName == null) return null;
		}
		return getEntityDescription(entityName);
	}

	/**
	 * Returns the cached, in memory, row description, that is shared among more instances of same db record.
	 */
	RowDescription getRowDescription(String entityName, Object pk)
	{
		String key = entityName + '|' + pk;
		if (keyToRowDescription.containsKey(key))
		{
			return keyToRowDescription.get(key);
		}

		RowDescription rd = getLocalStorageRowDescription(entityName, pk);
		if (rd != null)
		{
			keyToRowDescription.put(key, rd);
		}
		return rd;
	}

	void flushRowDescription(String entityName, Object pk)
	{
		String key = entityName + '|' + pk;
		getEditRecordList().removeEditedRecord(keyToRowDescription.get(key));
		keyToRowDescription.remove(key);
		entityNameToPK.remove(entityName);
	}

	/**
	 * Returns a new instance for local storage row description (saved data).
	 */
	RowDescription getLocalStorageRowDescription(String entityName, Object pk)
	{
		String json = localStorage.getItem(entityName + '|' + pk);
		if (json == null || entities == null) return null;

		JSONArray values = JSONParser.parseStrict(json).isArray();
		String[] dataProviders = entities.getDataProviders(entityName);
		return RowDescription.newInstance(dataProviders, values);
	}

	FoundSet getRelatedFoundSet(Record rec, String entityName, String relationName, String key)
	{
		if (entities == null) return null;
		if (key == null)
		{
			int relationID = getRelationID(relationName);
			RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
			if (rd != null)
			{
				JsArrayString pdp = rd.getPrimaryDataProviders();
				if (pdp.length() > 0)
				{
					Object[] coldata = new Object[pdp.length()];
					for (int j = 0; j < coldata.length; j++)
					{
						Object obj = getRecordValue(rec, pdp.get(j));
						if (obj == null) return null;
						coldata[j] = obj;
					}
					key = relationID + "|" + com.servoy.base.util.Utils.createPKHashKey(coldata);
				}
			}
		}
		if (key == null)
		{
			return null;
		}
		RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);

		if (rd != null)
		{
			HashMap<String, FoundSet> relatedEntityFoundsets = relatedFoundsets.get(rd.getForeignEntityName());
			if (relatedEntityFoundsets != null)
			{
				FoundSet fs = relatedEntityFoundsets.get(key);
				if (fs != null)
				{
					fs.touch();
					return fs;
				}
			}
		}
		//load data from offline db
		String json = localStorage.getItem(key);
		if (json == null)
		{
			return createRelatedFoundSet(relationName, rec);
		}

		FoundSetDescription fsd = JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
		if (fsd.needsInfoFromKey()) fsd.setInfoFromKey(key, relationName, rd.getForeignEntityName());
		removeDeletedRecords(fsd);
		addNewRecords(fsd, rec, rd.getForeignEntityName(), relationName);
		FoundSet foundset = new RelatedFoundSet(this, rec, fsd, relationName);
		addFoundsetInList(foundset, key, false);
		return foundset;
	}


	public void flushRelatedFoundsets()
	{
		for (HashMap<String, FoundSet> map : relatedFoundsets.values())
		{
			if (map != null)
			{
				for (FoundSet f : map.values())
				{
					f.flushIfPossible();
				}
			}
		}
	}

	String getEntityPrefix()
	{
		return (entityPrefix == null ? "" : entityPrefix);
	}

	void storeOfflineData(OfflineDataProxy offlineDataProxy, String fsname, OfflineDataDescription offlineData)
	{
		//first clear existing stuff if there is any
		clearLocalData();

		HashMap<String, HashSet<Object>> entitiesToPKs = new HashMap<String, HashSet<Object>>();

		//store data in offline db
		entities = new Entities(offlineData.getEntities(), valueStore);
		// first sync up Relations -> RelationDescription
		for (int i = 0; i < application.getFlattenedSolution().relationCount(); i++)
		{
			Relation relation = application.getFlattenedSolution().getRelation(i);
			String primaryString = FoundSetManager.getEntityFromDataSource(relation.getPrimaryDataSource());
			String foreignString = FoundSetManager.getEntityFromDataSource(relation.getForeignDataSource());
			EntityDescription primary = getEntityDescription(primaryString);
			EntityDescription foreign = getEntityDescription(foreignString);

			if (primary != null && foreign != null)
			{
				RelationDescription primaryRelation = primary.getPrimaryRelation(relation.getName());
				if (primaryRelation == null)
				{
					primaryRelation = RelationDescription.newInstance(relation.getName(), primaryString, foreignString);
					primary.addPrimaryRelation(primaryRelation);
				}
				primaryRelation.setSelfRef(relation.isSelfRef());
			}
			else
			{
				Log.error("A relation was defined: " + relation.getName() + " on primary: " + relation.getPrimaryDataSource() + " and foreign: " +
					relation.getForeignDataSource() + " where no Entities/Tables where given by the service solution");
			}
		}
		localStorage.setItem(ENTITIES_KEY, entities.toJSONArray());

		entityPrefix = offlineData.getEntityPrefix();
		initLocalStorage(entities.toJSONArray(), entityPrefix);
		if (entityPrefix != null) localStorage.setItem(ENTITY_PREFIX_KEY, entityPrefix);

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
						rd.setPK(newPK);
					}
					//replace relation names with relation ids, to save local storage space
					JsArrayString rfs = rd.getRFS();
					for (int k = 0; k < rfs.length(); k++)
					{
						String key = rfs.get(k);
						int idx = key.indexOf('|');
						int id = entities.getRelationID(key.substring(0, idx));
						String hash = key.substring(idx + 1);
						hash = replaceUUIDHash(hash, false);
						rfs.set(k, id + "|" + hash);
					}
				}

				//store data in offline db
				storeFoundSetDescription(fd, false, null);
			}
		}

		exportDataproviders();

		//initiate load of all row data
		offlineDataProxy.requestRowData(entitiesToPKs, false, null);
	}

	private void initLocalStorage(String entitiesJSON, String entityPrefixArg)
	{
		localStorage.setItem(STORAGE_VERSION_KEY, String.valueOf(STORAGE_VERSION));
	}

	/**
	 * @param valueStore null if it is already build in storage, or the value store object if the valueStore is not yet initialized/persisted.
	 */
	private void loadEntitiesInMemory(ValueStore valueStoreArg)
	{
		String ejson = localStorage.getItem(ENTITIES_KEY);
		if (ejson != null)
		{
			JSONArray ea = JSONParser.parseStrict(ejson).isArray();
			if (ea != null)
			{
				JsArray<EntityDescription> e = ea.getJavaScriptObject().cast();
				entities = new Entities(e, valueStoreArg);
			}
		}

		entityPrefix = localStorage.getItem(ENTITY_PREFIX_KEY);

		exportDataproviders();
	}

	private void storeFoundSetDescription(FoundSetDescription fd, boolean updateMode, ArrayList<FoundSet> contentChangedFoundSets)
	{
		boolean omitForKeyinfo = false;
		String key = fd.getEntityName();
		entityNameToPK.remove(key);
		if (fd.getRelationName() != null && entities != null)
		{
			int rid = entities.getRelationID(fd.getRelationName());
			if (rid != 0)
			{
				key = String.valueOf(rid);
				if (fd.getWhereArgsHash() != null) //if global/constant relation at server we did omit the argshash
				{
					omitForKeyinfo = true;
					String hash = fd.getWhereArgsHash();
					hash = replaceUUIDHash(hash, updateMode);
					key += '|' + hash;
				}
			}
		}
		if (updateMode)
		{
			String item = localStorage.getItem(key);
			if (item != null)
			{
				boolean changed = false;
				FoundSetDescription currentFSD = JSONParser.parseStrict(item).isObject().getJavaScriptObject().cast();

				if (fd.getRelationName() == null)
				{
					// update new records into the current set.
					JsArray<RecordDescription> currentSet = currentFSD.getRecords();
					JsArray<RecordDescription> newSet = fd.getRecords();
					int length = currentSet.length();
					outer : for (int i = 0; i < newSet.length(); i++)
					{
						RecordDescription recordDescription = newSet.get(i);
						Object pk = recordDescription.getPK();
						for (int j = 0; j < length; j++)
						{
							if (Utils.equalObjects(currentSet.get(j).getPK(), pk))
							{
								continue outer;
							}
						}
						currentSet.set(currentSet.length(), recordDescription);
						changed = true;
					}
					if (changed)
					{
						localStorage.setItem(key, currentFSD.toJSON(omitForKeyinfo));

						HashSet<FoundSet> set = foundsets.get(key);
						if (set != null)
						{
							for (FoundSet foundSet : set)
							{
								foundSet.updateFoundSetDescription(currentFSD);
								if (contentChangedFoundSets != null && contentChangedFoundSets.indexOf(foundSet) == -1) contentChangedFoundSets.add(foundSet);
							}
						}
						FoundSet foundSet = sharedFoundsets.get(key);
						if (foundSet != null)
						{
							foundSet.updateFoundSetDescription(currentFSD);
							if (contentChangedFoundSets != null && contentChangedFoundSets.indexOf(foundSet) == -1) contentChangedFoundSets.add(foundSet);
						}
					}
					return;
				}
				else
				{
					// related fsd that are coming in are just leading only make sure that the new records are kept in the FSD description.
					JsArray<RecordDescription> records = currentFSD.getRecords();
					for (int i = 0; i < records.length(); i++)
					{
						Object pk = records.get(i).getPK();
						if (newRecords.contains(currentFSD.getEntityName() + "|" + pk))
						{
							fd.getRecords().set(fd.getRecords().length(), records.get(i));
							changed = true;
						}
					}

					HashMap<String, FoundSet> map = relatedFoundsets.get(fd.getEntityName());
					if (map != null)
					{
						FoundSet foundSet = map.get(key);
						if (foundSet != null)
						{
							foundSet.updateFoundSetDescription(fd);
							if (contentChangedFoundSets != null && contentChangedFoundSets.indexOf(foundSet) == -1) contentChangedFoundSets.add(foundSet);
						}
					}
				}
			}
		}
		localStorage.setItem(key, fd.toJSON(omitForKeyinfo));
	}

	private String replaceUUIDHash(String hash, boolean updateMode)
	{
		if (!hash.startsWith("36.") && !hash.contains(";36.")) return hash;

		StringBuilder retval = new StringBuilder();
		String[] parts = hash.split(";");
		for (String part : parts)
		{
			if (part.startsWith("36."))
			{
				//replace
				retval.append(com.servoy.base.util.Utils.createPKHashKey(new Object[] { updateMode ? valueStore.getOrPutUUID(part.substring(3))
					: valueStore.putUUID(part.substring(3)) }));
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

	void storeServerRowDataBeforeChange(String entityName, RowDescription row)
	{
		if (!row.isCreatedOnDevice())
		{
			String key = getRowDataPkAndKey(entityName, row)[1] + SERVER_DATA_KEY;
			if (localStorage.getItem(key) == null)
			{
				localStorage.setItem(key, row.toJSONArray(entities.getDataProviders(entityName)));
			}
		}
	}

	void storeRowData(String entityName, JsArray<RowDescription> rowData, boolean updateMode, String pkString)
	{
		String[] uuidCols = entities != null ? entities.getUUIDDataProviderNames(entityName) : null;

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
					if (val != null) row.setValueInternal(dataProviderID, String.valueOf(updateMode ? valueStore.getOrPutUUID(val) : valueStore.putUUID(val)));
				}
			}

			list.add(row);
		}
		storeRowData(entityName, list, false, updateMode);

		String[] pks = pkString.split(",");
		//if the number of received rows is smaller than requested, it means data was removed during sync
		if (rowData.length() < pks.length) removeNotReceivedRecords(entityName, rowData, pks);

	}

	void storeRowData(String entityName, ArrayList<RowDescription> rowData, boolean local, boolean updateMode)
	{
		int oldSize = changes.size();
		entityNameToPK.remove(entityName);
		//store data in offline db
		for (RowDescription row : rowData)
		{
			String key = getRowDataPkAndKey(entityName, row)[1];
			if (local)
			{
				if (!row.isCreatedOnDevice())
				{
					// first remove this key, so that the "modification timestamp" is updated.
					changes.remove(key);
					changes.add(key);
				}
				else if (!changes.contains(key))
				{
					// its  new record and the changes list doesn't contain it yet so add it.
					changes.add(key);
				}
			}
			// if update mode then only update the row if it is not in the changes list.
			if (!updateMode || !changes.contains(key))
			{
				localStorage.setItem(key, row.toJSONArray(entities.getDataProviders(entityName)));
			}
		}

		if (changes.size() != oldSize)
		{
			updateChangesInLocalStorage();
		}
	}

	void deleteRowData(String entityName, RowDescription rowData, boolean createdOnClient)
	{
		String[] rowInfo = getRowDataPkAndKey(entityName, rowData);
		String key = rowInfo[1];

		if (localStorage.getItem(key) != null)
		{
			localStorage.removeItem(key);

			if (!createdOnClient)
			{
				if (!serverRecordsDeletes.contains(key)) serverRecordsDeletes.add(key);

				updateDeletesInLocalStorage();
			}
			else
			{
				if (!clientDeletes.contains(key)) clientDeletes.add(key);

				updateListInLocalStorage(CLIENT_DELETES_KEY, clientDeletes);
			}
		}
		HashSet<FoundSet> set = getCreatedFoundsets(entityName);
		for (FoundSet foundset : set)
		{
			foundset.removeRecord(rowInfo[0]);
		}

		if (changes.contains(key))
		{
			changes.remove(key);
			updateChangesInLocalStorage();
		}

		if (newRecords.contains(key))
		{
			newRecords.remove(key);
			updateListInLocalStorage(NEW_RECORDS_KEY, newRecords);
		}

		getEditRecordList().removeEditedRecord(keyToRowDescription.get(key));
		keyToRowDescription.remove(key);
		entityNameToPK.remove(entityName);

	}

	void checkForNewRecord(String entityName, RowDescription rowData)
	{
		String[] rowInfo = getRowDataPkAndKey(entityName, rowData);
		String key = rowInfo[1];
		if (localStorage.getItem(key) == null)
		{
			HashSet<FoundSet> set = getCreatedFoundsets(entityName);
			if (set != null)
			{
				for (FoundSet foundset : set)
				{
					boolean addRecord = true;
					if (foundset.getRecordByPk(rowInfo[0]) != null)
					{
						continue;
					}
					if (foundset instanceof RelatedFoundSet)
					{
						addRecord = matchesRelatedFoundset(((RelatedFoundSet)foundset).getParents()[0], ((RelatedFoundSet)foundset).getRelationName(), rowData);
					}
					if (addRecord)
					{
						foundset.addRecord(rowInfo[0], rowData);
					}
				}
			}

			if (!newRecords.contains(key))
			{
				newRecords.add(key);
				updateListInLocalStorage(NEW_RECORDS_KEY, newRecords);
			}
		}
	}

	private String[] getRowDataPkAndKey(String entityName, RowDescription rowData)
	{
		if (entities == null) throw new IllegalStateException("Cannot get record when there is no data in local storage");

		DataproviderIdAndTypeHolder dataProviderID = entities.getPKDataProviderID(entityName);
		if (dataProviderID == null) throw new IllegalStateException(application.getI18nMessageWithFallback("cannotWorkWithoutPK"));

//		Object pk = rowData.getValue(dataProviderID.getDataproviderId()); // uncomment for GWT debug mode (closer to what RecordDescription.getPK() does so that toString for both will match)
		Object pk = rowData.getValue(dataProviderID.getDataproviderId(), dataProviderID.getType()); // comment out for GWT debug mode (otherwise pk_keys are stored as "entity_1234" but accessed as "entity_1234.0" => doesn't work); maybe we could always use the line above instead of this
		return new String[] { pk.toString(), entityName + '|' + pk };

	}

	String getPKDataProviderID(String entityName)
	{
		if (entities != null)
		{
			DataproviderIdAndTypeHolder dataProviderID = entities.getPKDataProviderID(entityName);
			if (dataProviderID != null)
			{
				return dataProviderID.getDataproviderId();
			}
		}
		return null;
	}

	public void recordPushedToServer(String entityName, String pk)
	{
		HashSet<FoundSet> set = getCreatedFoundsets(entityName);
		if (set != null)
		{
			for (FoundSet foundset : set)
			{
				foundset.recordPushedToServer(pk);
			}
		}

	}

	private HashSet<FoundSet> getCreatedFoundsets(String entityName)
	{
		HashSet<FoundSet> set = new HashSet<FoundSet>();
		if (sharedFoundsets.containsKey(entityName))
		{
			set.add(sharedFoundsets.get(entityName));
		}
		if (foundsets.containsKey(entityName))
		{
			set.addAll(foundsets.get(entityName));
		}
		if (relatedFoundsets.containsKey(entityName))
		{
			set.addAll(relatedFoundsets.get(entityName).values());
		}
		return set;
	}

	private void addNewRecords(FoundSetDescription fsd, Record parentRecord, String entityName, String relationName)
	{
		for (String newRecord : newRecords)
		{
			int idx = newRecord.indexOf('|');
			String recordEntity = newRecord.substring(0, idx);
			if (entityName.equals(recordEntity))
			{
				Object pk = newRecord.substring(idx + 1);
				RowDescription rowd = getRowDescription(entityName, pk);
				if ((rowd != null) && ((relationName == null) || matchesRelatedFoundset(parentRecord, relationName, rowd)))
				{
					boolean foundRecord = false;
					JsArray<RecordDescription> recs = fsd.getRecords();
					for (int i = 0; i < recs.length(); i++)
					{
						RecordDescription rec = recs.get(i);
						if (rec.getPK() != null && rec.getPK().toString().equals(pk))
						{
							foundRecord = true;
						}
					}
					if (!foundRecord)
					{
						RecordDescription rd = RecordDescription.newInstance(pk);
						fsd.getRecords().push(rd);
					}
				}
			}
		}
	}

	private boolean matchesRelatedFoundset(Record parentRecord, String relationName, RowDescription rowData)
	{
		String entityName = parentRecord.getParent().getEntityName();
		RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
		if (rd != null)
		{
			JsArrayString pdp = rd.getPrimaryDataProviders();
			if (pdp.length() > 0)
			{
				Object[] coldata = new Object[pdp.length()];
				for (int j = 0; j < coldata.length; j++)
				{
					Object obj = getRecordValue(parentRecord, pdp.get(j));
					if (obj == null) return false;
					coldata[j] = obj;
				}
				boolean foundMatch = false;
				for (int i = 0; i < coldata.length; i++)
				{
					if (coldata[i].equals(rowData.getValue(rd.getForeignColumns().get(i))))
					{
						foundMatch = true;
					}
					else
					{
						foundMatch = false;
						break;
					}
				}
				return foundMatch;
			}
		}
		return false;
	}

	private void removeDeletedRecords(FoundSetDescription desc)
	{
		List<String> allDeletes = new ArrayList<String>();
		if (serverRecordsDeletes != null)
		{
			allDeletes.addAll(serverRecordsDeletes);
		}
		if (clientDeletes != null)
		{
			allDeletes.addAll(clientDeletes);
		}

		for (String deletedRecord : allDeletes)
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

	void updateChangesInLocalStorage()
	{
		updateListInLocalStorage(CHANGES_KEY, changes);
	}

	void updateDeletesInLocalStorage()
	{
		updateListInLocalStorage(DELETES_KEY, serverRecordsDeletes);
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
		int nameBeginIdx = dataSource.lastIndexOf('/');
		if (nameBeginIdx < 0) nameBeginIdx = dataSource.lastIndexOf(':'); // in memory datasource
		return dataSource.substring(nameBeginIdx + 1);
	}

	public FoundSet getFoundSet(String entityName, boolean shared)
	{
		if (shared && sharedFoundsets.containsKey(entityName)) return sharedFoundsets.get(entityName);

		//load data from offline db
		String json = localStorage.getItem(entityName);
		if (json == null) return null;
		FoundSetDescription fsd = JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
		removeDeletedRecords(fsd);
		addNewRecords(fsd, null, entityName, null);
		FoundSet foundset = new FoundSet(this, fsd);
		addFoundsetInList(foundset, null, shared);
		return foundset;
	}

	private void addFoundsetInList(FoundSet foundset, String relatedKey, boolean shared)
	{
		if (shared)
		{
			sharedFoundsets.put(foundset.getEntityName(), foundset);
		}
		else if (relatedKey != null)
		{
			HashMap<String, FoundSet> map = relatedFoundsets.get(foundset.getEntityName());
			if (map == null)
			{
				map = new HashMap<String, FoundSet>();
				relatedFoundsets.put(foundset.getEntityName(), map);
			}
			map.put(relatedKey, foundset);
		}
		else
		{
			for (HashSet<FoundSet> set : foundsets.values())
			{
				if (set != null)
				{
					for (FoundSet f : set)
					{
						f.flushIfPossible();
					}
				}
			}
			HashSet<FoundSet> set = foundsets.get(foundset.getEntityName());
			if (set == null)
			{
				set = new HashSet<FoundSet>();
				foundsets.put(foundset.getEntityName(), set);
			}
			set.add(foundset);
		}

	}

	//delete all local data
	public void clearLocalData()
	{
		clearLocalStorage();
		entities = null;

		editRecordList = new EditRecordList(this);
		changes = new ArrayList<String>();
		serverRecordsDeletes = new ArrayList<String>();
		clientDeletes = new ArrayList<String>();
		newRecords = new ArrayList<String>();
		entityNameToPK = new HashMap<String, List<String>>();

		foundsets.clear();
		relatedFoundsets.clear();
		sharedFoundsets.clear();
		keyToRowDescription.clear();
		valueStore.clearCache();

		application.getFormManager().getHistory().clear();
	}

	private void clearLocalStorage()
	{
		// do keep the credentials if set.
		String[] credentials = getCredentials();
		// keep all user properties
		Map<String, String> userProperties = new HashMap<String, String>();
		for (int i = 0; i < localStorage.getLength(); i++)
		{
			String key = localStorage.key(i);
			if (key.startsWith(USER_PROPERTY_PREFIX))
			{
				userProperties.put(key, localStorage.getItem(key));
			}
		}
		// now clear the local store
		localStorage.clear();
		// restore credentials
		if (credentials != null) storeCredentials(credentials[0], credentials[1]);
		// restore user properties
		for (Entry<String, String> entry : userProperties.entrySet())
		{
			localStorage.setItem(entry.getKey(), entry.getValue());
		}
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
		return (changes.size() != 0 || serverRecordsDeletes.size() > 0);
	}

	ArrayList<String> getChanges()
	{
		return changes;
	}

	ArrayList<String> getDeletes()
	{
		return serverRecordsDeletes;
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
		if (entities == null) throw new IllegalStateException("Cannot create a new record when there is no data in local storage");

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
					Object obj = getRecordValue(parentRecords[0], pdp.get(j));
					retval.setValue(fdp.get(j), obj);
				}
			}
		}
		keyToRowDescription.put(fs.getEntityName() + '|' + pkval, retval);
		entityNameToPK.remove(fs.getEntityName());
		return retval;
	}

	void checkForPKStorage(FoundSet fs)
	{
		FoundSetDescription fd = fs.needToSaveFoundSetDescription();
		if (fd != null)
		{
			storeFoundSetDescription(fd, false, null);
		}
	}

	private FoundSet createRelatedFoundSet(String relationName, Record record)
	{
		if (entities == null) return null;
		String entityName = record.getParent().getEntityName();
		RelationDescription rd = entities.getPrimaryRelation(entityName, relationName);
		if (rd != null)
		{
			JsArrayString pdp = rd.getPrimaryDataProviders();
			if (pdp.length() == 0)
			{
				Log.info("relation: " + relationName + " is asked for but had no primarydataproviders, entity: " + entityName);
				return null;
			}
			Object[] coldata = new Object[pdp.length()];
			for (int j = 0; j < coldata.length; j++)
			{
				Object obj = getRecordValue(record, pdp.get(j));
				if (obj == null) return null;//we cannot relate based on null
				coldata[j] = obj;
			}
			String whereArgsHash = com.servoy.base.util.Utils.createPKHashKey(coldata);
			String foreignEntityName = rd.getForeignEntityName();
			FoundSetDescription fsd = FoundSetDescription.newInstance(foreignEntityName, relationName, whereArgsHash);
			JsArray<RecordDescription> rds = fsd.getRecords();
			seek(foreignEntityName, rd.getForeignColumns(), coldata, rds);
			removeDeletedRecords(fsd);
			FoundSet foundset = new RelatedFoundSet(this, record, fsd, relationName);
			addFoundsetInList(foundset, getRelationID(relationName) + "|" + whereArgsHash, false);
			return foundset;
		}
		else
		{
			Log.info("relation: " + relationName + " is asked for but no RelationDescription found for it with entity: " + entityName);
		}
		return null;
	}

	//seeks trough all data for matching rhs/foreign records and add these to rds arg
	private void seek(String entityName, JsArrayString foreignColumns, Object[] coldata, JsArray<RecordDescription> rds)
	{
		if (entityNameToPK.containsKey(entityName))
		{
			List<String> pks = entityNameToPK.get(entityName);
			if (pks != null && pks.size() > 0)
			{
				for (String pk : pks)
				{
					checkRecord(pk, entityName, foreignColumns, coldata, rds, false);
				}
			}
			return;
		}
		// we rely on alphabetical order of local storage keys
		String entityNamePlusPipe = entityName + '|';
		int length = localStorage.getLength();
		int index = getKeyIndex(entityNamePlusPipe, 0, length - 1);
		if (index >= 0)
		{
			List<String> pks = new ArrayList<String>();
			for (int j = index; j >= 0; j--)
			{
				String key = localStorage.key(j);
				if (key.startsWith(entityNamePlusPipe))
				{
					String pk = key.substring(entityNamePlusPipe.length());
					pks.add(0, pk);
					checkRecord(pk, entityName, foreignColumns, coldata, rds, true);
				}
				else break;
			}

			for (int j = index + 1; j < length; j++)
			{
				String key = localStorage.key(j);
				if (key.startsWith(entityNamePlusPipe))
				{
					String pk = key.substring(entityNamePlusPipe.length());
					pks.add(pk);
					checkRecord(pk, entityName, foreignColumns, coldata, rds, false);
				}
				else break;
			}
			entityNameToPK.put(entityName, pks);
		}
	}

	private int getKeyIndex(String entityName, int start, int end)
	{
		if (start > end) return -1;
		if ((end - start) <= 1)
		{
			String key = localStorage.key(start);
			if (key.startsWith(entityName))
			{
				return start;
			}
			key = localStorage.key(end);
			if (key.startsWith(entityName))
			{
				return end;
			}
			return -1;
		}
		int middle = (start + end) / 2;
		String key = localStorage.key(middle);
		if (key.startsWith(entityName))
		{
			return middle;
		}
		else if (entityName.compareTo(key) > 0)
		{
			return getKeyIndex(entityName, middle, end);
		}
		else
		{
			return getKeyIndex(entityName, start, middle);
		}
	}

	private void checkRecord(String pk, String entityName, JsArrayString foreignColumns, Object[] coldata, JsArray<RecordDescription> rds, boolean addToTop)
	{
		RowDescription rowd = getRowDescription(entityName, pk);
		boolean foundMatch = false;
		if (rowd != null)
		{
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
		}
		if (foundMatch)
		{
			RecordDescription rd = RecordDescription.newInstance(pk);
			if (addToTop)
			{
				rds.unshift(rd);
			}
			else
			{
				rds.push(rd);
			}
		}
	}

	ArrayList<String> getAllPrimaryRelationNames(String entityName)
	{
		ArrayList<String> retval = new ArrayList<String>();
		if (entities != null)
		{
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
		}
		return retval;
	}

	String getRemotePK(String entityName, String pk, RowDescription row)
	{
		if (entities.isPKUUID(entityName) || (row != null && row.isCreatedOnDevice()))
		{
			String uuidPk = valueStore.getUUIDValue(Utils.getAsInteger(pk));
			if (uuidPk != null) return uuidPk;
			else Log.info("No uuid pk found for pk: " + pk + " of entity: " + entityName);
		}
		return pk;
	}

	JSONObject toRemoteJSON(String entityName, RowDescription row)
	{
		String[] uuidDataProviderNames = entities.getUUIDDataProviderNames(entityName);
		List<String> uuidCols = uuidDataProviderNames != null ? Arrays.asList(uuidDataProviderNames) : null;

		if (!row.isCreatedOnDevice())
		{
			// updated row
			String key = getRowDataPkAndKey(entityName, row)[1];
			String json = localStorage.getItem(key + SERVER_DATA_KEY);
			JSONArray values = JSONParser.parseStrict(json).isArray();
			String[] dataProviders = entities.getDataProviders(entityName);

			RowDescription serverRD = RowDescription.newInstance(dataProviders, values);

			JSONObject jsonObject = new JSONObject();
			for (String dataprovider : dataProviders)
			{
				Object serverValue = serverRD.getValue(dataprovider);
				Object clientValue = row.getValue(dataprovider);

				if (!Utils.equalObjects(serverValue, clientValue))
				{
					if (uuidCols != null && uuidCols.contains(dataprovider) && clientValue != null)
					{
						clientValue = valueStore.getUUIDValue(Utils.getAsInteger(clientValue));
					}
					if (clientValue == null)
					{
						jsonObject.put(dataprovider, JSONNull.getInstance());
					}
					else if (clientValue instanceof Number)
					{
						jsonObject.put(dataprovider, new JSONNumber(((Number)clientValue).doubleValue()));
					}
					else
					{
						jsonObject.put(dataprovider, new JSONString(clientValue.toString()));
					}
				}
			}
			if (jsonObject.size() == 1 && jsonObject.containsKey(RowDescription.MODIFICATION_DATE))
			{
				return null;
			}

			return jsonObject;
		}
		// new row
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

	private static Object getRecordValue(Record record, String dataProviderID)
	{
		if (dataProviderID != null)
		{
			if (dataProviderID.startsWith(LITERAL_PREFIX))
			{
				return Utils.parseJSExpression(dataProviderID.substring(LITERAL_PREFIX.length()));
			}
			else
			{
				return record.getValue(dataProviderID);
			}
		}
		return null;
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
				String dpn = dp.getDataProviderID();
				variableTypes.put(dpn, Integer.valueOf(dp.getType()));
				if (exported.add(dpn)) scope.exportProperty(javascriptObject, dpn);
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

	/**
	 * @param offlineDataProxy
	 * @param offlineData
	 */
	public void mergeOfflineData(OfflineDataProxy offlineDataProxy, OfflineDataDescription offlineData)
	{
		HashMap<String, HashSet<Object>> entitiesToPKs = new HashMap<String, HashSet<Object>>();

		//store data in offline db
		entities.update(offlineData.getEntities(), valueStore);
		// first sync up Relations -> RelationDescription
		for (int i = 0; i < application.getFlattenedSolution().relationCount(); i++)
		{
			Relation relation = application.getFlattenedSolution().getRelation(i);
			String primaryString = FoundSetManager.getEntityFromDataSource(relation.getPrimaryDataSource());
			String foreignString = FoundSetManager.getEntityFromDataSource(relation.getForeignDataSource());
			EntityDescription primary = getEntityDescription(primaryString);
			EntityDescription foreign = getEntityDescription(foreignString);

			if (primary != null && foreign != null)
			{
				RelationDescription primaryRelation = primary.getPrimaryRelation(relation.getName());
				if (primaryRelation == null)
				{
					primaryRelation = RelationDescription.newInstance(relation.getName(), primaryString, foreignString);
					primary.addPrimaryRelation(primaryRelation);
				}
				primaryRelation.setSelfRef(relation.isSelfRef());
			}
			else
			{
				Log.error("A relation was defined: " + relation.getName() + " on primary: " + relation.getPrimaryDataSource() + " and foreign: " +
					relation.getForeignDataSource() + " where no Entities/Tables where given by the service solution");
			}
		}
		localStorage.setItem(ENTITIES_KEY, entities.toJSONArray());

		ArrayList<FoundSet> contentChangedFoundSets = new ArrayList<FoundSet>();
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
						int newPK = valueStore.getOrPutUUID(uuid);
						rd.setPK(newPK);
					}
					//replace relation names with relation ids, to save local storage space
					JsArrayString rfs = rd.getRFS();
					for (int k = 0; k < rfs.length(); k++)
					{
						String key = rfs.get(k);
						int idx = key.indexOf('|');
						int id = entities.getRelationID(key.substring(0, idx));
						String hash = key.substring(idx + 1);
						hash = replaceUUIDHash(hash, true);
						rfs.set(k, id + "|" + hash);
					}
				}

				storeFoundSetDescription(fd, true, contentChangedFoundSets);
			}
		}

		exportDataproviders();

		//initiate load of all row data
		offlineDataProxy.requestRowData(entitiesToPKs, true, contentChangedFoundSets);
	}

	/**
	 * @param userName
	 * @param password
	 */
	public void storeCredentials(String userName, String password)
	{
		localStorage.setItem("user_name", userName);
		localStorage.setItem("user_password", password);
	}

	public String[] getCredentials()
	{
		String userName = localStorage.getItem("user_name");
		String password = localStorage.getItem("user_password");
		if (userName != null && password != null)
		{
			return new String[] { userName, password };
		}
		return null;
	}

	public void clearCredentials()
	{
		localStorage.removeItem("user_name");
		localStorage.removeItem("user_password");
	}

	public String getUUIDPKValueAsString(int intVal)
	{
		return valueStore.getUUIDValue(intVal);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void setUserProperty(String name, String value)
	{
		if (value == null)
		{
			localStorage.removeItem(USER_PROPERTY_PREFIX + name);
		}
		else
		{
			localStorage.setItem(USER_PROPERTY_PREFIX + name, value);
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public String getUserProperty(String name)
	{
		return localStorage.getItem(USER_PROPERTY_PREFIX + name);
	}

	/**
	 * Delete records which were removed server side during sync (between offline_data and data_* request)
	 * @param entityName
	 * @param rowData the data received from the server
	 * @param pks the pks for which a data request was made
	 */
	private void removeNotReceivedRecords(String entityName, JsArray<RowDescription> rowData, String[] pks)
	{
		FoundSet fs = getFoundSet(entityName, false);
		outer : for (String pk : pks)
		{
			for (int i = 0; i < rowData.length(); i++)
			{
				RowDescription row = rowData.get(i);
				String rowPK = getRowDataPkAndKey(entityName, row)[0];
				if (pk.equals(rowPK)) continue outer;
			}

			fs.removeRecord(pk);
		}
		storeFoundSetDescription(fs.getFoundSetDescription(), false, null);
	}
}
