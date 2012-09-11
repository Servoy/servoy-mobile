package com.servoy.mobile.client.scripting;

import com.servoy.mobile.client.ui.FormPage;

public class Controller
{

	private final FormPage page;

	public Controller(FormPage page)
	{
		this.page = page;
		export();
	}

	public String getName()
	{
		return page.getName();
	}

	public native void export() /*-{
		this.getName = function() {
			return this.@com.servoy.mobile.client.scripting.Controller::getName()();
		}
	}-*/;
}
