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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsonUtils;
import com.servoy.mobile.client.MobileClient;

/**
 * @author jcompagner
 * @since 2024.12.0
 *
 */
public class AngularBridge
{
	private final MobileClient mobileClient;

	private final Map<String, IService> services = new HashMap<String, IService>();

	private final WindowService windowService;

	private boolean firstCall = true;

	public AngularBridge(MobileClient mobileClient)
	{
		this.mobileClient = mobileClient;
		this.windowService = new WindowService(mobileClient);
		addEventListener(this);
		services.put(WindowService.WINDOW_SERVICE, windowService);
		services.put("i18nService", new I18NService(mobileClient));
		services.put("formService", new FormService(mobileClient));

	}

	/**
	 * @return the windowService
	 */
	public WindowService getWindowService()
	{
		return windowService;
	}

	protected void onAngularEvent(String message)
	{
		MobileClient.log("GWT received from Angular " + message);
		ServiceCallObject service = JsonUtils.safeEval(message);
		if (service.getServiceName() != null)
		{
			handleServiceCall(service);
		}
		if (firstCall)
		{
			firstCall = false;
			// first sent over the window and client nr
			JsPlainObj obj = new JsPlainObj();
			obj.set("windownr", "1");
			obj.set("clientnr", "1");
			JsPlainObj msg = new JsPlainObj();
			msg.set("msg", obj);
			String resultString = msg.toJSONString();
			sendMessage(resultString);

			// now show the first form
			mobileClient.onStartPageShown();
		}
	}

	/**
	 * @param service
	 */
	private void handleServiceCall(ServiceCallObject service)
	{
		IService iService = services.get(service.getServiceName());
		if (iService != null)
		{
			JsPlainObj result = iService.execute(service);
			String cmsgId = service.getCmsgId();
			if (cmsgId != null)
			{
				JsPlainObj obj = new JsPlainObj();
				obj.set("cmsgid", cmsgId);
				if (result != null)
				{
					obj.set("ret", result);
				}
				String resultString = obj.toJSONString();
				MobileClient.log("GWT sending to Angular " + resultString);
				sendMessage(resultString);
			}
		}
	}

	public void sendMessage(String message)
	{
		MobileClient.log("GWT sending to Angular " + message);
		nativeSendMessage(message);
	}

	private native static void addEventListener(AngularBridge bridge) /*-{
        var servoyAngularBridge = bridge;
        window.addEventListener(
        "message",
        function(e)
        {
            servoyAngularBridge.@com.servoy.mobile.client.angular.AngularBridge::onAngularEvent(Ljava/lang/String;)(e.data);
        });
    }-*/;

	private native static void nativeSendMessage(String message) /*-{
        parent.postMessage({from:'gwt',data:message});
    }-*/;

	/**
	 * @param windowService
	 * @param string
	 * @param objects
	 */
	public void executeServiceCall(String serviceName, String call, Object[] args)
	{
		JsPlainObj callObject = new JsPlainObj();
		callObject.set("call", call);
		callObject.set("name", serviceName);

		Array<Object> argObject = JsArrayHelper.createArray();
		for (Object arg : args)
		{
			argObject.push(arg);
		}
		callObject.set("args", argObject);

		Array<Object> serviceApis = JsArrayHelper.createArray();
		serviceApis.push(callObject);

		JsPlainObj serviceCall = new JsPlainObj();
		serviceCall.set("serviceApis", serviceApis);

		String message = serviceCall.toJSONString();
		MobileClient.log("GWT sending SERVICE to Angular " + message);
		sendMessage(message);
	}
}
