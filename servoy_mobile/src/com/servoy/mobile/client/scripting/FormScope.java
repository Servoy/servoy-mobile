package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dto.DataProviderDescription;
import com.servoy.mobile.client.dto.EntityDescription;

/**
 * 
 * @author jcompagner
 * @since 7.0
 */
@SuppressWarnings("nls")
public class FormScope extends GlobalScope
{
	protected final Map<String, Integer> recordTypes = new HashMap<String, Integer>();
	private final FormController formController;

	public FormScope(MobileClient application, FormController formController)
	{
		super(formController.getName(), application);
		this.formController = formController;

		FoundSet fs = formController.getFormModel();
		if (fs != null)
		{
			EntityDescription ed = fs.getFoundSetManager().getEntityDescription(fs.getEntityName());
			JsArray<DataProviderDescription> dataProviders = ed.getDataProviders();

			for (int k = 0; k < dataProviders.length(); k++)
			{
				DataProviderDescription dataProviderDescription = dataProviders.get(k);
				recordTypes.put(dataProviderDescription.getName(), Integer.valueOf(dataProviderDescription.getType()));
			}
		}
		servoyProperties.put("elements", new ElementScope());
		servoyProperties.put("controller", formController);
	}

	public FormController getController()
	{
		return (FormController)servoyProperties.get("controller");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.GlobalScope#getVariableType(java.lang.String)
	 */
	@Override
	public int getVariableType(String variable)
	{
		int type = super.getVariableType(variable);
		if (type == -4)
		{
			Integer recordType = recordTypes.get(variable);
			if (recordType != null) type = recordType.intValue();
		}
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.GlobalScope#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String variable)
	{
		if ("foundset".equals(variable)) return formController.getFormModel();
		if (recordTypes.containsKey(variable))
		{
			return formController.getFormModel().getSelectedRecord().getValue(variable);
		}
		FoundSet rfs = formController.getFormModel().getSelectedRecord().getRelatedFoundSet(variable);
		if (rfs != null) return rfs;
		return super.getValue(variable);
	}
}
