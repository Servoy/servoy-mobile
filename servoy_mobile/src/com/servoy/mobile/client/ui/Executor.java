package com.servoy.mobile.client.ui;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.scripting.JSEvent;
import com.servoy.mobile.client.util.Utils;

public class Executor
{

	private final FormController formController;

	public Executor(FormController formController)
	{
		this.formController = formController;
	}

	public void fireEventCommand(String type, String command, Object source, Object[] args)
	{
		JSEvent event = new JSEvent(type, source, formController.getName(), null);

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
				Object evalled = eval(argument);
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
			}
		}
		return call(function, jsArray);
	}

	private static native Object eval(Object param)
	/*-{
		var evalled = $wnd.eval(param);
		if (typeof evalled == "number")
			evalled = new Number(evalled);
		if (typeof evalled == "boolean")
			evalled = new Boolean(evalled);
		return evalled;
	}-*/;

	/**
	 * @param function
	 * @param string
	 */
	private static native Object call(JavaScriptObject func, JsArrayMixed params)
	/*-{
		return $wnd.internal.Utils.wrapIfPrimitive(func.apply(func, params));
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
