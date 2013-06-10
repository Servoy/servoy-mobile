package com.servoy.mobile.client.ui;

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

import java.util.ArrayList;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Component;

/**
 * Form display in list view mode
 * @author gboros
 *
 */
public class ListFormDisplay extends FormDisplay
{

	public ListFormDisplay(MobileClient application, FormController formController)
	{
		super(application, formController);
	}

	@Override
	public FormPage getDisplayPage()
	{
		if (formPage == null)
		{
			formPage = new ListFormPage(application, formController);
			initDisplay(formPage);
		}
		return formPage;
	}

	@Override
	public FormPanel getDisplayPanel(String parentFormName)
	{
		FormPanel formPanel = null;
		if (formPanelMap.get(parentFormName) == null)
		{
			formPanel = new ListFormPanel(application, formController);
			formPanelMap.put(parentFormName, formPanel);
			initDisplay(formPanel);
		}
		return formPanel;
	}

	@Override
	public void createContent(IFormComponent formComponent, ArrayList<Component> contentComponents)
	{
		FormList formList = new FormList(formController, formController.getForm().getComponents(), formComponent.getDataAdapter(), null);
		((ISupportFormList)formComponent).addFormList(formList);
	}

	class ListFormPage extends FormPage implements ISupportFormList
	{
		private FormList formList;

		public ListFormPage(MobileClient application, FormController formController)
		{
			super(application, formController);
		}

		@Override
		protected void onPageBeforeShow()
		{
			super.onPageBeforeShow();
			formList.refreshList();
		}

		@Override
		public void destroy()
		{
			formList.destroy();
			formList = null;
			super.destroy();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.servoy.mobile.client.ui.ListFormDisplay.ISupportFormList#addFormList(com.servoy.mobile.client.ui.FormList)
		 */
		@Override
		public void addFormList(FormList fl)
		{
			formList = fl;
			add(fl);
		}
	}

	class ListFormPanel extends FormPanel implements ISupportFormList
	{
		private FormList formList;

		public ListFormPanel(MobileClient application, FormController formController)
		{
			super(application, formController);
		}

		@Override
		protected void onPanelBeforeOpen()
		{
			super.onPanelBeforeOpen();
			formList.refreshList();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.servoy.mobile.client.ui.ListFormDisplay.ISupportFormList#addFormList(com.servoy.mobile.client.ui.FormList)
		 */
		@Override
		public void addFormList(FormList fl)
		{
			this.formList = fl;
			add(fl);
		}
	}

	interface ISupportFormList
	{
		void addFormList(FormList formList);
	}
}