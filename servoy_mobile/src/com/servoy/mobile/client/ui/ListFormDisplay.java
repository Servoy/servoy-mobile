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
import com.servoy.mobile.client.persistence.Form;

/**
 * Form display in list view mode
 * @author gboros
 *
 */
public class ListFormDisplay extends FormDisplay
{
	private final ListFormPage listFormPage;

	public ListFormDisplay(MobileClient application, Form form, FormController formController)
	{
		listFormPage = new ListFormPage(application, form, formController);
	}

	@Override
	public FormPage getDisplayPage()
	{
		return listFormPage;
	}

	class ListFormPage extends FormPage
	{
		public ListFormPage(MobileClient application, Form form, FormController formController)
		{
			super(application, form, formController);
		}

		@Override
		public void createContent(ArrayList<Component> contentComponents)
		{
			add(new FormList(form));
		}
	}
}