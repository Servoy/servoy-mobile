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

import java.util.HashMap;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.util.DataproviderIdAndTypeHolder;

/**
 * Helper class to do fast lookups on EntityDescriptions
 * @author jblok
 */
@SuppressWarnings("nls")
public class Entities
{
	private static final int MODIFICATION_DATE_TYPE = 93;//timestamp from java.sql.Types
	private static final int CREATED_ON_DEVICE_TYPE = 4;//int from java.sql.Types

	private final JsArray<EntityDescription> entities;
	private final HashMap<String, EntityDescription> eds = new HashMap<String, EntityDescription>();

	Entities(JsArray<EntityDescription> e, ValueStore valueStore)
	{
		if (e == null) throw new NullPointerException();
		entities = e;

		fillInRelationIds(valueStore);

		for (int i = 0; i < entities.length(); i++)
		{
			EntityDescription ed = entities.get(i);
			eds.put(ed.getEntityName(), ed);

			if (valueStore != null) addMissingColumns(ed);
		}
	}

	EntityDescription getDescription(String entityName)
	{
		return eds.get(entityName);
	}

	JsArray<EntityDescription> getEntityDescriptions()
	{
		return entities;
	}

	private void addMissingColumns(EntityDescription ed)
	{
		DataProviderDescription mod_date = null;
		DataProviderDescription created_on_device = null;
		JsArray<DataProviderDescription> dps = ed.getDataProviders();
		for (int i = 0; i < dps.length(); i++)
		{
			DataProviderDescription dpd = dps.get(i);
			if (RowDescription.MODIFICATION_DATE.equals(dpd.getName()))
			{
				mod_date = dpd;
			}
			if (RowDescription.CREATED_ON_DEVICE.equals(dpd.getName()))
			{
				mod_date = dpd;
			}
		}
		if (mod_date == null)
		{
			mod_date = DataProviderDescription.newInstance(RowDescription.MODIFICATION_DATE, MODIFICATION_DATE_TYPE);
			dps.push(mod_date);
		}
		if (created_on_device == null)
		{
			created_on_device = DataProviderDescription.newInstance(RowDescription.CREATED_ON_DEVICE, CREATED_ON_DEVICE_TYPE);
			dps.push(created_on_device);
		}
	}

	private final HashMap<String, DataproviderIdAndTypeHolder> pks = new HashMap<String, DataproviderIdAndTypeHolder>();

	DataproviderIdAndTypeHolder getPKDataProviderID(String entityName)
	{
		DataproviderIdAndTypeHolder retval = pks.get(entityName);
		if (retval == null)
		{
			EntityDescription ed = getDescription(entityName);
			if (ed != null)
			{
				retval = ed.getPKDataProviderID();
				pks.put(entityName, retval);
			}
		}
		return retval;
	}

	public String toJSONArray()
	{
		return new JSONArray(entities).toString();
	}

	//fill in ids
	private void fillInRelationIds(ValueStore valueStore)
	{
		for (int i = 0; i < entities.length(); i++)
		{
			EntityDescription ed = entities.get(i);
			JsArray<RelationDescription> rels = ed.getPrimaryRelations();
			if (rels != null)
			{
				for (int j = 0; j < rels.length(); j++)
				{
					RelationDescription rd = rels.get(j);
					int id = rd.getID();
					if (id == 0 && valueStore != null)
					{
						id = valueStore.getNextVal();
						rd.setID(id);
					}
					relationIDs.put(rd.getName(), new Integer(id));
				}
			}
		}
	}

	private final HashMap<String, Integer> relationIDs = new HashMap<String, Integer>();

	public int getRelationID(String relationName)
	{
		Integer retval = relationIDs.get(relationName);
		if (retval != null)
		{
			return retval.intValue();
		}
		return 0;
	}

	private final HashMap<String, String[]> dataproviders = new HashMap<String, String[]>();

	public String[] getDataProviders(String entityName)
	{
		String[] retval = dataproviders.get(entityName);
		if (retval == null)
		{
			EntityDescription ed = getDescription(entityName);
			if (ed != null)
			{
				JsArray<DataProviderDescription> dps = ed.getDataProviders();
				retval = new String[dps.length()];
				for (int i = 0; i < retval.length; i++)
				{
					retval[i] = dps.get(i).getName();
				}
				dataproviders.put(entityName, retval);
			}
		}
		return retval;
	}

	private final HashMap<String, String[]> uuiddataproviders = new HashMap<String, String[]>();

	public String[] getUUIDDataProviderNames(String entityName)
	{
		String[] retval = uuiddataproviders.get(entityName);
		if (retval == null)
		{
			EntityDescription ed = getDescription(entityName);
			if (ed != null)
			{
				retval = ed.getUUIDDataProviderNames();
				uuiddataproviders.put(entityName, retval);
			}
		}
		return retval;
	}

	private final HashMap<String, Boolean> uuidpk = new HashMap<String, Boolean>();

	public final boolean isPKUUID(String entityName)
	{
		Boolean retval = uuidpk.get(entityName);
		if (retval == null)
		{
			EntityDescription ed = getDescription(entityName);
			if (ed != null)
			{
				retval = new Boolean(ed.isPKUUID());
				uuidpk.put(entityName, retval);
			}
		}
		return retval.booleanValue();
	}

	RelationDescription getPrimaryRelation(String entityName, String relationName)
	{
		EntityDescription ed = getDescription(entityName);
		if (ed != null)
		{
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
		}
		return null;
	}
}
