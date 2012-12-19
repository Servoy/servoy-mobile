package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.Record;
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

		String ds = formController.getForm().getDataSource();
		if (ds != null)
		{
			EntityDescription ed = application.getFoundSetManager().getEntityDescription(FoundSetManager.getEntityFromDataSource(ds));
			JsArray<DataProviderDescription> dataProviders = ed.getDataProviders();

			for (int k = 0; k < dataProviders.length(); k++)
			{
				DataProviderDescription dataProviderDescription = dataProviders.get(k);
				recordTypes.put(dataProviderDescription.getName(), Integer.valueOf(dataProviderDescription.getType()));
				exportProperty(dataProviderDescription.getName());
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
		FoundSet foundSet = formController.getFormModel();

		if ("foundset".equals(variable)) return foundSet;

		if (foundSet != null)
		{
			Record selRecord = foundSet.getSelectedRecord();
			if (selRecord != null)
			{
				if (recordTypes.containsKey(variable))
				{
					return selRecord.getValue(variable);
				}

				FoundSet rfs = selRecord.getRelatedFoundSet(variable);
				if (rfs != null) return rfs;
			}
		}

		return super.getValue(variable);
	}

	/**
	 * 
	 */
	public ElementScope getElementScope()
	{
		return (ElementScope)servoyProperties.get("elements");
	}
}
