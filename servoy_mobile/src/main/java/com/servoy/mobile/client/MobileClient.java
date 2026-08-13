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
import java.util.Map;

import com.allen_sauer.gwt.log.client.DivLogger;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.servoy.base.solutionmodel.mobile.IMobileSolutionModel;
import com.servoy.base.test.IJSUnitSuiteHandler;
import com.servoy.mobile.client.angular.AngularBridge;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.OfflineDataProxy;
import com.servoy.mobile.client.dataprocessing.OfflineDataProxy.SaveOfflineDataParam;
import com.servoy.mobile.client.persistence.FlattenedSolution;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.properties.CssPositionConvertor;
import com.servoy.mobile.client.properties.DataProviderConvertor;
import com.servoy.mobile.client.properties.FormConvertor;
import com.servoy.mobile.client.properties.FormatConvertor;
import com.servoy.mobile.client.properties.IPropertyConverter;
import com.servoy.mobile.client.properties.RuntimeComponentConvertor;
import com.servoy.mobile.client.properties.ValueListConfigConverter;
import com.servoy.mobile.client.properties.ValuelistConvertor;
import com.servoy.mobile.client.properties.VarArgsConvertor;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.ui.ApiSpec;
import com.servoy.mobile.client.ui.Parameter;
import com.servoy.mobile.client.scripting.APPLICATION_TYPES;
import com.servoy.mobile.client.scripting.DEFAULTS;
import com.servoy.mobile.client.scripting.JSApplication;
import com.servoy.mobile.client.scripting.JSDatabaseManager;
import com.servoy.mobile.client.scripting.JSEvent;
import com.servoy.mobile.client.scripting.JSHistory;
import com.servoy.mobile.client.scripting.JSI18N;
import com.servoy.mobile.client.scripting.JSSecurity;
import com.servoy.mobile.client.scripting.JSUtils;
import com.servoy.mobile.client.scripting.MobilePlugin;
import com.servoy.mobile.client.scripting.PluginsScope;
import com.servoy.mobile.client.scripting.ScriptEngine;
import com.servoy.mobile.client.scripting.solutionmodel.JSSolutionModel;
import com.servoy.mobile.client.ui.Executor;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;
import com.servoy.mobile.client.ui.WebRuntimeLayoutContainer;
import com.servoy.mobile.client.ui.WebRuntimeService;
import com.servoy.mobile.client.util.Failure;
import com.servoy.mobile.client.util.Utils;

/**
 * The main mobile client entry point
 * @author jblok
 */
public class MobileClient implements EntryPoint
{

	private FoundSetManager foundSetManager;
	private OfflineDataProxy offlineDataProxy;
	private FormManager formManager;
	private JSSolutionModel solutionModel;
	private ScriptEngine scriptEngine;
	private FlattenedSolution flattenedSolution;
	private SolutionI18nProvider i18nProvider;
	protected boolean firstFormFirstShow = true;

	private IAfterLoginHandler afterLoginHandler;
	private int version = 1;

	private final Map<String, IPropertyConverter> converters = new HashMap<>();

	private AngularBridge angularBridge;

	@Override
	public void onModuleLoad()
	{
		// add stuff when needed in initialise rather then here (so that they get executed in mobile test client as well)
		if (!"true".equals(Window.Location.getParameter(IJSUnitSuiteHandler.NO_INIT_SMC_ARG))) initialize();
	}

	private static native void loadMediaResources()/*-{
		$wnd._ServoyUtils_.loadMediaResources();
	}-*/;

	protected void initialize()
	{
		loadMediaResources();
		/*
		 * Install an UncaughtExceptionHandler which will produce <code>FATAL</code> log messages
		 */
		Log.setUncaughtExceptionHandler();

		if (Window.Location.getParameter("showdivlogger") != null) //$NON-NLS-1$
		{
			DivLogger divLogger = new DivLogger();
			divLogger.setCurrentLogLevel(Log.getLowestLogLevel());
			Log.addLogger(divLogger);
		}
		boolean nodebug = false;
		if (Window.Location.getParameter("nodebug") != null) //$NON-NLS-1$
		{
			nodebug = true;
		}
		exportLog();

		GWT.create(JSDatabaseManager.class);
		GWT.create(PluginsScope.class);
		GWT.create(JSApplication.class);
		GWT.create(FormController.class);
		GWT.create(JSHistory.class);
		GWT.create(JSUtils.class);
		GWT.create(JSSecurity.class);
		GWT.create(JSI18N.class);
		GWT.create(JSEvent.class);
		GWT.create(JSSolutionModel.class); // this will also export all referenced classes like JSForm
		GWT.create(FoundSet.class); // foundset is not a scope yet, if it becomes a scope (aggregates) then this can't be done, or must he exported differently
		GWT.create(DEFAULTS.class);
		GWT.create(APPLICATION_TYPES.class);

		GWT.create(WebRuntimeComponent.class);
		GWT.create(WebRuntimeLayoutContainer.class);
		GWT.create(WebRuntimeService.class);
		GWT.create(MobilePlugin.class);
		// non solution related (internal) API
		GWT.create(Utils.class);

		registerConverters();

		flattenedSolution = new FlattenedSolution(createSolution());
		i18nProvider = new SolutionI18nProvider(flattenedSolution, getBrowserLocale());
		foundSetManager = new FoundSetManager(this);
		offlineDataProxy = new OfflineDataProxy(foundSetManager, getServerURL(), nodebug, getTimeout());
		formManager = createFormManager();
		solutionModel = new JSSolutionModel(this);
		scriptEngine = new ScriptEngine(this);

		//		JQMContext.setDefaultTransition(Transition.FADE);

		angularBridge = new AngularBridge(this);

		//		onStartPageShown();
	}

	private void registerConverters()
	{
		converters.put("format", new FormatConvertor());
		converters.put("cssPosition", new CssPositionConvertor());
		converters.put("dataprovider", new DataProviderConvertor());
		converters.put("valuelist", new ValuelistConvertor(this));
		converters.put("valuelistConfig", new ValueListConfigConverter());
		converters.put("form", new FormConvertor());
		converters.put("runtimecomponent", new RuntimeComponentConvertor());
	}

	public IPropertyConverter getConverter(String typeName)
	{
		return converters.get(typeName);
	}

	public Object convertValueForType(String typeName, Object value, PropertySpec propertyType)
	{
		IPropertyConverter converter = converters.get(typeName);
		if (converter != null)
		{
			return converter.convertForClient(value, null, propertyType);
		}
		return value;
	}

	/**
	 * Mobile-client equivalent of the server-side argument conversion that happens before a
	 * componentApis / serviceApis message is sent to the Angular client.
	 *
	 * Must be called <em>after</em> {@link ApiSpec#processVarArgsIfNeeded} has already collapsed
	 * surplus varargs into a single JS array at the varargs position.  For each declared parameter
	 * position the method looks up an {@link IPropertyConverter} by the parameter's base type name
	 * (stripping any trailing {@code "..."} varargs marker).  A varargs position is additionally
	 * wrapped in the {@code {"vEr":1,"v":[...]}} envelope by {@link VarArgsConvertor}, mirroring
	 * what the server's {@code CustomVariableArgsType.toJSON()} emits.
	 *
	 * @param arguments the args array (already varargs-collapsed)
	 * @param api       the API spec whose {@code parameters} array supplies the declared types
	 * @return the converted args array (the same array, mutated in place)
	 */
	public Object[] convertApiArgsForClient(Object[] arguments, ApiSpec api)
	{
		if (arguments == null || arguments.length == 0 || api == null) return arguments;
		Array<Parameter> parameters = api.getParameters();
		if (Array.isEmpty(parameters)) return arguments;

		int paramCount = parameters.getLength();
		for (int i = 0; i < arguments.length; i++)
		{
			// After processVarArgsIfNeeded there are at most paramCount entries; the last
			// declared parameter covers the (only possible) varargs position.
			Parameter param = parameters.getAt(i < paramCount ? i : paramCount - 1);
			if (param == null) continue;
			String paramType = param.getType();
			if (paramType == null) continue;

			boolean isVarArgs = paramType.endsWith("..."); //$NON-NLS-1$
			String baseType = isVarArgs ? paramType.substring(0, paramType.length() - 3) : paramType;

			IPropertyConverter elementConverter = converters.get(baseType);
			if (isVarArgs)
			{
				// Always wrap with VarArgsConvertor regardless of whether there is an element
				// converter — the envelope is required even for primitive element types.
				arguments[i] = new VarArgsConvertor(elementConverter).convertForClient(arguments[i], null, null);
			}
			else if (elementConverter != null)
			{
				arguments[i] = elementConverter.convertForClient(arguments[i], null, null);
			}
		}
		return arguments;
	}


	protected FormManager createFormManager()
	{
		return new FormManager(this);
	}

	public void onStartPageShown()
	{
		if (hasFirstFormADataSource() && !foundSetManager.hasContent())
		{
			sync(false);
		}
		else
		{
			showFirstForm();
		}
	}

	private native void addStartPageShowCallback()
	/*-{
		var mobileClient = this;
		if ($wnd.$.mobile.activePage
				&& $wnd.$.mobile.activePage.attr("id") == 'start') {
			mobileClient.@com.servoy.mobile.client.MobileClient::onStartPageShown()();
		} else {
			$wnd
					.$(document)
					.on(
							"pageshow",
							"#start",
							function(event) {
								mobileClient.@com.servoy.mobile.client.MobileClient::onStartPageShown()();
							});
		}
	}-*/;

	protected String getServerURL()
	{
		String serverURL = flattenedSolution.getServerUrl();
		if (serverURL == null)
		{
			serverURL = "http://127.0.0.1:8080";
		}
		return appendServiceToServerURL(serverURL);
		//		String hostPageBaseURL = GWT.getHostPageBaseURL();
		//		return hostPageBaseURL.substring(0, hostPageBaseURL.length() - 1);
	}

	public void setApplicationServerURL(String url)
	{
		offlineDataProxy.setServerURL(appendServiceToServerURL(url));
	}


	private String appendServiceToServerURL(String serverURL)
	{
		if (serverURL != null)
		{
			if (serverURL.endsWith("/"))
			{
				serverURL = serverURL.substring(0, serverURL.length() - 1);
			}
			return serverURL + "/servoy-service/rest_ws/" + getServiceSolutionName();
		}
		return null;
	}

	public String getApplicationServerURL()
	{
		return removeServiceFromServerURL(offlineDataProxy.getServerURL());
	}

	private String removeServiceFromServerURL(String serverURL)
	{
		if (serverURL != null)
		{
			int index = serverURL.lastIndexOf("/servoy-service/rest_ws/");
			if (index > 0)
			{
				return serverURL.substring(0, index);
			}
			// else: no service appended?
		}
		return serverURL;
	}


	protected int getTimeout()
	{
		return flattenedSolution.getTimeout();
	}

	protected String getSolutionName()
	{
		String solName = flattenedSolution.getSolutionName();
		if (solName == null)
		{
			solName = "MobileClient";
		}
		return solName;
	}

	private String getServiceSolutionName()
	{
		String solName = flattenedSolution.getServiceSolutionName();
		if (solName == null)
		{
			solName = getSolutionName() + "_service";
		}
		return solName;
	}

	protected native Solution createSolution()
	/*-{
		return $wnd._solutiondata_;
	}-*/;

	public void sync()
	{
		sync(false);
	}

	public void sync(final JavaScriptObject successCallback, final JavaScriptObject errorHandler, boolean forceUseSingleWsUpdateMethod)
	{
		sync(successCallback, errorHandler, false, forceUseSingleWsUpdateMethod);
	}

	public void sync(boolean useUncheckedCredentials)
	{
		sync(null, null, useUncheckedCredentials, false);
	}

	public void sync(final JavaScriptObject successCallback, final JavaScriptObject errorHandler, boolean useUncheckedCredentials,
		boolean forceUseSingleWsUpdateMethod)
	{
		if (!testLocalStorage()) return;
		if (flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials() &&
			!(offlineDataProxy.hasUncheckedCredentials() && useUncheckedCredentials))
		{
			afterLoginHandler = new IAfterLoginHandler()
			{
				@Override
				public void execute()
				{
					sync(successCallback, errorHandler, true, forceUseSingleWsUpdateMethod);
				}
			};
			formManager.showLogin();
		}
		else
		{
			if (isSynchronizing()) return;
			//			Mobile.showLoadingDialog(getI18nMessageWithFallback("syncing"));
			flagSyncStart();
			if (foundSetManager.hasChanges())
			{
				//save and clear, when successful do load
				offlineDataProxy.saveOfflineData(new Callback<SaveOfflineDataParam, Failure>()
				{
					@Override
					public void onSuccess(SaveOfflineDataParam result)
					{
						log("Done, submitted size: " + result.length);
						load(successCallback, errorHandler);
					}

					@Override
					public void onFailure(Failure reason)
					{
						//						Mobile.hideLoadingDialog();
						try
						{
							if (errorHandler != null && reason.getStatusCode() != Response.SC_UNAUTHORIZED)
							{
								JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
								jsArray.set(0, reason.getStatusCode());
								jsArray.set(1, reason.getMessage());
								Executor.call(errorHandler, jsArray);
							}
							else
							{
								error(reason.getMessage());
								if (reason.getStatusCode() == -1)
								{
									showFirstForm();
								}
								else if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
								{
									// clear the current credentials and call sync again.
									clearCredentials();
									sync(successCallback, errorHandler, false, forceUseSingleWsUpdateMethod);
								}
								else
								{
									boolean ok = Window.confirm(getI18nMessageWithFallback("discardLocalChanges"));
									if (ok)
									{
										load(successCallback, errorHandler);
									}
									else
									{
										showFirstForm();
									}
								}
							}
						}
						finally
						{
							flagSyncStop();
						}
					}
				}, forceUseSingleWsUpdateMethod);
			}
			else
			{
				load(successCallback, errorHandler);
			}
		}
	}

	private boolean isSynchronizing;

	private boolean isSynchronizing()
	{
		return isSynchronizing;
	}

	private void flagSyncStart()
	{
		isSynchronizing = true;
	}

	private void flagSyncStop()
	{
		isSynchronizing = false;
	}

	public String getI18nMessageWithFallback(String key)
	{
		return i18nProvider.getI18nMessageWithFallback(key);
	}

	public static void log(String msg)
	{
		GWT.log(msg);
		jsConsoleLog(msg);
	}

	native static void jsConsoleLog(Object message) /*-{
    try {
        console.log(message);
    } catch (e) {
    }
}-*/;

	public void error(String msg)
	{
		Window.alert(msg);
	}


	private FormController currentFormWhenRemoteSearch = null;

	/**
	 * @param foundset
	 * @param successCallback
	 * @param errorHandler
	 */
	public void load(final FoundSet foundset, final JavaScriptObject successCallback, final JavaScriptObject errorHandler)
	{
		if (flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials() && !(offlineDataProxy.hasUncheckedCredentials()))
		{
			currentFormWhenRemoteSearch = formManager.getCurrentForm();
			afterLoginHandler = new IAfterLoginHandler()
			{
				@Override
				public void execute()
				{
					load(foundset, successCallback, errorHandler);
				}
			};
			formManager.showLogin();
		}
		else
		{

			offlineDataProxy.loadOfflineData(foundset, new Callback<Integer, Failure>()
			{
				@Override
				public void onSuccess(Integer result)
				{
					if (currentFormWhenRemoteSearch != null)
					{
						getFormManager().showForm(currentFormWhenRemoteSearch);
						currentFormWhenRemoteSearch = null;
					}
					log("Done, loaded size: " + result);
					if (successCallback != null)
					{
						try
						{
							foundset.search();
						}
						catch (Exception e)
						{
							// shouldn't happen.
							Log.error("error calling search on foundset after remoteSearch", e);
						}
						JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
						jsArray.set(0, foundset.getJavaScriptInstance());
						Executor.call(successCallback, jsArray);
					}
				}

				@Override
				public void onFailure(Failure reason)
				{
					StringBuilder detail = new StringBuilder();
					if (reason.getStatusCode() != 0)
					{
						detail.append(reason.getStatusCode());
					}
					if (reason.getException() != null)
					{
						detail.append(",");
						detail.append(reason.getException().getMessage());
					}
					if (detail.length() > 0)
					{
						detail.insert(0, " (");
						detail.append(")");
					}
					GWT.log(detail.toString());
					Log.error(detail.toString());
					if (errorHandler != null && reason.getStatusCode() != Response.SC_UNAUTHORIZED)
					{
						JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
						jsArray.set(0, reason.getStatusCode());
						jsArray.set(1, reason.getMessage());
						jsArray.set(2, foundset.getJavaScriptInstance());
						Executor.call(errorHandler, jsArray);
					}
					else
					{
						error(reason.getMessage());
						// if authentication failed, clear the current checked/unchecked credentials
						if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
						{
							// for solutions that have mustAuthenticate == false - this will be a bit weird, but the server does ask for authentication it seems, and the server is leading
							clearCredentials();
							if (currentFormWhenRemoteSearch == null) currentFormWhenRemoteSearch = formManager.getCurrentForm();
							afterLoginHandler = new IAfterLoginHandler()
							{
								@Override
								public void execute()
								{
									load(foundset, successCallback, errorHandler);
								}
							};
							formManager.showLogin(); // TODO we should have this available in scripting - so that the developer can use it in callback methods as well
							// should we also make onSolutionOpen get called again after this happens - after successful re-login?
						}
					}
				}
			});
		}
	}

	public void load(final JavaScriptObject successCallback, final JavaScriptObject errorHandler)
	{
		if (flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials() && !(offlineDataProxy.hasUncheckedCredentials()))
		{
			afterLoginHandler = new IAfterLoginHandler()
			{
				@Override
				public void execute()
				{
					load(successCallback, errorHandler);
				}
			};
			formManager.showLogin();
		}
		else
		{
			offlineDataProxy.loadOfflineData(getSolutionName(), new Callback<Integer, Failure>()
			{
				@Override
				public void onSuccess(Integer result)
				{
					try
					{
						//						Mobile.hideLoadingDialog();
						log("Done, loaded size: " + result);
						if (successCallback != null)
						{
							JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
							jsArray.set(0, result.doubleValue());
							Executor.call(successCallback, jsArray);
						}
						else showFirstForm();
					}
					finally
					{
						flagSyncStop();
					}
				}

				@Override
				public void onFailure(Failure reason)
				{
					try
					{
						//						Mobile.hideLoadingDialog();
						StringBuilder detail = new StringBuilder();
						if (reason.getStatusCode() != 0)
						{
							detail.append(reason.getStatusCode());
						}
						if (reason.getException() != null)
						{
							detail.append(",");
							detail.append(reason.getException().getMessage());
						}
						if (detail.length() > 0)
						{
							detail.insert(0, " (");
							detail.append(")");
						}
						GWT.log(detail.toString());
						Log.error(detail.toString());
						if (errorHandler != null && reason.getStatusCode() != Response.SC_UNAUTHORIZED)
						{
							JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
							jsArray.set(0, reason.getStatusCode());
							jsArray.set(1, reason.getMessage());
							Executor.call(errorHandler, jsArray);
						}
						else
						{
							// if authentication failed, clear the current checked/unchecked credentials
							if (reason.getStatusCode() == Response.SC_UNAUTHORIZED ||
								(reason.getStatusCode() == 0 && flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials()))
							{
								// for solutions that have mustAuthenticate == false - this will be a bit weird, but the server does ask for authentication it seems, and the server is leading,
								// so we don't show it as an error but will forward to the login page
								if (reason.getStatusCode() != Response.SC_UNAUTHORIZED || flattenedSolution.getMustAuthenticate()) error(reason.getMessage());
								clearCredentials();
								afterLoginHandler = new IAfterLoginHandler()
								{
									@Override
									public void execute()
									{
										load(successCallback, errorHandler);
									}
								};
								formManager.showLogin(); // TODO we should have this available in scripting - so that the developer can use it in callback methods as well
								// should we also make onSolutionOpen get called again after this happens - after successful re-login?
							}
							else if (reason.getStatusCode() != 0)
							{
								error(reason.getMessage());
							}
						}
					}
					finally
					{
						flagSyncStop();
					}
				}
			});
		}
	}

	public ScriptEngine getScriptEngine()
	{
		return scriptEngine;
	}

	public FormManager getFormManager()
	{
		return formManager;
	}

	public AngularBridge getAngularBridge()
	{
		return angularBridge;
	}

	public FoundSetManager getFoundSetManager()
	{
		return foundSetManager;
	}

	public FlattenedSolution getFlattenedSolution()
	{
		return flattenedSolution;
	}

	public boolean hasFirstFormADataSource()
	{
		return !Utils.stringIsEmpty(getFlattenedSolution().getFirstForm().getDataSource());
	}

	public void showFirstForm()
	{
		if (hasFirstFormADataSource() && flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials() && !foundSetManager.hasContent())
		{
			afterLoginHandler = new IAfterLoginHandler()
			{
				@Override
				public void execute()
				{
					// after login of a show first form we just need to do a full sync using the credentials
					sync(true);
				}
			};
			formManager.showLogin();
		}
		else
		{

			if (firstFormFirstShow)
			{
				firstFormFirstShow = false;

				if (flattenedSolution.getOnSolutionOpen() != null)
				{
					Executor.callFunction(flattenedSolution.getOnSolutionOpen(), null, null, null);
				}
			}

			// now show the first form.
			formManager.showFirstForm();
		}
	}

	public void doLogin(String identifier, String password)
	{
		offlineDataProxy.setUncheckedLoginCredentials(identifier, password);
		if (afterLoginHandler != null)
		{
			afterLoginHandler.execute();
			// can only executed once, then it should be cleared
			afterLoginHandler = null;
		}
	}

	public void clearCredentials()
	{
		offlineDataProxy.setUncheckedLoginCredentials(null, null);

	}

	//check to see if currently connected to IP network
	// seems this is not really reliable, especially when using phonegap
	public final native boolean isOnline()
	/*-{
		try {
			return $wnd.navigator.onLine;
		} catch (err) {
			//browser does not support onLine yet
			return true;
		}
	}-*/;

	public String getBrowserLocale()
	{
		String locale = "";
		String localeFromURL = Window.Location.getParameter("locale");
		if (localeFromURL != null)
		{
			locale = localeFromURL.replace('-', '_');
		}
		else
		{
			String browserLocale = getLocaleInternal();
			if (browserLocale != null)
			{
				locale = browserLocale.replace('-', '_');
			}
		}
		return locale;
	}

	private final native boolean testLocalStorage()
	/*-{
		var testKey = 'qeTest', storage = $wnd.window.sessionStorage;
		try { // Try and catch quota exceeded errors
			storage.setItem(testKey, '1');
			storage.removeItem(testKey);
		} catch (error) {
			if (error.code === DOMException.QUOTA_EXCEEDED_ERR
					&& storage.length === 0)
				$wnd
						.alert('Local storage not available this is likely due to private browsing mode, this is not supported for the mobile client.');
			$wnd._ServoyUtils_.error(error);
			return false;
		}
		return true;
	}-*/;

	private final native String getLocaleInternal()
	/*-{
		return $wnd.navigator.language;
	}-*/;

	public SolutionI18nProvider getI18nProvider()
	{
		return i18nProvider;
	}

	public IMobileSolutionModel getSolutionModel()
	{
		return solutionModel;

	}

	private native void exportLog()
	/*-{
		$wnd._ServoyUtils_.error = function(output) {
			output = output.toString();
			return @com.allen_sauer.gwt.log.client.Log::error(Ljava/lang/String;)(output);
		}
		$wnd._ServoyUtils_.warn = function(output) {
			output = output.toString();
			return @com.allen_sauer.gwt.log.client.Log::warn(Ljava/lang/String;)(output);
		}
	}-*/;

	public OfflineDataProxy getOfflineDataProxy()
	{
		return offlineDataProxy;
	}

	public interface IAfterLoginHandler
	{
		void execute();
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public void push(final JavaScriptObject successCallback, final JavaScriptObject errorHandler)
	{
		if (flattenedSolution.getMustAuthenticate() && !offlineDataProxy.hasCredentials() && !(offlineDataProxy.hasUncheckedCredentials()))
		{
			afterLoginHandler = new IAfterLoginHandler()
			{
				@Override
				public void execute()
				{
					push(successCallback, errorHandler);
				}
			};
			formManager.showLogin();
		}
		else
		{
			if (isSynchronizing()) return;
			//			Mobile.showLoadingDialog(getI18nMessageWithFallback("pushing"));
			flagSyncStart();
			if (foundSetManager.hasChanges())
			{
				//save and clear, when successful do load
				offlineDataProxy.saveOfflineData(new Callback<SaveOfflineDataParam, Failure>()
				{
					@Override
					public void onSuccess(SaveOfflineDataParam result)
					{
						try
						{
							//							Mobile.hideLoadingDialog();
							log("Done, submitted size: " + result.length);
							if (successCallback != null)
							{
								JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
								jsArray.set(0, result.length.doubleValue());
								jsArray.set(1, result.idRemoteIDMap.getJavaScriptObject());
								Executor.call(successCallback, jsArray);
							}
						}
						finally
						{
							flagSyncStop();
						}
					}

					@Override
					public void onFailure(Failure reason)
					{
						try
						{
							//							Mobile.hideLoadingDialog();
							if (errorHandler != null && reason.getStatusCode() != Response.SC_UNAUTHORIZED)
							{
								JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
								jsArray.set(0, reason.getStatusCode());
								jsArray.set(1, reason.getMessage());
								Executor.call(errorHandler, jsArray);
							}
							else
							{
								error(reason.getMessage());
								if (reason.getStatusCode() == -1)
								{
									showFirstForm();
								}
								else if (reason.getStatusCode() == Response.SC_UNAUTHORIZED)
								{
									// clear the current credentials and call push again.
									clearCredentials();
									push(successCallback, errorHandler);
								}
							}
						}
						finally
						{
							flagSyncStop();
						}
					}
				}, false);
			}
			else
			{
				try
				{
					//					Mobile.hideLoadingDialog();
					if (successCallback != null)
					{
						JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
						jsArray.set(0, 0);
						jsArray.set(1, new JSONObject().getJavaScriptObject());
						Executor.call(successCallback, jsArray);
					}
				}
				finally
				{
					flagSyncStop();
				}
			}
		}
	}
}
