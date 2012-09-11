package com.servoy.mobile.client.scripting;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.ui.FormPage;


public class FormScope extends GlobalScope
{
	public FormScope(MobileClient application, FormPage form)
	{
		super(form.getName(), application);
		if (form.getDataSource() != null)
		{
			FoundSet fs = application.getFoundSetManager().getFoundSet(FoundSetManager.getEntityFromDataSource(form.getDataSource()));
			servoyProperties.put("foundset", fs);
		}
		servoyProperties.put("elements", new ElementScope());
		servoyProperties.put("controller", new Controller(form));
	}

	public Controller getController()
	{
		return (Controller)servoyProperties.get("controller");
	}
}
