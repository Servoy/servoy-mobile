package com.servoy.mobile.client.scripting;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;


public class FormScope extends GlobalScope
{
	public FormScope(MobileClient application, FormController formController)
	{
		super(formController.getName(), application);

		FoundSet fs = formController.getFormModel();
		if (fs != null) servoyProperties.put("foundset", fs);
		servoyProperties.put("elements", new ElementScope());
		servoyProperties.put("controller", formController);
	}

	public FormController getController()
	{
		return (FormController)servoyProperties.get("controller");
	}
}
