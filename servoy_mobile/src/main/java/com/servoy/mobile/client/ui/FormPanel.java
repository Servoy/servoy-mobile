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

import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.FoundSet;
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
	private final IFormDisplay formDisplay;
	private boolean isShown;

	public FormPanel(IFormDisplay formDisplay)
	{
		this.formDisplay = formDisplay;
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
	 * @see com.servoy.mobile.client.ui.IFormComponent#addNavigator(com.servoy.mobile.client.ui.FormPanel)
	 */
	@Override
	public void addNavigator(String navigatorFormName)
	{
	}

	@Override
	protected void onPanelBeforeOpen()
	{
		FoundSet foundSet = formDisplay.getFormController().getFormModel();
		if (foundSet != null) formDisplay.refreshRecord(foundSet.getSelectedRecord());
		else formDisplay.refreshRecord(null);
	}

	@Override
	protected void onPanelOpen()
	{
		isShown = true;
		formDisplay.getFormController().executeOnShowMethod();
	}

	@Override
	protected void onPanelClose()
	{
		isShown = false;
		FormController formDisplayController = formDisplay.getFormController();
		formDisplayController.executeOnHideMethod();
		if (formDisplayController.isMarkedForCleanup())
		{
			formDisplayController.cleanup();
		}
	}

	public String getName()
	{
		return formDisplay.getFormController().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#removeHeader()
	 */
	@Override
	public void removeHeader()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#removeFooter()
	 */
	@Override
	public void removeFooter()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#removeWidget(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void removeWidget(Widget w)
	{
		remove(w);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#getFormDisplay()
	 */
	@Override
	public IFormDisplay getFormDisplay()
	{
		return formDisplay;
	}

	@Override
	public boolean isShown()
	{
		return isShown;
	}
}