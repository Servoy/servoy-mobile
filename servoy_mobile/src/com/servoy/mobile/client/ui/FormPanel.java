/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.mobile.client.ui;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.Record;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;
import com.sksamuel.jqm4gwt.toolbar.JQMPanel;

/**
 * Form panel UI
 * 
 * @author gboros
 *
 */
public class FormPanel extends JQMPanel implements IFormComponent
{
	private final FormController formController;
	private final DataAdapterList dal;

	public FormPanel(MobileClient application, FormController formController)
	{
		this.formController = formController;
		dal = new DataAdapterList(application, formController);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#addHeader(com.sksamuel.jqm4gwt.toolbar.JQMHeader)
	 */
	@Override
	public void addHeader(JQMHeader header)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#addFooter(com.sksamuel.jqm4gwt.toolbar.JQMFooter)
	 */
	@Override
	public void addFooter(JQMFooter footer)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#getDataAdapter()
	 */
	@Override
	public DataAdapterList getDataAdapter()
	{
		return dal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#addNavigator(com.servoy.mobile.client.ui.FormPanel)
	 */
	@Override
	public void addNavigator(String navigatorFormName)
	{
	}

	public void refreshRecord(Record r)
	{
		dal.setRecord(r);
	}

	@Override
	protected void onPanelBeforeOpen()
	{
		FoundSet foundSet = formController.getFormModel();
		if (foundSet != null) refreshRecord(foundSet.getSelectedRecord());
		else refreshRecord(null);
	}

	@Override
	protected void onPanelOpen()
	{
		formController.executeOnShowMethod();
	}

	@Override
	protected void onPanelClose()
	{
		formController.executeOnHideMethod();
	}

	public String getName()
	{
		return formController.getName();
	}
}
