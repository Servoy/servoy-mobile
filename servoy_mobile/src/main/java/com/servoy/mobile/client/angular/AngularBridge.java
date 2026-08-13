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
	public static final String APPLICATION_SERVICE = "$applicationService";

	public static final String TYPES_REGISTRY_SERVICE = "$typesRegistry";


	private final MobileClient mobileClient;

	private final Map<String, IService> services = new HashMap<String, IService>();

	private final WindowService windowService;

	private boolean firstCall = true;

	private final ServerScriptManager serverScriptManager;

	public AngularBridge(MobileClient mobileClient)
	{
		this.mobileClient = mobileClient;
		this.windowService = new WindowService(mobileClient);
		addEventListener(this);
		services.put(WindowService.WINDOW_SERVICE, windowService);
		services.put("i18nService", new I18NService(mobileClient));
		services.put("formService", new FormService(mobileClient));
		this.serverScriptManager = new ServerScriptManager(this);
	}

	/**
	 * @return the windowService
	 */
	public WindowService getWindowService()
	{
		return windowService;
	}

	public ServerScriptManager getServerScriptManager()
	{
		return serverScriptManager;
	}

	public MobileClient getMobileClient()
	{
		return mobileClient;
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


			String styleSheet = mobileClient.getFlattenedSolution().getStyleSheet();
			if (styleSheet != null)
			{
				executeServiceCall(APPLICATION_SERVICE, "setStyleSheets", new Object[] { new Object[] { styleSheet } });
			}

			// register the component/service client-side types with the Angular TypesRegistry (same $typesRegistry calls a real
			// NGClient sends over the websocket). Services first, then components, both before the first form's data (SVY-21262).
			Object serviceClientSideTypes = getServiceClientSideTypes();
			if (serviceClientSideTypes != null)
			{
				executeServiceCall(TYPES_REGISTRY_SERVICE, "setServiceClientSideSpecs", new Object[] { serviceClientSideTypes });
			}
			Object componentClientSideTypes = getComponentClientSideTypes();
			if (componentClientSideTypes != null)
			{
				executeServiceCall(TYPES_REGISTRY_SERVICE, "addComponentClientSideSpecs", new Object[] { componentClientSideTypes });
			}

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
			serverScriptManager.flushAllChanges();
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
		else if (serverScriptManager != null && service.getMethodName() != null)
		{
			String serviceName = service.getServiceName();
			String methodName = service.getMethodName();
			if ("applicationServerService".equals(serviceName) && "callServerSideApi".equals(methodName))
			{
				String targetService = getArgStringProperty(service.getArgs(), "service");
				String targetMethod = getArgStringProperty(service.getArgs(), "methodName");
				Object[] targetArgs = getArgNestedArray(service.getArgs(), "args");
				if (targetService != null && targetMethod != null)
				{
					if (serverScriptManager.hasServerScript(targetService, targetMethod))
					{
						serverScriptManager.executeServerScript(targetService, targetMethod, targetArgs);
					}
					else if (serverScriptManager.hasInternalHandler(targetService, targetMethod))
					{
						serverScriptManager.executeInternalHandler(targetService, targetMethod, targetArgs);
					}
				}
				serverScriptManager.flushAllChanges();
				String cmsgId = service.getCmsgId();
				if (cmsgId != null)
				{
					JsPlainObj obj = new JsPlainObj();
					obj.set("cmsgid", cmsgId);
					obj.set("ret", (Object)null);
					sendMessage(obj.toJSONString());
				}
			}
			else if (serverScriptManager.hasInternalHandler(serviceName, methodName))
			{
				Object[] args = getArgsArray(service.getArgs());
				serverScriptManager.executeInternalHandler(serviceName, methodName, args);
				serverScriptManager.flushAllChanges();
				String cmsgId = service.getCmsgId();
				if (cmsgId != null)
				{
					JsPlainObj obj = new JsPlainObj();
					obj.set("cmsgid", cmsgId);
					obj.set("ret", (Object)null);
					sendMessage(obj.toJSONString());
				}
			}
			else if (serverScriptManager.hasServerScript(serviceName, methodName))
			{
				Object[] args = getArgsArray(service.getArgs());
				serverScriptManager.executeServerScript(serviceName, methodName, args);
				serverScriptManager.flushAllChanges();
				String cmsgId = service.getCmsgId();
				if (cmsgId != null)
				{
					JsPlainObj obj = new JsPlainObj();
					obj.set("cmsgid", cmsgId);
					obj.set("ret", (Object)null);
					sendMessage(obj.toJSONString());
				}
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

	private native static Object getComponentClientSideTypes() /*-{
        return $wnd._clientsidetypes_;
    }-*/;

	private native static Object getServiceClientSideTypes() /*-{
        return $wnd._serviceclientsidetypes_;
    }-*/;

	private native static Object[] getArgsArray(Object args) /*-{
		if (!args) return null;
		if (Array.isArray(args)) return args;
		return Array.prototype.slice.call(args);
	}-*/;

	private native static String getArgStringProperty(Object args, String key) /*-{
		if (!args) return null;
		return args[key] || null;
	}-*/;

	private native static Object[] getArgNestedArray(Object args, String key) /*-{
		if (!args || !args[key]) return null;
		var val = args[key];
		if (Array.isArray(val)) return val;
		return [];
	}-*/;


	public void sendServiceModelChange(String serviceName, String propertyName, Object value)
	{
		JsPlainObj propertyObj = new JsPlainObj();
		propertyObj.set(propertyName, value);

		JsPlainObj servicesObj = new JsPlainObj();
		servicesObj.set(serviceName, propertyObj);

		JsPlainObj msgObj = new JsPlainObj();
		msgObj.set("services", servicesObj);

		JsPlainObj envelope = new JsPlainObj();
		envelope.set("msg", msgObj);

		String message = envelope.toJSONString();
		MobileClient.log("GWT sending SERVICE MODEL to Angular " + message);
		sendMessage(message);
	}

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
