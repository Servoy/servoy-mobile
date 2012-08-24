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

import com.servoy.mobile.client.solutionmodel.JSComponent;
import com.servoy.mobile.client.solutionmodel.JSForm;
import com.servoy.mobile.client.solutionmodel.JSSolutionModel;

/**
 * Form display in list view mode
 * @author gboros
 *
 */
public class ListFormDisplay implements IFormDisplay
{
	private ListFormPage listFormPage;
	
	public ListFormDisplay(JSSolutionModel solutionModel, JSForm form)
	{
		listFormPage = new ListFormPage(solutionModel, form);
	}
	
	@Override
	public FormPage getDisplayPage()
	{
		return listFormPage;
	}

	class ListFormPage extends FormPage
	{
		private JSForm form;
		public ListFormPage(JSSolutionModel solutionModel, JSForm form)
		{
			super(solutionModel, form);
			this.form = form;
		}
		
		@Override
		public void createContent(ArrayList<JSComponent> contentComponents)
		{
			add(new FormList(form));
		}
	}
}