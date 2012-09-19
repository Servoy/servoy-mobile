package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.servoy.mobile.client.ui.FormPage;

@Export
public class Controller implements Exportable
{

	private final FormPage page;

	public Controller(FormPage page)
	{
		this.page = page;
	}

	public String getName()
	{
		return page.getName();
	}
}
