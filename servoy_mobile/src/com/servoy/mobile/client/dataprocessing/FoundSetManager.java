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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.storage.client.Storage;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.FoundSetDescription;
import com.servoy.mobile.client.dto.OfflineDataDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.dto.ValueListDescription;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile foundset manager
 * @author jblok
 */
//export as databaseManager
public class FoundSetManager
{
	private final Storage localStorage = Storage.getLocalStorageIfSupported();
	private final MobileClient application;
	private EditRecordList editRecordList;

	//fields mapped to local storage
	private JsArray<EntityDescription> entities;
	private String entityPrefix;
	private JsArray<ValueListDescription> valueLists;
	private ArrayList<String> changes;

	public FoundSetManager(MobileClient mc)
	{
		application = mc;
		editRecordList = new EditRecordList(this);

		String ejson = localStorage.getItem("entities");
		if (ejson != null)
		{
			JSONArray ea = JSONParser.parseStrict(ejson).isArray();
			if (ea != null) entities = ea.getJavaScriptObject().cast();
		}
		entityPrefix = localStorage.getItem("entityPrefix");
		changes = new ArrayList<String>();
		String cjson = localStorage.getItem("changes");
		if (cjson != null)
		{
			JSONArray ca = JSONParser.parseStrict(cjson).isArray();
			if (ca != null)
			{
				JsArrayString jca = ca.getJavaScriptObject().cast();
				for (int i = 0; i < jca.length(); i++)
				{
					changes.add(jca.get(i));
				}
			}
		}
		String vjson = localStorage.getItem("valuelists");
		if (vjson != null)
		{
			JSONArray va = JSONParser.parseStrict(vjson).isArray();
			if (va != null) valueLists = va.getJavaScriptObject().cast();
		}
	}

	public boolean hasContent()
	{
		return (localStorage.getItem("entities") != null);
	}

	RowDescription getRowDescription(String entityName, Object pk)
	{
		//load data from offline db
		String json = localStorage.getItem(entityName + '|' + pk);
		if (json == null) return null;
		return JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
	}

	FoundSet getRelatedFoundSet(Record rec, String entityName, String relationName, String key)
	{
		//load data from offline db
		String json = localStorage.getItem(key);
		if (json == null) return null;
		FoundSetDescription fsd = JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
		return new RelatedFoundSet(this, rec, fsd, relationName);
	}

	String getEntityPrefix()
	{
		return (entityPrefix == null ? "" : entityPrefix);
	}

	void storeOfflineData(OfflineDataProxy offlineDataProxy, String fsname, OfflineDataDescription offlineData)
	{
		HashMap<String, HashSet<Object>> entitiesToPKs = new HashMap<String, HashSet<Object>>();

		//store data in offline db
		entities = offlineData.getEntities();
		localStorage.setItem("entities", new JSONArray(entities).toString());

		entityPrefix = offlineData.getEntityPrefix();
		if (entityPrefix != null)
		{
			localStorage.setItem("entityPrefix", entityPrefix);
		}

		valueLists = offlineData.getValueLists();
		if (valueLists != null)
		{
			localStorage.setItem("valuelists", new JSONArray(valueLists).toString());
		}

		JsArray<FoundSetDescription> fsds = offlineData.getFoundSets();
		if (fsds != null)
		{
			for (int i = 0; i < fsds.length(); i++)
			{
				FoundSetDescription fd = fsds.get(i);

				//fill entitiesToPKs
				HashSet<Object> set = entitiesToPKs.get(fd.getEntityName());
				if (set == null)
				{
					set = new HashSet<Object>();
					entitiesToPKs.put(fd.getEntityName(), set);
				}
				set.addAll(fd.getPKs());

				//store data in offline db
				storeFoundSetDescription(fd);
			}
		}
		//initiate load of all row data
		offlineDataProxy.requestRowData(entitiesToPKs);
	}

	private void storeFoundSetDescription(FoundSetDescription fd)
	{
		String key = fd.getEntityName();
		if (fd.getRelationName() != null)
		{
			key = fd.getRelationName();
			if (fd.getWhereArgsHash() != null) //if global/constant relation at server we did omit the argshash
			{
				key += '|' + fd.getWhereArgsHash();
			}
		}
		localStorage.setItem(key, fd.toJSON());
	}

	void storeRowData(String entityName, JsArray<RowDescription> rowData)
	{
		ArrayList<RowDescription> list = new ArrayList<RowDescription>();
		for (int i = 0; i < rowData.length(); i++)
		{
			RowDescription row = rowData.get(i);
			list.add(row);
		}
		storeRowData(entityName, list, false);
	}

	void storeRowData(String entityName, ArrayList<RowDescription> rowData, boolean local)
	{
		String dataProviderID = getPKDataProviderID(entityName);
		if (dataProviderID == null) throw new IllegalStateException(application.getMessages().cannotWorkWithoutPK());

		int oldSize = changes.size();

		//store data in offline db
		for (RowDescription row : rowData)
		{
			Object pk = row.getValue(dataProviderID);
			String key = entityName + '|' + pk;
			if (local)
			{
				if (!changes.contains(key)) changes.add(key);
			}
			localStorage.setItem(key, row.toJSON());
		}

		if (changes.size() != oldSize)
		{
			JSONArray jsona = new JSONArray();
			for (int i = 0; i < changes.size(); i++)
			{
				String change = changes.get(i);
				jsona.set(i, new JSONString(change));
			}
			localStorage.setItem("changes", jsona.toString());
		}
	}

	private String getPKDataProviderID(String entityName)
	{
		EntityDescription ed = getEntityDescription(entityName);
		if (ed != null)
		{
			return ed.getPKDataProviderID();
		}
		return null;
	}

	EntityDescription getEntityDescription(String entityName)
	{
		if (entities == null) return null;
		for (int i = 0; i < entities.length(); i++)
		{
			EntityDescription ed = entities.get(i);
			if (entityName.equals(ed.getEntityName()))
			{
				return ed;
			}
		}
		return null;
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
		return new FoundSet(this, fsd);
	}

	//delete all local data
	public void clearLocalStorage()
	{
		localStorage.clear();
		entities = null;

		editRecordList = new EditRecordList(this);
		changes = new ArrayList<String>();
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
		return (changes.size() != 0);
	}

	ArrayList<String> getChanges()
	{
		return changes;
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

	public ValueListDescription getValueListItems(String valueListName)
	{
		if (valueLists != null)
		{
			for (int i = 0; i < valueLists.length(); i++)
			{
				ValueListDescription vld = valueLists.get(i);
				if (vld.getName().equals(valueListName))
				{
					return vld;
				}
			}
		}
		return null;
	}

	RowDescription createRowDescription(FoundSet fs, Object pkval)
	{
		RowDescription retval = RowDescription.newInstance();
		retval.setValue(getPKDataProviderID(fs.getEntityName()), pkval);
		if (fs instanceof RelatedFoundSet)
		{
			RelatedFoundSet rfs = (RelatedFoundSet)fs;
			String relationName = rfs.getRelationName();
			Record[] parentRecords = rfs.getParents();
			String entityName = parentRecords[0].getParent().getEntityName();
			RelationDescription rd = getPrimaryRelation(relationName, entityName);
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

	RelationDescription getPrimaryRelation(String relationName, String entityName)
	{
		EntityDescription ed = getEntityDescription(entityName);
		JsArray<RelationDescription> rels = ed.getPrimaryRelations();
		if (rels != null)
		{
			for (int i = 0; i < rels.length(); i++)
			{
				RelationDescription rd = rels.get(i);
				if (relationName.equals(rd.getName()))
				{
					return rd;
				}
			}
		}
		return null;
	}

	FoundSet createRelatedFoundSet(String relationName, Record record)
	{
		String entityName = record.getParent().getEntityName();
		RelationDescription rd = getPrimaryRelation(relationName, entityName);
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

	private void seek(String entityName, JsArrayString foreignColumns, Object[] coldata, JsArray<RecordDescription> rds)
	{
		String entityNamePlusPipe = entityName + "|";
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
		EntityDescription ed = getEntityDescription(entityName);
		JsArray<RelationDescription> rels = ed.getPrimaryRelations();
		if (rels != null)
		{
			for (int i = 0; i < rels.length(); i++)
			{
				RelationDescription rd = rels.get(i);
				retval.add(rd.getName());
			}
		}
		return retval;
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
}
