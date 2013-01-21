package com.servoy.mobile.client.dto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class RelationDescription extends JavaScriptObject
{
	protected RelationDescription()
	{
	}

	public final native int getID() /*-{
		if (!this.id)
			return 0;
		return this.id;
	}-*/;

	public final native void setID(int id) /*-{
		this.id = id;
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native JsArrayString getPrimaryDataProviders() /*-{
		return this.primaryDataProviders;
	}-*/;

	public final native JsArrayString getForeignColumns() /*-{
		return this.foreignColumns;
	}-*/;

	public final native boolean getAllowCreationRelatedRecords() /*-{
		return this.allowCreationRelatedRecords;
	}-*/;

	public final native String getPrimaryEntityName() /*-{
		return this.primaryEntityName;
	}-*/;

	public final native String getForeignEntityName() /*-{
		return this.foreignEntityName;
	}-*/;

	public final native boolean isSelfRef() /*-{
		if (this.selfRef)
			return true;
		return false;
	}-*/;

	public final native void setSelfRef(boolean selfRef) /*-{
		this.selfRef = selfRef;
	}-*/;


	public static RelationDescription newInstance(String name, String primaryEntityName, String foreignEntityName)
	{
		RelationDescription rd = JavaScriptObject.createObject().cast();
		rd.init(name, primaryEntityName, foreignEntityName);
		return rd;
	}

	private final native void init(String name, String primaryEntityName, String foreignEntityName) /*-{
		this.name = name;
		this.primaryEntityName = primaryEntityName;
		this.foreignEntityName = foreignEntityName;
	}-*/;
}
