package com.servoy.mobile.client.scripting;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.solutionmodel.JSForm;


public class FormScope extends GlobalScope 
{
	public FormScope(MobileClient application, JSForm form) {
		super(form.getName());
		// TODO form.getDataSource()
//		if (form.getDataSource() != null) {
//			FoundSet fs = application.getFoundSetManager().getFoundSet(form.getDataSource());
//			servoyProperties.put("foundset", fs);
//		}
		servoyProperties.put("elements", new ElementScope());
		servoyProperties.put("controller", new Controller());
	}
}
