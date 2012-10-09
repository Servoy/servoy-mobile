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

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.scripting.GlobalScope;

/**
 * This adapter is a kind of model between the display(s) and the state.
 * 
 * @author gboros
 */
public class DisplaysAdapter implements IDataAdapter, IEditListener
{
	private final MobileClient application;
	private final DataAdapterList dal;
	private final String dataproviderID;
	private final ArrayList<IDisplayData> displays = new ArrayList<IDisplayData>();
	private Record record;

	public DisplaysAdapter(MobileClient application, DataAdapterList dal, String dataproviderID)
	{
		this.application = application;
		this.dal = dal;
		this.dataproviderID = dataproviderID;
	}

	public void addDisplay(IDisplayData display)
	{
		displays.add(display);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDataAdapter#setRecord(com.servoy.mobile.client.dataprocessing.Record)
	 */
	@Override
	public void setRecord(Record record)
	{
		this.record = record;
		Object value = dal.getRecordValue(record, dataproviderID);

		for (IDisplayData d : displays)
			d.setValueObject(value);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDataAdapter#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return dataproviderID;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDataAdapter#addDataListener(com.servoy.mobile.client.dataprocessing.IDataAdapter)
	 */
	@Override
	public void addDataListener(IDataAdapter listner)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDataAdapter#removeDataListener(com.servoy.mobile.client.dataprocessing.IDataAdapter)
	 */
	@Override
	public void removeDataListener(IDataAdapter listner)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDataAdapter#displayValueChanged()
	 */
	@Override
	public void displayValueChanged()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IEditListener#startEdit(com.servoy.mobile.client.dataprocessing.IDisplayData)
	 */
	@Override
	public void startEdit(IDisplayData e)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IEditListener#commitEdit(com.servoy.mobile.client.dataprocessing.IDisplayData)
	 */
	@Override
	public void commitEdit(IDisplayData e)
	{
		if (this.dataproviderID == null) return;

		Object value = e.getValueObject();
		String[] globalVariableScope = GlobalScope.getVariableScope(dataproviderID);

		if (globalVariableScope[0] != null)
		{
			application.getGlobalScope().setValue(globalVariableScope[1], value);
		}
		else if (dal.getFormScope().hasVariable(dataproviderID))
		{
			dal.getFormScope().setVariableValue(dataproviderID, value);
		}
		else if (record != null)
		{
			record.setValue(dataproviderID, value);
		}
	}
}