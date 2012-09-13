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

import com.servoy.mobile.client.dto.FoundSetDescription;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.util.Utils;

/**
 * The mobile foundset
 * @author jblok
 */
public class FoundSet //  extends Scope if we support aggregates on foundset
{
	private final FoundSetManager foundSetManager;
	private final FoundSetDescription foundSetDescription;
	private final ArrayList<Record> records = new ArrayList<Record>();
	private boolean needToSaveFoundSetDescription;

	public FoundSet(FoundSetManager fsm, FoundSetDescription fsd)
	{
		foundSetManager = fsm;
		foundSetDescription = fsd;
		export();
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

	public FoundSetManager getFoundSetManager()
	{
		return foundSetManager;
	}

	public int getSize()
	{
		return foundSetDescription.getRecords().length();
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

	private final int selectedRecord = 0;

	public Object getSelectedRecordValue(String dataProviderID)
	{
		if (getSize() > 0)
		{
			return getSelectedRecord().getValue(dataProviderID);
		}
		return null;
	}

	public Record getSelectedRecord()
	{
		return getRecord(selectedRecord);
	}

	public Record newRecord()
	{
		Object pk = Utils.createStringUUID();
		RecordDescription recd = RecordDescription.newInstance(pk);
		RowDescription rowd = foundSetManager.createRowDescription(this, pk);
		Record retval = new Record(this, recd, rowd);
		foundSetDescription.getRecords().push(recd);
		needToSaveFoundSetDescription = true;
		records.add(retval);
		startEdit(retval);
		return retval;
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

	public String getWhereArgsHash()
	{
		return foundSetDescription.getWhereArgsHash();
	}

	ArrayList<String> getAllPrimaryRelationNames()
	{
		return foundSetManager.getAllPrimaryRelationNames(getEntityName());
	}

	private native void export() /*-{
		this.getSelectedRecord = function() {
			return this.@com.servoy.mobile.client.dataprocessing.FoundSet::getSelectedRecord()();
		}
		this.getRecord = function(index) {
			return this.@com.servoy.mobile.client.dataprocessing.FoundSet::getRecord(I)(index-1);
		}
	}-*/;
}
