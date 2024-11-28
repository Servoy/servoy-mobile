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

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.client.dto.RelationDescription;

/**
 * @author gboros
 */
public class Solution extends JavaScriptObject
{
	protected Solution()
	{
	}

	public final native int relationCount() /*-{
		return this.relations.length;
	}-*/;

	public final native int formCount() /*-{
		return this.forms.length;
	}-*/;

	public final native Form getForm(int i) /*-{
		return this.forms[i];
	}-*/;

	public final Form getForm(String name)
	{
		for (int i = 0; i < formCount(); i++)
		{
			if (getForm(i).getName().equals(name)) return getForm(i);
		}
		return null;
	}

	/**
	 * This one shouldn't be called from here normally for the ui, you have to use {@link RelationDescription} instances
	 */
	public final native Relation getRelation(int i) /*-{
		return this.relations[i];
	}-*/;

	public final native String getSolutionName() /*-{
		return this.solutionName;
	}-*/;

	public final native String getOnSolutionOpen()
	/*-{
		return this.onSolutionOpen;
	}-*/;

	public final native String getLoginForm()
	/*-{
		return this.loginForm;
	}-*/;

	public final native String getServerUrl() /*-{
		return this.serverURL;
	}-*/;

	public final native String getServiceSolutionName() /*-{
		return this.serviceSolutionName;
	}-*/;

	public final native int getTimeout() /*-{
		return this.timeout;
	}-*/;

	public final native boolean getSkipConnect() /*-{
		return this.skipConnect;
	}-*/;

	public final native void setSkipConnect(boolean skip) /*-{
		this.skipConnect = skip;
	}-*/;

	public final native boolean getMustAuthenticate() /*-{
		return this.mustAuthenticate;
	}-*/;

	public final native String getStyleSheet() /*-{
		return this.styleSheet;
	}-*/;


	public final native String getI18nValue(String key) /*-{
		if (!this.i18n)
			this.i18n = {};
		return this.i18n[key];
	}-*/;

	// this is a "clone" form, not original; it will not be referenced in "forms" member
	public final native Form instantiateForm(String name, String uuid, int typeId) /*-{
		var nf = {};
		nf.uuid = uuid;
		nf.name = name;
		nf.typeid = typeId;
		nf.items = [];
		return nf;
	}-*/;

	public final native void setI18nValue(String key, String value)
	/*-{
		if (!this.i18n)
			this.i18n = {};
		this.i18n[key] = value;
	}-*/;

	/**
	 * This one shouldn't be called from here normally for the ui, you have to use {@link RelationDescription} instances
	 */
	public final Relation getRelation(String name)
	{
		for (int i = 0; i < relationCount(); i++)
		{
			if (getRelation(i).getName().equals(name)) return getRelation(i);
		}
		return null;
	}

	/**
	 * @param valuelistID
	 * @return
	 */
	public final ValueList getValueListByUUID(String valuelistID)
	{
		for (int i = 0; i < valuelistCount(); i++)
		{
			if (getValueList(i).getUUID().equals(valuelistID)) return getValueList(i);
		}
		return null;
	}


	/**
	 * @param name
	 */
	public final ValueList getValueList(String name)
	{
		for (int i = 0; i < valuelistCount(); i++)
		{
			if (getValueList(i).getName().equals(name)) return getValueList(i);
		}
		return null;
	}

	public final native int valuelistCount()
	/*-{
		return this.valuelists.length;
	}-*/;

	public final native ValueList getValueList(int i)
	/*-{
		return this.valuelists[i];
	}-*/;

	public final native ValueList instantiateValueList(String name, String uuid) /*-{
		var vl = {};
		vl.uuid = uuid;
		vl.name = name;
		vl.displayValues = [];
		vl.realValues = [];
		if (!this.valuelists) {
			this.valuelists = [];
		}
		this.valuelists.push(vl);
		return vl;
	}-*/;

	public final native void removeValueList(ValueList valuelist)
	/*-{
		if (this.valuelists) {
			var index = this.valuelists.indexOf(valuelist);
			this.valuelists.splice(index, 1);
		}
	}-*/;
}
