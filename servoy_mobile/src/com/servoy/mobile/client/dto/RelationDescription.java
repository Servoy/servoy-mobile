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

	public final native JsArrayString getAllowCreationRelatedRecords() /*-{
		return this.allowCreationRelatedRecords;
	}-*/;

	public final native String getPrimaryEntityName() /*-{
		return this.primaryEntityName;
	}-*/;

	public final native String getForeignEntityName() /*-{
		return this.foreignEntityName;
	}-*/;
}
