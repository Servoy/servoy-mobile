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
import com.servoy.mobile.client.ui.ApiSpec;

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

	/**
	 * Counter for smsgids GWT includes in outgoing sync serviceApis/componentApis messages.
	 * Angular echoes the smsgid back in its response so GWT can resolve the matching Promise.
	 * Starts at 1; the Angular-side nextMessageId (used for cmsgid) is a separate namespace.
	 */
	private int nextSmsgId = 1;

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

		// Check first: is this a response to a GWT-initiated sync API call?
		// Angular echoes back {smsgid, ret} on success or {smsgid, err} on failure after
		// processing a serviceApis/componentApis message that GWT sent with an smsgid.
		String incomingSmsgId = service.getSmsgId();
		if (incomingSmsgId != null)
		{
			String err = service.getErr();
			if (err != null)
			{
				rejectPendingApiCall(incomingSmsgId, err);
			}
			else
			{
				resolvePendingApiCall(incomingSmsgId, service.getRet());
			}
			return;
		}

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


	/**
	 * Generates the next smsgid string for a GWT-initiated sync API call.
	 */
	public String nextSyncApiCallSmsgId()
	{
		return "gwt-" + (nextSmsgId++);
	}

	/**
	 * Creates a native JS Promise for a sync API call and stores both its resolve and reject
	 * functions keyed by smsgid, along with the ApiSpec so the return value can be converted.
	 * The caller must include that smsgid in the outgoing serviceApis/componentApis message so
	 * Angular echoes it back; then {@link #resolvePendingApiCall} or {@link #rejectPendingApiCall}
	 * will settle the promise.
	 */
	public native Object createSyncApiCallPromise(String smsgId, ApiSpec apiSpec) /*-{
		var self = this;
		if (!self._pendingApiCalls) self._pendingApiCalls = {};
		return new Promise(function(resolve, reject) {
			self._pendingApiCalls[smsgId] = { resolve: resolve, reject: reject, apiSpec: apiSpec };
		});
	}-*/;

	/**
	 * Proxy so JSNI in this class can call {@link MobileClient#convertApiReturnValue} without
	 * needing to chain two field references in JSNI notation.
	 */
	public Object convertApiReturnValue(Object rawValue, ApiSpec apiSpec)
	{
		return mobileClient.convertApiReturnValue(rawValue, apiSpec);
	}

	/**
	 * Resolves the pending Promise stored for the given smsgid with the return value from Angular.
	 * The raw value is first run through {@link MobileClient#convertApiReturnValue} so that
	 * typed values (e.g. dates encoded as {_T,_V}) are converted before reaching solution code.
	 * Called from {@link #onAngularEvent} when it detects an incoming {smsgid, ret} response.
	 */
	private native void resolvePendingApiCall(String smsgId, Object retValue) /*-{
		if (this._pendingApiCalls && this._pendingApiCalls[smsgId]) {
			var entry = this._pendingApiCalls[smsgId];
			delete this._pendingApiCalls[smsgId];
			var converted = this.@com.servoy.mobile.client.angular.AngularBridge::convertApiReturnValue(Ljava/lang/Object;Lcom/servoy/mobile/client/ui/ApiSpec;)(retValue, entry.apiSpec);
			entry.resolve(converted);
		} else {
			@com.servoy.mobile.client.MobileClient::log(Ljava/lang/String;)("GWT: no pending promise found for smsgid: " + smsgId);
		}
	}-*/;

	/**
	 * Rejects the pending Promise stored for the given smsgid with the error from Angular.
	 * Called from {@link #onAngularEvent} when it detects an incoming {smsgid, err} response,
	 * which Angular sends when client-side execution of the API call failed.
	 * The rejection propagates as a thrown exception at the {@code await} site in solution code.
	 */
	private native void rejectPendingApiCall(String smsgId, String errorMessage) /*-{
		if (this._pendingApiCalls && this._pendingApiCalls[smsgId]) {
			var handlers = this._pendingApiCalls[smsgId];
			delete this._pendingApiCalls[smsgId];
			handlers.reject(new Error(errorMessage));
		} else {
			@com.servoy.mobile.client.MobileClient::log(Ljava/lang/String;)("GWT: no pending promise found for smsgid (reject): " + smsgId);
		}
	}-*/;

	/**
	 * Rejects all currently pending sync API call promises with the given reason.
	 * Should be called when the Angular connection is lost or the client is shutting down,
	 * so that any suspended async solution functions are unblocked with an error rather
	 * than hanging indefinitely.
	 */
	public native void rejectAllPendingApiCalls(String reason) /*-{
		if (!this._pendingApiCalls) return;
		var err = new Error(reason);
		var pending = this._pendingApiCalls;
		this._pendingApiCalls = {};
		for (var smsgId in pending) {
			if (pending.hasOwnProperty(smsgId)) {
				pending[smsgId].reject(err);
			}
		}
	}-*/;

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

        // Reject all pending sync API call promises when the page is being torn down so that
        // any suspended async solution functions are unblocked with an error rather than
        // hanging indefinitely. 'pagehide' fires on mobile browsers; 'unload' is the fallback.
        var onTeardown = function() {
            servoyAngularBridge.@com.servoy.mobile.client.angular.AngularBridge::rejectAllPendingApiCalls(Ljava/lang/String;)("Mobile client page unloaded");
        };
        window.addEventListener("pagehide", onTeardown);
        window.addEventListener("unload", onTeardown);
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
