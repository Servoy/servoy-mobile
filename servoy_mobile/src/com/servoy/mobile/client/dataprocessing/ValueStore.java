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

import com.google.gwt.storage.client.Storage;
import com.servoy.mobile.client.util.Utils;

/**
 * UUID to value mapper
 * @author jblok
 */
@SuppressWarnings("nls")
public class ValueStore
{
	private static final String LAST_VALUE_KEY = "_svy_lastVal";

	private transient HashMap<String, Integer> uid_to_id = new HashMap<String, Integer>();//temp storage for fast lookups
	private final Storage localStorage;

	ValueStore(Storage s)
	{
		localStorage = s;
	}

	void setRemoteID(int val, String remoteID)
	{
		localStorage.setItem("_" + val + "_r", remoteID);
	}

	String getRemoteID(int val)
	{
		return localStorage.getItem("_" + val + "_r");
	}

	void removeValueFromLocalStorage(int val)
	{
		localStorage.removeItem("_" + val);
		localStorage.removeItem("_" + val + "_r");
	}

	String getUUIDValue(int val)
	{
		return localStorage.getItem("_" + val);
	}

	int getOrPutUUID(String uuid)
	{
		uuid = uuid.toUpperCase(); //default java UUID representation is in uppercase A-F
		int val = Utils.getAsInteger(uid_to_id.get(uuid));
		if (val == 0)
		{
			// now first scan
			for (int i = 0; i < localStorage.getLength(); i++)
			{
				String key = localStorage.key(i);
				if (key.startsWith("_"))
				{
					String item = localStorage.getItem(key);
					if (item.equals(uuid))
					{
						val = Utils.getAsInteger(key.substring(1));
						uid_to_id.put(uuid, val);
						return val;
					}
				}
			}
			return putUUID(uuid);
		}
		return val;
	}

	int putUUID(String uuid)
	{
		uuid = uuid.toUpperCase(); //default java UUID representation is in uppercase A-F
		int val = Utils.getAsInteger(uid_to_id.get(uuid));
		if (val == 0)
		{
			val = getNextVal();

			uid_to_id.put(uuid, val);

			//store persistent as well
			localStorage.setItem("_" + val, uuid);
		}
		return val;
	}

	int getNextVal()
	{
		int lastVal = Utils.getAsInteger(localStorage.getItem(LAST_VALUE_KEY));
		// go to negative values so it doesn't overlap with real pk coming from server
		lastVal--;
		localStorage.setItem(LAST_VALUE_KEY, String.valueOf(lastVal));
		return lastVal;
	}

	void clearCache()
	{
		uid_to_id.clear();
	}
}
