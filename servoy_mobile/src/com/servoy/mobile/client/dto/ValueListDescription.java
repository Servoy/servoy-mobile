package com.servoy.mobile.client.dto;

import org.timepedia.exporter.client.ExporterBaseActual.JsArrayObject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ValueListDescription extends JavaScriptObject 
{
	protected ValueListDescription() {}

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native JsArrayString getDiplayValues() /*-{
		return this.displayValues;
	}-*/;

	public final native JsArrayObject getRealValues() /*-{
		return this.realValues;
	}-*/;
	
	public final native boolean hasRealValues() /*-{
		return (this.realValues && this.realValues.lenght == this.displayValues.lenght);
	}-*/;
}
