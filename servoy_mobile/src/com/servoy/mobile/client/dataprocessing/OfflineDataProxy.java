package com.servoy.mobile.client.dataprocessing;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.servoy.mobile.client.dto.OfflineDataDescription;
import com.servoy.mobile.client.dto.RowDescription;
import com.servoy.mobile.client.request.Base64Coder;
import com.servoy.mobile.client.util.Failure;

/**
 * The proxy class to communicate with server
 * @author jblok
 */
public class OfflineDataProxy
{
	private static final int version = 1;
	private final FoundSetManager foundSetManager;
	private final String serverURL;

	private Callback<Integer, Failure> loadCallback;
	private int totalLength;
	private String[] credentials; //id, password

	public OfflineDataProxy(FoundSetManager fsm, String serverURL)
	{
		foundSetManager = fsm;
		this.serverURL = serverURL;
	}

	public void loadOfflineData(final String name, Callback<Integer, Failure> cb)
	{
		this.loadCallback = cb;

		//requires a REST url like: serverURL/offline_data/version/name
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, serverURL + "/offline_data/" + version + "/" + URL.encode(name));
		setRequestCredentials(builder);

		builder.setHeader("Accept", "application/json");
		try
		{
			builder.sendRequest(null, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), exception));
					loadCallback = null;
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						String content = response.getText();
						totalLength = 0;
						totalLength += content.length();
						OfflineDataDescription offlineData = getOfflineData(content);
						content = null;

						if (offlineData != null) foundSetManager.storeOfflineData(OfflineDataProxy.this, name, offlineData);
					}
					else
					{
						loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), response.getStatusCode()));
						loadCallback = null;
					}
				}
			});
		}
		catch (RequestException e)
		{
			loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), e));
			loadCallback = null;
		}
	}

	private final OfflineDataDescription getOfflineData(String json)
	{
		JSONObject jsono = JSONParser.parseStrict(json).isObject();
		if (jsono == null) return null;
		return jsono.getJavaScriptObject().cast();
	}

	public void requestRowData(final HashMap<String, HashSet<Object>> entitiesToPKs)
	{
		final String entityName = getNextItem(entitiesToPKs.keySet());
		if (entityName == null)
		{
			//when empty stop
			loadCallback.onSuccess(totalLength);
			loadCallback = null;
			return;
		}

		HashSet<Object> coll = entitiesToPKs.get(entityName);
		if (coll.size() == 0) //deal/skip with empty coll
		{
			entitiesToPKs.remove(entityName);//remove current when empty
			requestRowData(entitiesToPKs);//process the next
			return;
		}
		Iterator<Object> pks = coll.iterator();
		String params = "?ids=" + concatAsString(pks);

		//requires REST urls like:
		//serverURL/entityName/list?ids=1,2,3,4,12
		//serverURL/entityName/filter?name=bla
		//serverURL/entityName/12 GET
		//serverURL/entityName/12 POST (for update)
		//serverURL/entityName PUT (for new)
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, serverURL + "/" + foundSetManager.getEntityPrefix() + entityName + "/" + version +
			"/list" + URL.encode(params));
		setRequestCredentials(builder);

		builder.setHeader("Accept", "application/json");
		try
		{
			builder.sendRequest(null, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), exception));
					loadCallback = null;
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						String content = response.getText();
						totalLength += content.length();
						JsArray<RowDescription> rowData = getRows(content);
						content = null;

						if (rowData != null) foundSetManager.storeRowData(entityName, rowData);

						if (entitiesToPKs.get(entityName).size() == 0)
						{
							entitiesToPKs.remove(entityName);//remove current when empty
						}

						requestRowData(entitiesToPKs);//process the next
					}
					else
					{
						loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), response.getStatusCode()));
						loadCallback = null;
					}
				}
			});
		}
		catch (RequestException e)
		{
			loadCallback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotLoadJSON(), e));
			loadCallback = null;
		}
	}

	private String concatAsString(Iterator<Object> pks)
	{
		StringBuilder sb = new StringBuilder();
		while (pks.hasNext())
		{
			Object pk = pks.next();
			pks.remove();
			sb.append(pk);
			if (sb.length() > 1700) break;//prevent going over 2000 URL length 
			if (pks.hasNext()) sb.append(',');
		}
		return sb.toString();
	}

	private String getNextItem(Collection<String> coll)
	{
		Iterator<String> it = coll.iterator();
		if (it.hasNext())
		{
			return it.next();
		}
		return null;
	}

	private final JsArray<RowDescription> getRows(String json)
	{
		JSONArray ja = JSONParser.parseStrict(json).isArray();
		if (ja == null) return null;
		return ja.getJavaScriptObject().cast();
	}

	public void saveOfflineData(final String serverUrl, final Callback<Integer, Failure> callback)
	{
		totalLength = 0;
		deleteRowData(serverUrl, foundSetManager.getDeletes(), callback);
		postRowData(serverUrl, foundSetManager.getChanges(), callback);
	}

	private void deleteRowData(final String serverUrl, final ArrayList<String> keys, final Callback<Integer, Failure> callback)
	{
		final String key = getNextItem(keys);
		if (key == null)
		{
			if (foundSetManager.getChanges().size() == 0)
			{
				//when no updates stop
				callback.onSuccess(totalLength);
			}
			return;
		}

		int idx = key.indexOf('|');
		final String entityName = key.substring(0, idx);
		String pk = key.substring(idx + 1, key.length());

		//DELETE server side
		RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, serverURL + "/" + foundSetManager.getEntityPrefix() + entityName + "/" + version +
			"/" + URL.encode(pk));
		setRequestCredentials(builder);
		builder.setHeader("Access-Control-Request-Method", "DELETE");

		try
		{
			builder.sendRequest("", new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotDeleteRecord(), exception));
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						keys.remove(key);//remove current
						foundSetManager.updateDeletesInLocalStorage(); //update deletes
						deleteRowData(serverUrl, keys, callback);
					}
					else
					{
						callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotDeleteRecord(), response.getStatusCode()));
					}
				}
			});
		}
		catch (RequestException e)
		{
			callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotDeleteRecord(), e));
		}
	}

	private void postRowData(final String serverUrl, final ArrayList<String> keys, final Callback<Integer, Failure> callback)
	{
		final String key = getNextItem(keys);
		if (key == null)
		{
			//when empty stop
			callback.onSuccess(totalLength);
			return;
		}

		int idx = key.indexOf('|');
		final String entityName = key.substring(0, idx);
		final String pk = key.substring(idx + 1, key.length());
		final RowDescription row = foundSetManager.getRowDescription(entityName, pk);
		String remotepk = foundSetManager.getRemotePK(entityName, pk, row);

		String json = foundSetManager.toRemoteJSON(entityName, row);
		totalLength += json.length();

		//serverURL/entityName/12 PUT (for update), POST for new
		RequestBuilder builder = new RequestBuilder(row.isCreatedOnDevice() ? RequestBuilder.POST : RequestBuilder.PUT, serverURL + "/" +
			foundSetManager.getEntityPrefix() + entityName + "/" + version + "/" + URL.encode(remotepk));
		setRequestCredentials(builder);
		builder.setHeader("Access-Control-Request-Method", row.isCreatedOnDevice() ? "POST" : "PUT");

		builder.setHeader("Content-Type", "application/json");
		try
		{
			builder.sendRequest(json, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotSaveJSON(), exception));
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						keys.remove(key);//remove current
						foundSetManager.updateChangesInLocalStorage(); //update changes
						if (row.isCreatedOnDevice())
						{
							foundSetManager.recordPushedToServer(entityName, pk); //is present on server, reset flag
						}
						postRowData(serverUrl, keys, callback);//process the next
					}
					else
					{
						callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotSaveJSON(), response.getStatusCode()));
					}
				}
			});
		}
		catch (RequestException e)
		{
			callback.onFailure(new Failure(foundSetManager.getApplication().getMessages().cannotSaveJSON(), e));
		}
	}

	public void setLoginCredentials(String identifier, String password)
	{
		if (identifier != null && password != null)
		{
			credentials = new String[] { identifier, password };
		}
		else
		{
			credentials = null;
		}
	}

	public boolean hasCredentials()
	{
		return credentials != null;
	}

	private void setRequestCredentials(RequestBuilder builder)
	{
		if (credentials != null)
		{
			builder.setUser(credentials[0]);
			builder.setPassword(credentials[1]);
			builder.setHeader("Authorization", "Basic " + Base64Coder.encodeString(credentials[0] + ":" + credentials[1]));
		}
	}
}
