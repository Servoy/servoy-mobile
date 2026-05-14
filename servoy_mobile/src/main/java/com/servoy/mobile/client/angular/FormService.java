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
import com.servoy.mobile.client.ui.WebBaseComponent;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Any;
import jsinterop.base.Js;
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
				dataPush(Js.cast(args), false);
				executeEvent(Js.cast(args));
				break;
			}
			case "dataPush" :
			{
				dataPush(Js.cast(args), false);
				break;
			}
			case "svyPush" :
			{
				DataPush dataPush = Js.cast(args);
				Object oldValue = dataPush(dataPush, true);
				JsPropertyMap<Object> eventArgs = JsPropertyMap.of();
				eventArgs.set("formname", dataPush.getFormname());
				eventArgs.set("beanname", dataPush.getBeanname());
				eventArgs.set("event", "onDataChangeMethodID");
				Object[] newValue = new Object[1];
				dataPush.getChanges().forEach(key -> {
					newValue[0] = dataPush.getChanges().get(key);
				});
				Object[] values = new Object[] { oldValue, newValue[0] };
				eventArgs.set("args", values);
				executeEvent(Js.cast(eventArgs));
				break;
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("nls")
	private void executeEvent(EventCall eventCall)
	{
		String formName = eventCall.getFormname();
		String beanName = eventCall.getBeanname();
		String eventType = eventCall.getEvent();
		JsArrayLike<Object> jsargs = eventCall.getArgs();
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
	private Object dataPush(DataPush dataPush, boolean dataproviderPush)
	{
		// TODO Auto-generated method stub
		MobileClient.log("dataPush" + JSON.stringify(dataPush));

		JsPropertyMap<Object> changes = dataPush.getChanges();
		String formName = dataPush.getFormname();
		String beanName = dataPush.getBeanname();

		FormController formController = mobileClient.getFormManager().getForm(formName);
		WebBaseComponent component = beanName.length() > 0 ? formController.getView().getComponent(beanName) : formController.getView();

		Object[] oldValue = new Object[1];
		changes.forEach(key -> {
			Object value = changes.get(key);
			oldValue[0] = component.putBrowserProperty(key, value);

			if (dataproviderPush && component instanceof WebRuntimeComponent)
			{
				formController.getView().pushChanges((WebRuntimeComponent)component, key);
			}
		});
		return oldValue[0];

	}

}
