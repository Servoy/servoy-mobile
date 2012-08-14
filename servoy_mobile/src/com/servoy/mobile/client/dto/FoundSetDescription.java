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
import com.google.gwt.json.client.JSONParser;

/**
 * @author jblok
 */
public class FoundSetDescription extends JavaScriptObject
{
	protected FoundSetDescription() {}

	public final native String getEntityName() /*-{
		return this.entityName;
	}-*/;
	
	public final native String getRelationName() /*-{
		return this.relationName;
	}-*/;

	public final native String getWhereArgsHash() /*-{
		return this.hash;
	}-*/;
	
	public final String toJSON()
	{
		return new JSONObject(this).toString();
	}
	
	public final native JsArray<RecordDescription> getRecords() /*-{
		if (!this.records) this.records = new Array();
		return this.records;
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

	public static FoundSetDescription newInstance(String entityName,String relationName, String whereArgsHash) 
	{
		return JSONParser.parseStrict("{\"entityName\":\""+entityName+"\",\"relationName\":\""+relationName+"\",\"hash\":\""+whereArgsHash+"\"}").isObject().getJavaScriptObject().cast();
	}
}
