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

import java.util.List;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Any;
import jsinterop.base.JsArrayLike;
import jsinterop.base.JsPropertyMap;

/**
 * @author jcomp
 *
 */
public class FormService implements IService
{

	private final MobileClient mobileClient;

	/**
	 * @param mobileClient
	 */
	public FormService(MobileClient mobileClient)
	{
		this.mobileClient = mobileClient;
	}

	@Override
	public JsPlainObj execute(ServiceCallObject serviceCall)
	{
		JsPropertyMap<Object> args = serviceCall.getArgs();
		switch (serviceCall.getMethodName())
		{
			case "executeEvent" :
			{
				dataP1ush(args);
				executeEvent(args);
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("nls")
	private void executeEvent(JsPropertyMap<Object> args)
	{
		String formName = args.getAsAny("formname").asString();
		String beanName = args.getAsAny("beanname").asString();
		String eventType = args.getAsAny("event").asString();
		JsArrayLike<Object> jsargs = args.getAsAny("args").asArrayLike();
		List<Object> asList = jsargs.asList();

		FormController formController = mobileClient.getFormManager().getForm(formName);
		WebRuntimeComponent component = formController.getView().getComponent(beanName);
		Any methodName = component.getJSONProperty(eventType);
		if (methodName != null)
			formController.getExecutor().fireEventCommand(eventType, methodName.asString(), component, asList.toArray());
		else MobileClient.log("No method found for event " + eventType + " on " + formName + "." + beanName);

	}

	/**
	 * @param args
	 */
	private void dataP1ush(JsPropertyMap<Object> args)
	{
		// TODO Auto-generated method stub

	}

}
