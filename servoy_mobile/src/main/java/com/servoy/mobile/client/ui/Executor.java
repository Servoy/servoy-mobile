package com.servoy.mobile.client.ui;

import java.util.Date;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsDate;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.scripting.JSEvent;
import com.servoy.mobile.client.util.Utils;

public class Executor
{

	private FormController formController;

	public Executor(FormController formController)
	{
		this.formController = formController;
	}

	public void destroy()
	{
		formController = null;
	}

	public void fireEventCommand(String type, String command, WebRuntimeComponent source, Object[] args)
	{
		JSEvent event = new JSEvent(type, source, formController.getName(), source == null ? null : source.getName());
		callFunction(command, args, formController.getName(), event);
	}

	/**
	 * @param command
	 * @param args
	 * @param event
	 */
	@SuppressWarnings("nls")
	public static Object callFunction(String command, Object[] args, String formName, JSEvent event)
	{
		int index = command.indexOf('(');
		String functionLookup = command.substring(0, index);
		String argumentsString = command.substring(index + 1, command.length() - 1);
		Object[] persistArgs = argumentsString.split(",");

		Object[] argsTmp = args;
		if (event != null) argsTmp = Utils.arrayJoin(argsTmp, new Object[] { ExporterUtil.wrap(event) });

		persistArgs = Utils.arrayMerge(argsTmp, persistArgs);

		JavaScriptObject function = null;
		if (!functionLookup.startsWith("scopes."))
		{
			if (formName == null) throw new RuntimeException("form name is not given, by calling a form method");
			function = getFunction("forms", formName, functionLookup);
		}
		else
		{
			String[] methodStack = functionLookup.split("\\.");
			function = getFunction(methodStack[0], methodStack[1], methodStack[2]);
		}

		JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
		for (int i = 0; i < persistArgs.length; i++)
		{
			Object argument = persistArgs[i];
			if (argument instanceof JavaScriptObject || argument == null)
			{
				jsArray.set(i, (JavaScriptObject)argument);
			}
			else
			{
				Object evalled = (args != null && i < args.length) ? argument : eval(argument);
				if (evalled instanceof String)
				{
					jsArray.set(i, (String)evalled);
				}
				else if (evalled instanceof Number)
				{
					jsArray.set(i, ((Number)evalled).doubleValue());
				}
				else if (evalled instanceof Boolean)
				{
					jsArray.set(i, ((Boolean)evalled).booleanValue());
				}
				else if (evalled instanceof JavaScriptObject)
				{
					jsArray.set(i, (JavaScriptObject)evalled);
				}
				else if (evalled instanceof Date)
				{
					jsArray.set(i, JsDate.create(((Date)evalled).getTime()));
				}
			}
		}
		return call(function, jsArray);
	}

	private static native Object eval(Object param)
	/*-{
		return $wnd.internal.Utils.wrapIfPrimitive($wnd.eval(param));
	}-*/;

	/**
	 * @param function
	 * @param string
	 */
	public static native Object call(JavaScriptObject func, JsArrayMixed params)
	/*-{
		var result = $wnd.internal.Utils.wrapIfPrimitive(func.apply(func, params));
		// If the called function is async it returns a Promise. Attach a .catch() so that
		// unhandled rejections (exceptions thrown inside the async handler after an await)
		// surface in the GWT log instead of being silently swallowed by the browser.
		// The promise itself is still returned so any future caller that needs to await it can.
		// Note: bracket notation is required because 'catch' is a reserved word in JS/GWT.
		if (result && typeof result.then === 'function') {
			result['catch'](function(err) {
				@com.servoy.mobile.client.MobileClient::log(Ljava/lang/String;)(
					"Unhandled error in async solution function: " + (err && err.message ? err.message : String(err)));
			});
		}
		return result;
	}-*/;

	/**
	 * @param functionName
	 * @return
	 */
	private static native JavaScriptObject getFunction(String topLevel, String scopeOrForm, String methodName)
	/*-{
		return $wnd[topLevel][scopeOrForm][methodName];
	}-*/;


}
