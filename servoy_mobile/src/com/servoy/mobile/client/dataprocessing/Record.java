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

import com.google.gwt.core.client.JsArrayString;
import com.servoy.mobile.client.dto.RecordDescription;
import com.servoy.mobile.client.dto.RowDescription;

/**
 * The mobile record
 * @author jblok
 */
public class Record
{
	protected FoundSet parent;
	protected RecordDescription recordDescription;
	protected RowDescription rowDescription;
	protected HashMap<String,FoundSet> relatedFoundSets;
	
	//existing record
	public Record(FoundSet p,RecordDescription rd)
	{
		parent = p;
		recordDescription = rd;
		relatedFoundSets = new HashMap<String,FoundSet>();
	}
	
	//new record
	public Record(FoundSet foundSet, RecordDescription rd, RowDescription rowd) 
	{
		this(foundSet,rd);
		rowDescription = rowd;
	}

	public Object getPK()
	{
		return recordDescription.getPK();
	}
	
	public Object getValue(String dataProviderID)
	{
		RowDescription rd = getRowDescription();
		if (rd == null) return null;
		return rd.getValue(dataProviderID);
	}

	public void setValue(String dataProviderID,Object obj)
	{
		RowDescription rd = getRowDescription();
		if (rd == null) return;
		parent.startEdit(this);
		rd.setValue(dataProviderID,obj);
	}

	private RowDescription getRowDescription()
	{
		if (rowDescription == null)
		{
			rowDescription = parent.getRowDescription(getPK());
		}
		return rowDescription;
	}
	
	public FoundSet getRelatedFoundSet(String relationName)
	{
		FoundSet retval = relatedFoundSets.get(relationName);
		if (retval == null)
		{
			JsArrayString avail = recordDescription.getRFS();
			if (avail != null)
			{
				for (int i = 0; i < avail.length(); i++) 
				{
					String key = avail.get(i);
					if (key.startsWith(relationName+"|"))
					{
						retval = parent.getRelatedFoundSet(this,relationName,key);
						relatedFoundSets.put(relationName,retval);
						return retval;
					}
				}
			}
			retval = parent.createRelatedFoundSet(relationName,this);
			if (retval != null)
			{
				relatedFoundSets.put(relationName,retval);
				recordDescription.getRFS().push(relationName+"|"+retval.getWhereArgsHash());
			}
		}
		return retval;
	}
	
	public FoundSet getParent() 
	{
		return parent;
	}

	public RowDescription getRow() 
	{
		return rowDescription;
	}
	
	void linkupRelatedFoundSets() 
	{
		ArrayList<String> pRelNames = parent.getAllPrimaryRelationNames();
		for (String relationName : pRelNames) 
		{
			if (!relatedFoundSets.containsKey(relationName))
			{
				getRelatedFoundSet(relationName);
			}
		}
	}
}
