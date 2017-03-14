/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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
package com.servoy.mobile.test.client;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.test.shared.service.ITestSuiteControllerAsync;

/**
 * This object is the jsUnit test result listener. It only contains the interface methods that will be
 * available to JS, and forwards requests to the handler.
 * 
 * @author acostescu
 * 
 */
@Export
public class JSUnitTestListener implements Exportable
{

	private final JSUnitTestListenerHandler handler;

	public JSUnitTestListener(ITestSuiteControllerAsync rpcController, TestMobileClient application)
	{
		this.handler = new JSUnitTestListenerHandler(rpcController, application);
	}

	// JS parameters (Test, Error)
	public void addError(JavaScriptObject test, JavaScriptObject throwable)
	{
		handler.addError(test, throwable);
	}

	// JS parameters (Test, AssertionFailedError)
	public void addFailure(JavaScriptObject test, JavaScriptObject assertionfailederror)
	{
		handler.addFailure(test, assertionfailederror);
	}

	// JS parameters (Test)
	public void endTest(JavaScriptObject test)
	{
		handler.endTest(test);
	}

	// JS parameters (Test)
	public void startTest(JavaScriptObject test)
	{
		handler.startTest(test);
	}

	public void setResult(JavaScriptObject result)
	{
		handler.setJSResult(result);
	}

}