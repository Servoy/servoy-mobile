/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */

package com.servoy.mobile.client.dataprocessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.GlobalScope;

/**
 * This class encapsulates all the dataproviders for a form page, it does the creation and setup of dataAdapters.
 * 
 * @author gboros
 */
public class DataAdapterList
{
	private final MobileClient application;
	private final FormController formController;
	private final LinkedHashMap<String, IDataAdapter> dataAdapters = new LinkedHashMap<String, IDataAdapter>();
	private final ArrayList<IDisplayRelatedData> relatedDataAdapters = new ArrayList<IDisplayRelatedData>();

	public DataAdapterList(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;
	}

	public void addFormObject(Object obj)
	{
		if (obj instanceof IDisplayData)
		{
			IDisplayData displayData = (IDisplayData)obj;
			String dataproviderID = displayData.getDataProviderID();
			if (dataproviderID != null)
			{
				IDataAdapter dataAdapter = dataAdapters.get(dataproviderID);
				if (dataAdapter == null)
				{
					dataAdapter = new DisplaysAdapter(application, this, dataproviderID);
					dataAdapters.put(dataproviderID, dataAdapter);
				}

				if (dataAdapter instanceof DisplaysAdapter) ((DisplaysAdapter)dataAdapter).addDisplay(displayData);

				if (displayData.needEditListener() && dataAdapter instanceof IEditListener)
				{
					displayData.addEditListener((IEditListener)dataAdapter);
				}
			}
		}
		else if (obj instanceof IDisplayRelatedData)
		{
			relatedDataAdapters.add((IDisplayRelatedData)obj);
		}
	}

	public FormScope getFormScope()
	{
		return formController.getFormScope();
	}

	public void setRecord(Record record)
	{
		Iterator<IDataAdapter> it = dataAdapters.values().iterator();
		while (it.hasNext())
		{
			IDataAdapter da = it.next();
			da.setRecord(record);
		}

		for (IDisplayRelatedData relatedDataAdapter : relatedDataAdapters)
			relatedDataAdapter.setRecord(record);
	}

	public Object getRecordValue(Record record, String dataproviderID)
	{
		Object recordValue = null;
		if (dataproviderID != null)
		{
			String[] globalVariableScope = GlobalScope.getVariableScope(dataproviderID);

			if (globalVariableScope[0] != null)
			{
				recordValue = application.getGlobalScope().getValue(globalVariableScope[1]);
			}
			else if (getFormScope().hasVariable(dataproviderID))
			{
				recordValue = getFormScope().getVariableValue(dataproviderID);
			}
			else if (record != null)
			{
				recordValue = record.getValue(dataproviderID);
			}
		}
		return recordValue;
	}
}
