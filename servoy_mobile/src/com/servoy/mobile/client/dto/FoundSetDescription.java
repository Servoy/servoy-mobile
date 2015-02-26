package com.servoy.mobile.client.dto;

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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.servoy.mobile.client.dataprocessing.Record;

/**
 * @author jblok
 */
public class FoundSetDescription extends JavaScriptObject
{
	protected FoundSetDescription()
	{
	}

	public final native String getEntityName() /*-{
		return this.entityName;
	}-*/;

	private final native void deleteEntityName() /*-{
		delete this.entityName;
	}-*/;

	private final native void setEntityName(String ename) /*-{
		this.entityName = ename;
	}-*/;

	public final native String getRelationName() /*-{
		return this.relationName;
	}-*/;

	private final native void deleteRelationName() /*-{
		delete this.relationName;
	}-*/;

	private final native String setRelationName(String rname) /*-{
		this.relationName = rname;
	}-*/;

	public final native String getWhereArgsHash() /*-{
		return this.hash;
	}-*/;

	private final native void deleteWhereArgsHash() /*-{
		delete this.hash;
	}-*/;

	private final native String setWhereArgsHash(String ahash) /*-{
		this.hash = ahash;
	}-*/;

	private final native void deleteEmptyAttributes() /*-{
		if (this.records) {
			if (this.records.length == 0) {
				delete this.records;
			} else {
				for (var i = 0; i < this.records.length; i++) {
					var rec = this.records[i];
					if (rec.rfs && rec.rfs.length == 0) {
						delete rec.rfs;
					}
				}
			}
		}
	}-*/;

	public final String toJSON()
	{
		return toJSON(false);
	}

	public final String toJSON(boolean omitForKeyInfo)
	{
		String relationName = getRelationName();
		String hash = getWhereArgsHash();
		String entityName = getEntityName();
		try
		{
			if (omitForKeyInfo)
			{
				deleteRelationName();
				deleteWhereArgsHash();
				deleteEntityName();
			}
			deleteEmptyAttributes();
			return new JSONObject(this).toString();
		}
		finally
		{
			if (omitForKeyInfo)
			{
				setRelationName(relationName);
				setWhereArgsHash(hash);
				setEntityName(entityName);
			}
		}
	}

	public final boolean needsInfoFromKey()
	{
		return (getRelationName() == null && getWhereArgsHash() == null);
	}

	public final void setInfoFromKey(String key, String relationName, String entityName)
	{
		setRelationName(relationName);
		int idx = key.indexOf('|');
		setWhereArgsHash(key.substring(idx + 1));
		setEntityName(entityName);
	}

	public final native JsArray<RecordDescription> getRecords() /*-{
		if (!this.records)
			this.records = new Array();
		return this.records;
	}-*/;

	public final native void removeRecord(int index) /*-{
		if (this.records) {
			this.records.splice(index, 1);
		}
	}-*/;

	public final native void insertRecord(int index, RecordDescription record) /*-{
		if (!this.records) {
			this.records = new Array();
		}
		if (index == this.records.length) {
			this.records.push(record);
		} else {
			this.records.splice(index, 0, record);
		}
	}-*/;

	public final ArrayList<Object> getPKs()
	{
		ArrayList<Object> retval = new ArrayList<Object>();
		JsArray<RecordDescription> recs = getRecords();
		for (int i = 0; i < recs.length(); i++)
		{
			RecordDescription rec = recs.get(i);
			retval.add(rec.getPK());
		}
		return retval;
	}

	public static FoundSetDescription newInstance(String entityName, String relationName, String whereArgsHash)
	{
		FoundSetDescription fd = JavaScriptObject.createObject().cast();
		fd.setEntityName(entityName);
		fd.setRelationName(relationName);
		fd.setWhereArgsHash(whereArgsHash);
		return fd;
	}

	/**
	 * @param records
	 */
	public final void updateRecordDescriptions(ArrayList<Record> records)
	{
		JsArray<RecordDescription> newArray = JavaScriptObject.createArray().cast();
		for (Record record : records)
		{
			newArray.push(record.getRecordDescription());
		}
		setRecordDescriptions(newArray);
	}

	public final native void setRecordDescriptions(JsArray<RecordDescription> newArray) /*-{
		this.records = newArray;
	}-*/;
}
