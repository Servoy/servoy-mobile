/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2024 Servoy BV

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

package com.servoy.mobile.client.angular;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;

/**
 * @author jcomp
 *
 */
public class WindowService implements IService
{
	public static final String WINDOW_SERVICE = "$windowService";

	private final MobileClient client;

	public WindowService(MobileClient client)
	{
		this.client = client;
	}

	@Override
	public JsPlainObj execute(ServiceCallObject serviceCallObject)
	{
		return null;
	}

	public void switchForm(FormController currentForm)
	{
		JsPlainObj mainForm = new JsPlainObj();
		JsPlainObj size = new JsPlainObj();
		size.set("width", currentForm.getForm().getSize());
		size.set("height", currentForm.getForm().getSize());
		mainForm.set("size", size);
		mainForm.set("name", currentForm.getName());


		JsPlainObj navigatorForm = new JsPlainObj();
		if (currentForm.getNavigator() != null) navigatorForm.set("name", currentForm.getNavigator());
		// todo now always just 0 sizes..
		JsPlainObj size2 = new JsPlainObj();
		size2.set("width", 0);
		size2.set("height", 0);
		navigatorForm.set("size", size2);


		client.getAngularBridge().executeServiceCall(WINDOW_SERVICE, "switchForm", new Object[] { "1", mainForm, navigatorForm, false });

//		.executeAsyncServiceCall("switchForm",
//			new Object[] { getName(), mainForm, navigatorForm, isLoginForm });

	}


}
