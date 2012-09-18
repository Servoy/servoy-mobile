package com.servoy.mobile.client;

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

import java.util.HashMap;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.OfflineDataProxy;
import com.servoy.mobile.client.dto.ValueListDescription;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.scripting.JSApplication;
import com.servoy.mobile.client.scripting.JSDatabaseManager;
import com.servoy.mobile.client.scripting.PluginsScope;
import com.servoy.mobile.client.scripting.Scope;
import com.servoy.mobile.client.solutionmodel.JSSolutionModel;
import com.servoy.mobile.client.util.Failure;
import com.sksamuel.jqm4gwt.Mobile;

/**
 * The main mobile client entry point
 * @author jblok
 */
public class MobileClient implements EntryPoint
{
	protected I18NMessages messages = (I18NMessages)GWT.create(I18NMessages.class);

	private final HashMap<String, GlobalScope> scopes = new HashMap<String, GlobalScope>();
	private FoundSetManager foundSetManager;
	private OfflineDataProxy offlineDataProxy;
	private FormManager formManager;
	private JSSolutionModel solutionModel;

	@Override
	public void onModuleLoad()
	{
		solutionModel = createJSSolutionModel();
		foundSetManager = new FoundSetManager(this);
		offlineDataProxy = new OfflineDataProxy(foundSetManager, getServerURL());
		formManager = new FormManager(this);
		new JSApplication();
		new PluginsScope(this);
		new JSDatabaseManager(foundSetManager);
		export();

		if (!foundSetManager.hasContent() && isOnline())
		{
			sync();
		}
		else
		{
			showFirstForm();
		}
	}

	protected String getServerURL()
	{
		String serverURL = solutionModel.getServerUrl();
		if (serverURL == null)
		{
			serverURL = "http://127.0.0.1:8080";
		}
		if (serverURL.endsWith("/"))
		{
			serverURL = serverURL.substring(0, serverURL.length() - 1);
		}
		return serverURL + "/servoy-service/rest_ws/" + getSolutionName() + "_service";
//		String hostPageBaseURL = GWT.getHostPageBaseURL();
//		return hostPageBaseURL.substring(0, hostPageBaseURL.length() - 1);
	}

	protected String getSolutionName()
	{
		String solName = solutionModel.getSolutionName();
		if (solName == null)
		{
			solName = "MobileClient";
		}
		return solName;
	}

	protected native JSSolutionModel createJSSolutionModel() /*-{
		// Get a reference to the first customer in the JSON array from earlier
		return $wnd._solutiondata_;
	}-*/;

	public void sync()
	{
		if (!isOnline())
		{
			Window.alert(messages.noNetwork());
			return;
		}


		Mobile.showLoadingDialog(messages.syncing());

		if (foundSetManager.hasChanges())
		{
			//save and clear, when successful do load
			offlineDataProxy.saveOfflineData(getServerURL(), new Callback<Integer, Failure>()
			{
				@Override
				public void onSuccess(Integer result)
				{
					log("Done, submitted size: " + result);

					load();
				}

				@Override
				public void onFailure(Failure reason)
				{
					Mobile.hideLoadingDialog();

					if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
					{
						formManager.showLogin();
					}
					else
					{
						error(reason.getMessage());
						boolean ok = Window.confirm(messages.discardLocalChanges());
						if (ok)
						{
							load();
						}
						else
						{
							showFirstForm();
						}
					}
				}
			});
		}
		else
		{
			load();
		}
	}

	public void log(String msg)
	{
		GWT.log(msg);
	}

	public void error(String msg)
	{
		Window.alert(msg);
	}

	private void load()
	{
		//first clear existing stuff if there is any
		foundSetManager.clearLocalStorage();

		offlineDataProxy.loadOfflineData(getSolutionName(), new Callback<Integer, Failure>()
		{
			@Override
			public void onSuccess(Integer result)
			{
				Mobile.hideLoadingDialog();
				log("Done, loaded size: " + result);
				showFirstForm();
			}

			@Override
			public void onFailure(Failure reason)
			{
				Mobile.hideLoadingDialog();
				if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
				{
					formManager.showLogin();
				}
				else
				{
					error(reason.getMessage());
					showFirstForm();
				}
			}
		});
	}

	public FormManager getFormManager()
	{
		return formManager;
	}

	public FoundSetManager getFoundSetManager()
	{
		return foundSetManager;
	}

	public JSSolutionModel getSolutionModel()
	{
		return solutionModel;
	}

	public void showFirstForm()
	{
		formManager.showFirstForm();
	}

	void setLoginCredentials(String identifier, String password)
	{
		offlineDataProxy.setLoginCredentials(identifier, password);
	}

	//check to see if currently connected to IP network
	public final native boolean isOnline()/*-{
		try {
			return $wnd.navigator.onLine;
		} catch (err) {
			//browser does not support onLine yet
			return true;
		}
	}-*/;

	public I18NMessages getMessages()
	{
		return messages;
	}

	public ValueListDescription getValueListItems(String valueListName)
	{
		return foundSetManager.getValueListItems(valueListName);
	}

	public Scope getGlobalScope()
	{
		return getGlobalScope("globals");
	}

	public GlobalScope getGlobalScope(String name)
	{
		GlobalScope scope = scopes.get(name);
		if (scope == null)
		{
			scope = new GlobalScope(name, this);
			scopes.put(name, scope);
		}
		return scope;
	}

	private native void export()/*-{
		$wnd._ServoyUtils_.application = this;
		$wnd._ServoyUtils_.getGlobalScope = function(name) {
			return $wnd._ServoyUtils_.application.@com.servoy.mobile.client.MobileClient::getGlobalScope(Ljava/lang/String;)(name);
		}
		$wnd._ServoyUtils_.setScopeVariableType = function(scope, name, type) {
			return scope.@com.servoy.mobile.client.scripting.Scope::setVariableType(Ljava/lang/String;I)(name,type);
		}
		$wnd._ServoyUtils_.getScopeVariable = function(scope, name) {
			var type = scope.@com.servoy.mobile.client.scripting.Scope::getVariableType(Ljava/lang/String;)(name);
			if (type == 8 || type == 4) {
				var value = scope.@com.servoy.mobile.client.scripting.Scope::getVariableNumberValue(Ljava/lang/String;)(name);
				return isNaN(value) ? null : value;
			} else if (type == 93) {
				return scope.@com.servoy.mobile.client.scripting.Scope::getVariableDateValue(Ljava/lang/String;)(name);
			}
			return scope.@com.servoy.mobile.client.scripting.Scope::getVariableValue(Ljava/lang/String;)(name);
		}
		$wnd._ServoyUtils_.setScopeVariable = function(scope, name, value) {
			var type = scope.@com.servoy.mobile.client.scripting.Scope::getVariableType(Ljava/lang/String;)(name);
			if (typeof value == "number" || type == 8 || type == 4) {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableNumberValue(Ljava/lang/String;D)(name,value);
			} else if (type == 93) {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableDateValue(Ljava/lang/String;Lcom/google/gwt/core/client/JsDate;)(name,value);
			} else {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableValue(Ljava/lang/String;Ljava/lang/Object;)(name,value);
			}
		}
		$wnd._ServoyInit_.init();
	}-*/;
}
