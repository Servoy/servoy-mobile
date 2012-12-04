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
	private transient HashMap<String, Integer> uid_to_id = new HashMap<String, Integer>();//temp storage for fast lookups 
	private final Storage localStorage;

	ValueStore(Storage s)
	{
		localStorage = s;
	}

	String getUUIDValue(int val)
	{
		return localStorage.getItem("_" + val);
	}

	int putUUID(String uuid)
	{
		uuid = uuid.toLowerCase();
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
		int lastVal = Utils.getAsInteger(localStorage.getItem("_lastVal"));
		lastVal++;
		localStorage.setItem("_lastVal", String.valueOf(lastVal));
		return lastVal;
	}
}
