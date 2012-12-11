package com.servoy.mobile.client.dataprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.servoy.mobile.client.dto.RowDescription;

public class EditRecordList
{
	public static final int STOPPED = 1;

	private final FoundSetManager foundSetManager;
	private final List<Record> editedRecords = new ArrayList<Record>();

	public EditRecordList(FoundSetManager fsm)
	{
		foundSetManager = fsm;
	}

	public boolean startEditing(Record record)
	{
		if (!editedRecords.contains(record))
		{
			editedRecords.add(record);
		}
		return true;
	}

	public int stopEditing(boolean javascriptStop)
	{
		HashSet<FoundSet> toCheck = new HashSet<FoundSet>();
		HashMap<String, ArrayList<RowDescription>> toStore = new HashMap<String, ArrayList<RowDescription>>();

		for (Record rec : editedRecords)
		{
			//rec.linkupRelatedFoundSets();//prevent future seeks after this save, but this is too expensive

			FoundSet fs = rec.getParent();
			toCheck.add(fs);
			String entityName = fs.getEntityName();
			ArrayList<RowDescription> rows = toStore.get(entityName);
			if (rows == null)
			{
				rows = new ArrayList<RowDescription>();
				toStore.put(entityName, rows);
			}
			RowDescription row = rec.getRow();
			if (!rows.contains(row))
			{
				rows.add(row);
			}
		}

		Iterator<String> it = toStore.keySet().iterator();
		while (it.hasNext())
		{
			String entityName = it.next();
			foundSetManager.storeRowData(entityName, toStore.get(entityName), true);
		}

		Iterator<FoundSet> it2 = toCheck.iterator();
		while (it2.hasNext())
		{
			FoundSet fs = it2.next();
			foundSetManager.checkForPKStorage(fs);
		}

		return STOPPED;
	}

	/**
	 * @param record
	 * @return
	 */
	public boolean isEditting(Record record)
	{
		return editedRecords.contains(record);
	}
}
