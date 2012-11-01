package com.servoy.mobile.client.persistence;

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

import org.timepedia.exporter.client.Export;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author gboros
 */
public class Solution extends JavaScriptObject
{
	protected Solution()
	{
	}

	public final native int formCount() /*-{
		return this.forms.length;
	}-*/;

	public final native int relationCount() /*-{
		return this.relations.length;
	}-*/;

	public final native Form getForm(int i) /*-{
		return this.forms[i];
	}-*/;

	public final native Relation getRelation(int i) /*-{
		return this.relations[i];
	}-*/;

	public final native String getSolutionName() /*-{
		return this.solutionName;
	}-*/;

	public final native String getServerUrl() /*-{
		return this.serverURL;
	}-*/;

	public final native boolean getSkipConnect() /*-{
		return this.skipConnect;
	}-*/;

	public final native boolean getMustAuthenticate() /*-{
		return this.mustAuthenticate;
	}-*/;

	@Export
	public final Form getForm(String name)
	{
		for (int i = 0; i < formCount(); i++)
		{
			if (getForm(i).getName().equals(name)) return getForm(i);
		}
		return null;
	}

	@Export
	public final Relation getRelation(String name)
	{
		for (int i = 0; i < relationCount(); i++)
		{
			if (getRelation(i).getName().equals(name)) return getRelation(i);
		}
		return null;
	}

	public final Form getFormByUUID(String uuid)
	{
		for (int i = 0; i < formCount(); i++)
		{
			if (getForm(i).getUUID().equals(uuid)) return getForm(i);
		}
		return null;
	}
}
