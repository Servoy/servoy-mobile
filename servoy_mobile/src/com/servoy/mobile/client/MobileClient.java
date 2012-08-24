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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.OfflineDataProxy;
import com.servoy.mobile.client.dto.ValueListDescription;
import com.servoy.mobile.client.solutionmodel.JSSolutionModel;
import com.servoy.mobile.client.util.Failure;
import com.sksamuel.jqm4gwt.Mobile;

/**
 * The main mobile client entry point
 * @author jblok
 */
public abstract class MobileClient implements EntryPoint 
{
	protected I18NMessages messages = (I18NMessages) GWT.create(I18NMessages.class);

	private Scope globalScope;
	private FoundSetManager foundSetManager;
	private OfflineDataProxy offlineDataProxy;
	private FormManager formManager;
	private JSSolutionModel solutionModel;
	
	@Override
	public void onModuleLoad() 
	{
		globalScope = new Scope();
		foundSetManager = new FoundSetManager(this);
		offlineDataProxy = new OfflineDataProxy(foundSetManager,getServerURL());
		formManager = createFormManager();
		solutionModel = createJSSolutionModel(null);
		
		if (!foundSetManager.hasContent() && isOnline())
		{
			sync();
		}
		else
		{
			showFirstForm();
		}
	}
	
	protected abstract FormManager createFormManager();
	
	protected abstract String getServerURL();

	protected abstract String getSolutionName();
	
	protected JSSolutionModel createJSSolutionModel(String formsJSON)
	{
		if(formsJSON != null)
		{
			JSONArray forms = JSONParser.parseStrict(formsJSON).isArray();
			if(forms != null) return (JSSolutionModel)forms.getJavaScriptObject().cast(); 
		}

		return null; 
	}

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
			    	log("Done, submitted size: "+result);

					load();
				}
				
				@Override
				public void onFailure(Failure reason) 
				{
					Mobile.hideLoadingDialog();
					
					if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
					{
						showLoginForm();
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
		    	log("Done, loaded size: "+result);
				showFirstForm();
			}
			
			@Override
			public void onFailure(Failure reason) 
			{
				Mobile.hideLoadingDialog();
				if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
				{
					showLoginForm();
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

	public void showLoginForm()
	{
		formManager.showLoginForm();
	}

	public void setLoginCredentials(String identifier,String password) 
	{
		offlineDataProxy.setLoginCredentials(identifier,password);
	}

	public Scope getGlobalScope()
	{
		return globalScope;
	}
	
	//check to see if currently connected to IP network
	private final native boolean isOnline()/*-{ 
		try
		{
			return $wnd.navigator.onLine;
		}
		catch(err)
		{
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
}
