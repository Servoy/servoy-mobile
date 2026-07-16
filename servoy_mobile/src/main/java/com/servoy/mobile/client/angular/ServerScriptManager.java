package com.servoy.mobile.client.angular;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.client.MobileClient;

public class ServerScriptManager
{
	private final AngularBridge angularBridge;
	private final Map<String, ServiceScriptScope> scopes = new HashMap<>();
	private boolean initialized = false;

	public ServerScriptManager(AngularBridge angularBridge)
	{
		this.angularBridge = angularBridge;
		initialize();
	}

	private void initialize()
	{
		JavaScriptObject serverScriptsData = getServerScriptsData();
		if (serverScriptsData == null)
		{
			return;
		}
		JavaScriptObject services = getServicesSection(serverScriptsData);
		if (services == null)
		{
			return;
		}
		String[] serviceNames = getKeys(services);
		for (String serviceName : serviceNames)
		{
			JavaScriptObject serviceData = getProperty(services, serviceName);
			JavaScriptObject scriptFn = getProperty(serviceData, "script");
			if (scriptFn == null)
			{
				continue;
			}

			ServiceScriptScope scope = new ServiceScriptScope(serviceName, angularBridge);
			scope.initializeWithScript(scriptFn);
			scopes.put(serviceName, scope);
		}
		initialized = true;
		MobileClient.log("ServerScriptManager initialized with " + scopes.size() + " services");
	}

	public boolean hasServerScript(String serviceName, String methodName)
	{
		ServiceScriptScope scope = scopes.get(serviceName);
		if (scope == null)
		{
			return false;
		}
		return scope.hasApiFunction(methodName);
	}

	public Object executeServerScript(String serviceName, String methodName, Object[] args)
	{
		ServiceScriptScope scope = scopes.get(serviceName);
		if (scope == null)
		{
			return null;
		}
		return scope.invokeApiFunction(methodName, args);
	}

	public boolean hasInternalHandler(String serviceName, String methodName)
	{
		ServiceScriptScope scope = scopes.get(serviceName);
		if (scope == null)
		{
			return false;
		}
		return scope.hasInternalFunction(methodName);
	}

	public Object executeInternalHandler(String serviceName, String methodName, Object[] args)
	{
		ServiceScriptScope scope = scopes.get(serviceName);
		if (scope == null)
		{
			return null;
		}
		return scope.invokeInternalFunction(methodName, args);
	}

	public void flushAllChanges()
	{
		for (ServiceScriptScope scope : scopes.values())
		{
			if (scope.hasPendingChanges())
			{
				scope.flushChanges();
			}
		}
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	private static native JavaScriptObject getServerScriptsData() /*-{
		return $wnd._serverscriptsdata_ || null;
	}-*/;

	private static native JavaScriptObject getServicesSection(JavaScriptObject data) /*-{
		return data.services || null;
	}-*/;

	private static native String[] getKeys(JavaScriptObject obj) /*-{
		return Object.keys(obj);
	}-*/;

	private static native JavaScriptObject getProperty(JavaScriptObject obj, String key) /*-{
		return obj[key] || null;
	}-*/;
}
