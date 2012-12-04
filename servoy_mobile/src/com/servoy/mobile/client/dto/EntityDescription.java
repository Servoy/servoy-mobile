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

/**
 * @author jblok
 */
public class EntityDescription extends JavaScriptObject
{
	protected EntityDescription() {}

	public final native String getDataSource() /*-{
		return this.dataSource;
	}-*/;

	public final native String getEntityName() /*-{
		return this.entityName;
	}-*/;
	
	public final native JsArray<DataProviderDescription> getDataProviders() /*-{
		return this.dataProviders;
	}-*/;

	public final native JsArray<RelationDescription> getPrimaryRelations() /*-{
		return this.primaryRelations;
	}-*/;

	public final String getPKDataProviderID() 
	{
		JsArray<DataProviderDescription> dps = getDataProviders();
		for (int i = 0; i < dps.length(); i++) 
		{
			DataProviderDescription dpd = dps.get(i);
			if (dpd.isPK() || dpd.isRowIdent())
			{
				//is pk or row_ident
				return dpd.getName();
			}
		}
		return null;
	}

	public final String[] getUUIDDataProviderNames() 
	{
		ArrayList<String> retval = new ArrayList<String>();  
		JsArray<DataProviderDescription> dps = getDataProviders();
		for (int i = 0; i < dps.length(); i++) 
		{
			DataProviderDescription dpd = dps.get(i);
			if (dpd.isUUID())
			{
				retval.add(dpd.getName());
			}
		}
		if (retval.size() == 0) return null;
		return retval.toArray(new String[retval.size()]);
	}

	public final boolean isPKUUID() 
	{
		JsArray<DataProviderDescription> dps = getDataProviders();
		for (int i = 0; i < dps.length(); i++) 
		{
			DataProviderDescription dpd = dps.get(i);
			if ((dpd.isPK() || dpd.isRowIdent()) && dpd.isUUID())
			{
				return true;
			}
		}
		return false;
	}
}
