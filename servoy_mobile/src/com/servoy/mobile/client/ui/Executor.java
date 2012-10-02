package com.servoy.mobile.client.ui;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.servoy.mobile.client.scripting.JSEvent;
import com.servoy.mobile.client.util.Utils;

public class Executor
{

	private final FormPage formPage;

	public Executor(FormPage formPage)
	{
		this.formPage = formPage;
	}

	public void fireEventCommand(String type, String command, Object source, Object[] args)
	{
		int index = command.indexOf('(');
		String functionLookup = command.substring(0, index);
		String argumentsString = command.substring(index + 1, command.length() - 1);
		Object[] persistArgs = argumentsString.split(",");

		JSEvent event = new JSEvent(type, source);

		Object[] functionArgs = Utils.arrayMerge(Utils.arrayJoin(args, new Object[] { ExporterUtil.wrap(event) }), persistArgs);

		JavaScriptObject function = null;
		if (!functionLookup.startsWith("scopes."))
		{
			function = getFunction("forms", formPage.getName(), functionLookup);
		}
		else
		{
			String[] methodStack = functionLookup.split("\\.");
			function = getFunction(methodStack[0], methodStack[1], methodStack[2]);
		}

		JsArrayMixed jsArray = JavaScriptObject.createArray().cast();
		for (int i = 0; i < functionArgs.length; i++)
		{
			Object argument = functionArgs[i];
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
				else if (evalled instanceof JavaScriptObject)
				{
					jsArray.set(i, (JavaScriptObject)evalled);
				}
			}
		}
		call(function, jsArray);
	}

	private native Object eval(Object param)
	/*-{
	  var evalled = $wnd.eval(param);
	  if (typeof evalled == "number") evalled = new Number(evalled);
	  return evalled;
	}-*/;

	/**
	 * @param function
	 * @param string
	 */
	private native void call(JavaScriptObject func, JsArrayMixed params)
	/*-{
		func.apply(func,params);
	}-*/;

	/**
	 * @param functionName
	 * @return
	 */
	private native JavaScriptObject getFunction(String topLevel, String scopeOrForm, String methodName)
	/*-{
	    return $wnd[topLevel][scopeOrForm][methodName];
	}-*/;


}
