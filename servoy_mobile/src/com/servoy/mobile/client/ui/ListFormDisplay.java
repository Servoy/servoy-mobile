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

import com.google.gwt.user.client.ui.Widget;
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

	private FormList formList;

	public ListFormDisplay(MobileClient application, FormController formController)
	{
		super(application, formController);
	}

	@Override
	protected ArrayList<Widget> createContent(ArrayList<Component> contentComponents)
	{
		ArrayList<Widget> contentList = new ArrayList<Widget>();

		formList = new FormList(formController, formController.getForm().getComponents(), dal, null);
		contentList.add(formList);

		return contentList;
	}

	@Override
	public void cleanup()
	{
		formList.destroy();
		formList = null;
		super.cleanup();
	}

}