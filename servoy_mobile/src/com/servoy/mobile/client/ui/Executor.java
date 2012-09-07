package com.servoy.mobile.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

public class Executor
{

	private final FormPage formPage;

	public Executor(FormPage formPage)
	{
		this.formPage = formPage;
	}

	public void execute(String command)
	{
		JavaScriptObject function = null;
		if (!command.startsWith("scopes."))
		{
			function = getFunction("forms", formPage.getName(), command);
		}
		else
		{
			String[] methodStack = command.split("\\.");
			function = getFunction(methodStack[0], methodStack[1], methodStack[2]);
		}

		call(function, "test");
	}

	/**
	 * @param function
	 * @param string
	 */
	private native void call(JavaScriptObject func, String param)
	/*-{
		func(param);
	}-*/;

	/**
	 * @param functionName
	 * @return
	 */
	private native JavaScriptObject getFunction(String topLevel, String scopeOrForm, String methodName)
	/*-{
	    return $wnd[topLevel][scopeOrForm][methodName]
	}-*/;


}
