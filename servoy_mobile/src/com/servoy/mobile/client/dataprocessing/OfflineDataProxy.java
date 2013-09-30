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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.storage.client.Storage;
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
	private String serverURL;
	private final boolean nodebug;
	private final int timeout;

	private Callback<Integer, Failure> loadCallback;
	private int totalLength;
	private String[] credentials; //id, password
	private String[] uncheckedCredentials; //id, password
	private Boolean hasSingleWsUpdateMethod = null;
	private final Storage sessionStorage = Storage.getSessionStorageIfSupported();

	public static String WS_NODEBUG_HEADER = "servoy.nodebug";
	public static String WS_USER_PROPERTIES_HEADER = "servoy.userproperties";

	public OfflineDataProxy(FoundSetManager fsm, String serverURL, boolean nodebug, int timeout)
	{
		this.nodebug = nodebug;
		foundSetManager = fsm;
		this.serverURL = serverURL;
		this.timeout = timeout;
		credentials = fsm.getCredentials();
	}


	/**
	 * @param name
	 * @param foundset
	 * @param cb
	 */
	@SuppressWarnings("nls")
	public void loadOfflineData(final FoundSet foundset, Callback<Integer, Failure> cb)
	{
		this.loadCallback = cb;

		//requires a REST url like: serverURL/offline_data/version/name
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, serverURL + "/offline_data/" + version + "/search");
		setRequestParameters(builder);

		JSONObject payload = new JSONObject();

		if (foundset instanceof RelatedFoundSet)
		{
			RelatedFoundSet relFoundset = (RelatedFoundSet)foundset;
			payload.put("relationname", new JSONString(relFoundset.getRelationName()));
			Record parentRecord = relFoundset.getParents()[0];
			payload.put("datasource", new JSONString(parentRecord.getDataSource()));
			String remotepk = foundSetManager.getRemotePK(parentRecord.getFoundset().getEntityName(), parentRecord.getPK().toString(), parentRecord.getRow());
			payload.put("pk", new JSONString(remotepk));
		}
		else
		{
			payload.put("datasource", new JSONString(foundSetManager.getEntityDescription(foundset.getEntityName()).getDataSource()));
			JSONArray findstates = new JSONArray();
			payload.put("findstates", findstates);
			for (int i = 0; i < foundset.getSize(); i++)
			{
				FindState fs = (FindState)foundset.getRecord(i);
				Map<String, Object> columnData = fs.getAllData();
				JSONObject json = new JSONObject();
				serializeFindState(columnData, json);
				findstates.set(i, json);
			}
		}

		builder.setHeader("Accept", "application/json");
		builder.setHeader("Content-Type", "application/json");
		try
		{
			builder.sendRequest(payload.toString(), new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
						"cannotLoadJSON"), exception));
					loadCallback = null;
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						storeUserProperties(response);
						successfullRestAuthResponseReceived();

						String content = response.getText();
						totalLength = 0;
						totalLength += content.length();
						OfflineDataDescription offlineData = getOfflineData(content);
						content = null;

						if (offlineData != null)
						{
							foundSetManager.mergeOfflineData(OfflineDataProxy.this, offlineData);
						}
					}
					else
					{
						loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotLoadJSON"), response.getStatusCode()));
						loadCallback = null;
					}
				}
			});
		}
		catch (RequestException e)
		{
			loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotLoadJSON"),
				e));
			loadCallback = null;
		}
	}

	private void serializeFindState(Map<String, Object> columnData, JSONObject json)
	{
		for (Entry<String, Object> entry : columnData.entrySet())
		{
			Object value = entry.getValue();
			if (value instanceof String)
			{
				json.put(entry.getKey(), new JSONString((String)value));
			}
			else if (value instanceof Number)
			{
				json.put(entry.getKey(), new JSONNumber(((Number)value).doubleValue()));
			}
			else if (value instanceof Date)
			{
				json.put(entry.getKey(), new JSONNumber(((Date)value).getTime()));
			}
			else if (value instanceof JsDate)
			{
				json.put(entry.getKey(), new JSONNumber(((JsDate)value).getTime()));
			}
			else if (value instanceof List)
			{
				if (((List)value).size() > 0)
				{
					JSONArray relatedStates = new JSONArray();
					json.put(entry.getKey(), relatedStates);
					for (int j = 0; j < ((List)value).size(); j++)
					{
						Object relatedValues = ((List)value).get(j);
						if (relatedValues instanceof Map)
						{
							JSONObject relatedJSon = new JSONObject();
							relatedStates.set(j, relatedJSon);
							serializeFindState((Map<String, Object>)relatedValues, relatedJSon);
						}
					}
				}
			}
			else
			{
				Log.error("value unknown", value.toString());
			}
		}
	}

	@SuppressWarnings("nls")
	public void loadOfflineData(final String name, Callback<Integer, Failure> cb)
	{
		this.loadCallback = cb;

		//requires a REST url like: serverURL/offline_data/version/name
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, serverURL + "/offline_data/" + version + '/' + URL.encode(name));
		setRequestParameters(builder);

		builder.setHeader("Accept", "application/json");
		try
		{
			builder.sendRequest(null, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
						"cannotLoadJSON"), exception));
					loadCallback = null;
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						storeUserProperties(response);
						successfullRestAuthResponseReceived();

						String content = response.getText();
						totalLength = 0;
						totalLength += content.length();
						OfflineDataDescription offlineData = getOfflineData(content);
						content = null;

						if (offlineData != null) foundSetManager.storeOfflineData(OfflineDataProxy.this, name, offlineData);
					}
					else
					{
						loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotLoadJSON"), response.getStatusCode()));
						loadCallback = null;
					}
				}
			});
		}
		catch (RequestException e)
		{
			loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotLoadJSON"),
				e));
			loadCallback = null;
		}
	}

	private final OfflineDataDescription getOfflineData(String json)
	{
		JSONObject jsono = JSONParser.parseStrict(json).isObject();
		if (jsono == null) return null;
		return jsono.getJavaScriptObject().cast();
	}

	@SuppressWarnings("nls")
	public void requestRowData(final HashMap<String, HashSet<Object>> entitiesToPKs, final boolean updateMode)
	{
		final String entityName = getNextItem(entitiesToPKs.keySet());
		if (entityName == null)
		{
			//when empty stop
			loadCallback.onSuccess(Integer.valueOf(totalLength));
			loadCallback = null;
			return;
		}

		HashSet<Object> coll = entitiesToPKs.get(entityName);
		if (coll.size() == 0) //deal/skip with empty coll
		{
			entitiesToPKs.remove(entityName);//remove current when empty
			requestRowData(entitiesToPKs, updateMode);//process the next
			return;
		}
		Iterator<Object> pks = coll.iterator();
		String params = "?ids=" + concatAsString(pks);

		//requires REST urls like:
		//serverURL/entityName/list?ids=1,2,3,4,12
		//serverURL/entityName/filter?name=bla
		//serverURL/entityName/12 GET
		//serverURL/entityName/12 POST (for new)
		//serverURL/entityName PUT (for update)
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, serverURL + '/' + foundSetManager.getEntityPrefix() + entityName + '/' + version +
			"/list" + URL.encode(params));
		setRequestParameters(builder);

		builder.setHeader("Accept", "application/json");
		try
		{
			builder.sendRequest(null, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
						"cannotLoadJSON"), exception));
					loadCallback = null;
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						storeUserProperties(response);
						successfullRestAuthResponseReceived();

						String content = response.getText();
						totalLength += content.length();
						JsArray<RowDescription> rowData = getRows(content);
						content = null;

						if (rowData != null) foundSetManager.storeRowData(entityName, rowData, updateMode);

						if (entitiesToPKs.get(entityName).size() == 0)
						{
							entitiesToPKs.remove(entityName);//remove current when empty
						}

						requestRowData(entitiesToPKs, updateMode);//process the next
					}
					else
					{
						loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotLoadJSON"), response.getStatusCode()));
						loadCallback = null;
					}
				}
			});
		}
		catch (RequestException e)
		{
			loadCallback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotLoadJSON"),
				e));
			loadCallback = null;
		}
	}

	protected void successfullRestAuthResponseReceived()
	{
		if (credentials == null && uncheckedCredentials != null)
		{
			credentials = uncheckedCredentials; // login confirmed
			uncheckedCredentials = null;

			foundSetManager.storeCredentials(credentials[0], credentials[1]);

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

	@SuppressWarnings("nls")
	public void saveOfflineData(final Callback<Integer, Failure> callback)
	{
		if (hasSingleWsUpdateMethod == null)
		{
			testOffLineDataWSUpdate(callback);
		}
		else if (hasSingleWsUpdateMethod.booleanValue())
		{
			JSONObject payload = new JSONObject();
			payload.put("entityPrefix", new JSONString(foundSetManager.getEntityPrefix()));

			final ArrayList<String> deletes = foundSetManager.getDeletes();
			if (deletes.size() > 0)
			{
				JSONArray deletedKeys = new JSONArray();
				payload.put("deletes", deletedKeys);
				for (String key : deletes)
				{
					int idx = key.indexOf('|');
					final String entityName = key.substring(0, idx);
					String pk = key.substring(idx + 1, key.length());
					String remotepk = foundSetManager.getRemotePK(entityName, pk, null);

					JSONObject value = new JSONObject();
					value.put("pk", new JSONString(remotepk));
					value.put("entity", new JSONString(entityName));
					deletedKeys.set(deletedKeys.size(), value);
				}
			}

			final ArrayList<String> changes = foundSetManager.getChanges();
			final ArrayList<String[]> createdOnDevice = new ArrayList<String[]>();
			if (changes.size() > 0)
			{
				JSONArray changeRows = new JSONArray();
				payload.put("changes", changeRows);
				for (String key : changes)
				{
					int idx = key.indexOf('|');
					final String entityName = key.substring(0, idx);
					final String pk = key.substring(idx + 1, key.length());
					final RowDescription row = foundSetManager.getLocalStorageRowDescription(entityName, pk);
					JSONObject json = foundSetManager.toRemoteJSON(entityName, row);
					if (json == null) continue;
					String remotepk = foundSetManager.getRemotePK(entityName, pk, row);

					JSONObject value = new JSONObject();
					value.put("pk", new JSONString(remotepk));
					value.put("entity", new JSONString(entityName));
					value.put("row", json);

					if (row.isCreatedOnDevice())
					{
						createdOnDevice.add(new String[] { entityName, pk });
						value.put("method", new JSONString("i"));
					}
					else
					{
						value.put("method", new JSONString("u"));
					}
					changeRows.set(changeRows.size(), value);
				}
			}

			RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, serverURL + "/offline_data/" + version);
			setRequestParameters(builder);
			builder.setHeader("Content-Type", "application/json");
			String json = payload.toString();
			totalLength = json.length();
			try
			{
				builder.sendRequest(json, new RequestCallback()
				{
					public void onError(Request request, Throwable exception)
					{
						callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotSaveJSON"), exception));
					}

					public void onResponseReceived(Request request, Response response)
					{
						if (Response.SC_OK == response.getStatusCode())
						{
							storeUserProperties(response);
							successfullRestAuthResponseReceived();

							deletes.clear();
							changes.clear();

							foundSetManager.updateDeletesInLocalStorage(); //update deletes
							foundSetManager.updateChangesInLocalStorage(); //update changes

							for (String[] entityAndPk : createdOnDevice)
							{
								foundSetManager.recordPushedToServer(entityAndPk[0], entityAndPk[1]); //is present on server, reset flag
							}
							callback.onSuccess(Integer.valueOf(totalLength));
						}
						else
						{
							callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
								"cannotSaveJSON"), response.getStatusCode()));
						}
					}
				});
			}
			catch (RequestException e)
			{
				callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotSaveJSON"),
					e));
			}
		}
		else
		{
			totalLength = 0;
			postRowData(foundSetManager.getChanges(), callback);
		}
	}

	@SuppressWarnings("nls")
	private void deleteRowData(final ArrayList<String> keys, final Callback<Integer, Failure> callback)
	{
		final String key = getNextItem(keys);
		if (key == null)
		{
			//when empty stop
			callback.onSuccess(Integer.valueOf(totalLength));
			return;
		}

		int idx = key.indexOf('|');
		final String entityName = key.substring(0, idx);
		String pk = key.substring(idx + 1, key.length());
		String remotepk = foundSetManager.getRemotePK(entityName, pk, null);

		//DELETE server side
		RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, serverURL + '/' + foundSetManager.getEntityPrefix() + entityName + '/' + version +
			'/' + URL.encode(remotepk));
		setRequestParameters(builder);
		//builder.setHeader("Access-Control-Request-Method", "DELETE");

		try
		{
			builder.sendRequest("", new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
						"cannotDeleteRecord"), exception));
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						storeUserProperties(response);
						successfullRestAuthResponseReceived();

						keys.remove(key);//remove current
						foundSetManager.updateDeletesInLocalStorage(); //update deletes
						deleteRowData(keys, callback);
					}
					else
					{
						callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotDeleteRecord"), response.getStatusCode()));
					}
				}
			});
		}
		catch (RequestException e)
		{
			callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotDeleteRecord"),
				e));
		}
	}

	@SuppressWarnings("nls")
	private void postRowData(final ArrayList<String> keys, final Callback<Integer, Failure> callback)
	{
		final String key = getNextItem(keys);
		if (key == null)
		{

			if (foundSetManager.getDeletes().size() == 0)
			{
				//when no updates stop
				callback.onSuccess(Integer.valueOf(totalLength));
			}
			else
			{
				deleteRowData(foundSetManager.getDeletes(), callback);
			}
			return;
		}

		int idx = key.indexOf('|');
		final String entityName = key.substring(0, idx);
		final String pk = key.substring(idx + 1, key.length());
		final RowDescription row = foundSetManager.getLocalStorageRowDescription(entityName, pk);
		JSONObject jsonObject = foundSetManager.toRemoteJSON(entityName, row);
		if (jsonObject == null)
		{
			keys.remove(key);//remove current
			foundSetManager.updateChangesInLocalStorage(); //update changes
			postRowData(keys, callback);//process the next
			return;
		}
		String remotepk = foundSetManager.getRemotePK(entityName, pk, row);

		String json = jsonObject.toString();
		totalLength += json.length();

		//serverURL/entityName/12 PUT (for update), POST for new
		RequestBuilder builder = new RequestBuilder(row.isCreatedOnDevice() ? RequestBuilder.POST : RequestBuilder.PUT, serverURL + '/' +
			foundSetManager.getEntityPrefix() + entityName + '/' + version + '/' + URL.encode(remotepk));
		setRequestParameters(builder);
		//builder.setHeader("Access-Control-Request-Method", row.isCreatedOnDevice() ? "POST" : "PUT");

		builder.setHeader("Content-Type", "application/json");
		try
		{
			builder.sendRequest(json, new RequestCallback()
			{
				public void onError(Request request, Throwable exception)
				{
					callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
						"cannotSaveJSON"), exception));
				}

				public void onResponseReceived(Request request, Response response)
				{
					if (Response.SC_OK == response.getStatusCode())
					{
						storeUserProperties(response);
						successfullRestAuthResponseReceived();

						keys.remove(key);//remove current
						foundSetManager.updateChangesInLocalStorage(); //update changes
						if (row.isCreatedOnDevice())
						{
							foundSetManager.recordPushedToServer(entityName, pk); //is present on server, reset flag
						}
						postRowData(keys, callback);//process the next
					}
					else
					{
						callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback(
							"cannotSaveJSON"), response.getStatusCode()));
					}
				}
			});
		}
		catch (RequestException e)
		{
			callback.onFailure(new Failure(foundSetManager.getApplication(), foundSetManager.getApplication().getI18nMessageWithFallback("cannotSaveJSON"), e));
		}
	}

	public void setUncheckedLoginCredentials(String identifier, String password)
	{
		credentials = null;
		foundSetManager.clearCredentials();
		if (identifier != null && password != null)
		{
			uncheckedCredentials = new String[] { identifier, password };
		}
		else
		{
			uncheckedCredentials = null;
		}
	}

	public boolean hasCredentials()
	{
		return credentials != null;
	}

	public boolean hasUncheckedCredentials()
	{
		return uncheckedCredentials != null;
	}

	@SuppressWarnings("nls")
	private void testOffLineDataWSUpdate(final Callback<Integer, Failure> callback)
	{
		//requires a REST url like: serverURL/offline_data/version/name
		RequestBuilder builder = new ServoyRequestBuilder("OPTIONS", serverURL + "/offline_data/" + version + "/ws_update");
		setRequestParameters(builder);
		try
		{
			builder.sendRequest("", new RequestCallback()
			{

				@Override
				public void onResponseReceived(Request request, Response response)
				{
					try
					{
						storeUserProperties(response);
						String header = response.getHeader("Allow");
						Log.info("options response for offlinedate.ws_update: got header response: " + header);
						hasSingleWsUpdateMethod = (header != null && header.contains("PUT")) ? Boolean.TRUE : Boolean.FALSE;
					}
					catch (Exception e)
					{
						Log.error("options response for offlinedate.ws_update", e);
						hasSingleWsUpdateMethod = Boolean.FALSE;
					}
					saveOfflineData(callback);
				}

				@Override
				public void onError(Request request, Throwable exception)
				{
					Log.error("on error: " + exception.getMessage());
					callback.onFailure(new Failure(foundSetManager.getApplication(), "Couldn't test for a offline_data.ws_update method", exception));
				}
			});
		}
		catch (RequestException e)
		{
			e.printStackTrace();
		}

	}

	public void setServerURL(String url)
	{
		this.serverURL = url;
	}

	/**
	 * Stores the user properties comming form the request in session storage , for later setting
	 */
	private void storeUserProperties(Response response)
	{
		String jsonString = response.getHeader(WS_USER_PROPERTIES_HEADER);
		if (jsonString != null)
		{
			sessionStorage.setItem(WS_USER_PROPERTIES_HEADER, jsonString);
		}
	}

	@SuppressWarnings("nls")
	private void setRequestParameters(RequestBuilder builder)
	{
		if (nodebug) builder.setHeader(WS_NODEBUG_HEADER, "true");
		String jsonString = sessionStorage.getItem(WS_USER_PROPERTIES_HEADER);
		if (jsonString != null)
		{
			builder.setHeader(WS_USER_PROPERTIES_HEADER, jsonString);
		}
		String[] credentialsToUse = credentials != null ? credentials : uncheckedCredentials != null ? uncheckedCredentials : null;
		if (credentialsToUse != null)
		{
			try
			{
// TODO check should we also set these? so the system knows we did set them?
//				builder.setUser(credentialsToUse[0]);
//				builder.setPassword(credentialsToUse[1]);
// Maybe also set this one, and then don't set the header below, problem is that chrome has a bug: https://code.google.com/p/chromium/issues/detail?id=31582
//				builder.setIncludeCredentials(true);
				builder.setHeader("Authorization",
					"Basic " + new String(Base64Coder.encode((credentialsToUse[0] + ":" + credentialsToUse[1]).getBytes("UTF-8"))));
			}
			catch (UnsupportedEncodingException e)
			{
				Log.error("Cannot convert to UTF8 encoding:", e);
			}
		}
		builder.setTimeoutMillis(timeout * 1000);
	}


	class ServoyRequestBuilder extends RequestBuilder
	{
		public ServoyRequestBuilder(String method, String url)
		{
			super(method, url);
		}
	}
}
