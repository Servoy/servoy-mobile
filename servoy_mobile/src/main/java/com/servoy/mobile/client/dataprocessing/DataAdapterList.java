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

import com.servoy.base.util.ITagResolver;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.ui.TitleText;
import com.servoy.mobile.client.util.IDestroyable;

/**
 * This class encapsulates all the dataproviders for a form page, it does the creation and setup of dataAdapters.
 *
 * @author gboros
 */
public class DataAdapterList implements IModificationListener, ITagResolver
{
	private final MobileClient application;
	private FormController formController;
	private final LinkedHashMap<String, IDataAdapter> dataAdapters = new LinkedHashMap<String, IDataAdapter>();
	private final ArrayList<IDisplayRelatedData> relatedDataAdapters = new ArrayList<IDisplayRelatedData>();
	private final ArrayList<IDestroyable> destroyables = new ArrayList<IDestroyable>();

	private Record record;

	public DataAdapterList(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;

		application.getScriptEngine().getGlobalScopeModificationDelegate().addModificationListener(this);
		formController.getFormScope().addModificationListener(this);
	}

	public void addFormObject(Object obj)
	{
		if (obj instanceof IDisplayData)
		{
			IDisplayData displayData = (IDisplayData)obj;
			String dataproviderID = displayData instanceof IRuntimeComponentProvider
				? ((IRuntimeComponentProvider)displayData).getRuntimeComponent().getDataProviderID() : displayData instanceof TitleText
					? ((TitleText)displayData).getDataProviderID() : null;

			IDataAdapter dataAdapter = dataAdapters.get(dataproviderID);
			if (dataAdapter == null)
			{
				dataAdapter = new DisplaysAdapter(application, this, dataproviderID);
				dataAdapters.put(dataproviderID, dataAdapter);
			}

			if (dataAdapter instanceof DisplaysAdapter) ((DisplaysAdapter)dataAdapter).addDisplay(displayData);

			if (displayData instanceof IEditListenerSubject && dataAdapter instanceof IEditListener)
			{
				((IEditListenerSubject)displayData).addEditListener((IEditListener)dataAdapter);
			}
		}
		else if (obj instanceof IDisplayRelatedData)
		{
			relatedDataAdapters.add((IDisplayRelatedData)obj);
		}

		if (obj instanceof IDestroyable)
		{
			destroyables.add((IDestroyable)obj);
		}
	}

	public FormScope getFormScope()
	{
		return formController.getFormScope();
	}

	public void setRecord(Record record)
	{
		if (this.record != null) this.record.removeModificationListener(this);
		this.record = record;
		if (this.record != null) this.record.addModificationListener(this);

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
				recordValue = application.getScriptEngine().getGlobalScope(globalVariableScope[0]).getValue(globalVariableScope[1]);
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

	/*
	 * @see com.servoy.mobile.client.scripting.IModificationListener#valueChanged(com.servoy.mobile.client.scripting.ModificationEvent)
	 */
	@Override
	public void valueChanged(ModificationEvent e)
	{
		Iterator<IDataAdapter> it = dataAdapters.values().iterator();
		while (it.hasNext())
		{
			IDataAdapter da = it.next();
			da.valueChanged(e);
		}
	}

	public void destroy()
	{
		if (this.record != null) this.record.removeModificationListener(this);
		application.getScriptEngine().getGlobalScopeModificationDelegate().removeModificationListener(this);
		formController.getFormScope().removeModificationListener(this);

		dataAdapters.clear();
		relatedDataAdapters.clear();
		for (IDestroyable destroyable : destroyables)
		{
			destroyable.destroy();
		}
		destroyables.clear();

		formController = null;
		record = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.util.ITagResolver#getStringValue(java.lang.String)
	 */
	@Override
	public String getStringValue(String name)
	{
		Object valueObj = getRecordValue(record, name);
		return valueObj != null ? valueObj.toString() : null;
	}
}
