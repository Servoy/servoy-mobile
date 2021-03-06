package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
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
	protected final Map<String, Integer> recordTypes;
	private FormController formController;

	public FormScope(MobileClient application, FormController formController)
	{
		super(formController.getName(), application);
		this.formController = formController;

		String ds = formController.getForm().getDataSource();
		if (ds != null)
		{
			recordTypes = application.getFoundSetManager().exportColumns(FoundSetManager.getEntityFromDataSource(ds), this, this);
		}
		else recordTypes = new HashMap<String, Integer>();
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
		if (type == IColumnTypeConstants.MEDIA)
		{
			Integer recordType = recordTypes.get(variable);
			if (recordType != null)
			{
				if (formController.getFormModel() != null && formController.getFormModel().isInFind())
				{
					type = IColumnTypeConstants.TEXT;
				}
				else
				{
					type = recordType.intValue();
				}
			}
			else
			{
				int dotIndex = variable.lastIndexOf('.');
				if (dotIndex != -1)
				{
					// relation
					String relationpart = variable.substring(0, dotIndex);
					EntityDescription relatedEntityDescription = client.getFoundSetManager().getRelatedEntityDescription(
						FoundSetManager.getEntityFromDataSource(formController.getDataSource()), relationpart);
					if (relatedEntityDescription != null)
					{
						String relDataProvider = variable.substring(dotIndex + 1);
						JsArray<DataProviderDescription> dataProviders = relatedEntityDescription.getDataProviders();
						for (int i = 0; i < dataProviders.length(); i++)
						{
							if (dataProviders.get(i).getDataProviderID().equals(relDataProvider))
							{
								type = dataProviders.get(i).getType();
								break;
							}
						}
					}
				}
			}
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

	@Override
	public void setValue(String variable, Object value)
	{
		FoundSet foundSet = formController.getFormModel();
		if (foundSet != null)
		{
			Record selRecord = foundSet.getSelectedRecord();
			if (selRecord != null)
			{
				if (recordTypes.containsKey(variable))
				{
					selRecord.setValue(variable, value);
					return;
				}
			}
		}
		super.setValue(variable, value);
	}

	/**
	 * 
	 */
	public ElementScope getElementScope()
	{
		return (ElementScope)servoyProperties.get("elements");
	}

	public void destroy()
	{
		formController = null;
		if (getElementScope() != null)
		{
			getElementScope().destroy();
		}
		servoyProperties.clear();
		ScriptEngine.clearScope(this);
	}
}
