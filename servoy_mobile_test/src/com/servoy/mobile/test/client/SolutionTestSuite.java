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
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.servoy.mobile.test.shared.service.ITestSuiteControllerAsync;

/**
 * Class responsible for setting up and running the current solution test suite.
 * @author acostescu
 */
public class SolutionTestSuite implements Exportable
{

	private final TestMobileClient application;
	private final ITestSuiteControllerAsync rpcController;

	public SolutionTestSuite(TestMobileClient testMobileClient, ITestSuiteControllerAsync rpcController)
	{
		this.application = testMobileClient;
		this.rpcController = rpcController;
		export(ExporterUtil.wrap(this));
	}

	private native void export(Object object)
	/*-{
		$wnd.__solutionTestSuite = object;
	}-*/;

	public void runCurrentSolutionTestSuite()
	{
		// inject JSUnit related javascript code
		rpcController.getJsUnitJavascriptCode(new AsyncCallback<String[]>()
		{
			public void onSuccess(final String[] result)
			{
				application.runSafe(new Runnable()
				{
					@Override
					@SuppressWarnings("nls")
					public void run()
					{
						// unfortunately all JS (libs / suite code) need to be located directly in window scope, otherwise JsUtil's Function.prototype.glue
						// will not work correctly (it assumes that "this" contains function definitions - that are stored in current scope); and as the current scope cannot be
						// accessed in JS, nor this altered to match it, the only place left in browsers where the 2 are the same seems to be the main window.

						// if we need in the future to eval all this JS in a different scope (an isolated function scope), the "glue" method needs to change at least

						for (String code : result)
						{
							appendScriptTagToHead(code); // current scope and "this" must be the same so we have to eval the libs inside the window; we do not have acces to closure scopes programmatically, so we can't isolate the libs
						}

						// @formatter:off
						appendScriptTagToHead("function startTestSuiteInternal(testListener, startSuiteName) {\n"
//							+ "\ttry { throw ''; } catch (e) {}\n" // just for a kind of breakpoint in browser debug tool
							+ "\tvar result = new TestResult();\n"
							+ "\ttestListener.setResult(result);\n"
							+ "\tresult.addListener(testListener);\n"
							+ "\teval(startSuiteName + '.prototype.suite().run(result)');\n" +
						"}");
						// @formatter:on

						// generated suite code needs to be evaluated after the JSUnit library code is available (see code above)  
						appendScriptLinkTagToHead(GWT.getModuleBaseForStaticFiles() + getGeneratedScriptFileLocation());

						// when the script link above will get evaluated (unfortunately that doesn't happen synchronously it seems) it will call
						// method sendTestTreeAndRun() on this object
					}

				}, "Injecting library code / generating test suite failed."); //$NON-NLS-1$
			}

			public void onFailure(Throwable caught)
			{
				application.reportUnexpectedThrowable("Cannot get the required JSUnit library code or related JS code...", caught); //$NON-NLS-1$
			}
		});
	}

	@SuppressWarnings("nls")
	@Export
	public void sendTestTreeAndRun()
	{
		final String rootSuiteClassName = getRootTestSuiteClassNameAndPrepare();
		if (rootSuiteClassName != null)
		{
			rpcController.setFlattenedTestTree(getFlattenedTestTree(rootSuiteClassName), new AsyncCallback<Void>()
			{

				@Override
				public void onFailure(Throwable caught)
				{
					application.reportUnexpectedThrowable("Cannot set flattened test tree...", caught); //$NON-NLS-1$
				}

				@Override
				public void onSuccess(Void result)
				{
					application.runSafe(new Runnable()
					{
						@Override
						public void run()
						{
							startSuite(rootSuiteClassName);
						}
					}, "Error when trying to start the javascript testsuite.");
				}
			});
		}
		else
		{
			application.reportUnexpectedThrowable("Cannot get the javascript JS Unit suite code... Client-side generation not yet supported.", null); //$NON-NLS-1$
		}
	}

	protected void startSuite(String suiteName)
	{
		JSUnitTestListener testListener = new JSUnitTestListener(rpcController, application);
		startTestSuiteInternal(ExporterUtil.wrap(testListener), suiteName); // TODO the test suite name is hardcoded currently - change this
		rpcController.doneTesting(application.bumpedSequenceNumber(), new AsyncCallback<Void>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				application.reportUnexpectedThrowable("Unable to send the 'doneTesting' signal...", caught); //$NON-NLS-1$
			}

			@Override
			public void onSuccess(Void result)
			{
				// GREAT - we are done
			}
		});
	}

	private native void appendScriptLinkTagToHead(String src)
	/*-{
		var scriptTag = $wnd.document.createElement("script");
		scriptTag.setAttribute('language', 'javascript');
		scriptTag.setAttribute('src', src);
		$wnd.document.getElementsByTagName('head')[0].appendChild(scriptTag);
	}-*/;

	private native void appendScriptTagToHead(String code)
	/*-{
		var scriptTag = $wnd.document.createElement("script");
		scriptTag.setAttribute('language', 'javascript');
		var scriptString = $wnd.document.createTextNode(code);
		scriptTag.appendChild(scriptString);
		$wnd.document.getElementsByTagName('head')[0].appendChild(scriptTag);
	}-*/;

	private String[] getFlattenedTestTree(String suiteName)
	{
		JsArrayString testTree = getFlattenedTestTreeInternal(suiteName);
		String[] result = new String[testTree.length()];
		for (int i = 0; i < testTree.length(); i++)
		{
			result[i] = testTree.get(i);
		}
		return result;
	}

	// __rootTestSuiteClassName is part of an war included file - testSuite_generatedCode.js (generated by the developer exporter)
	private native String getRootTestSuiteClassNameAndPrepare()
	/*-{
		return $wnd.__rootTestSuiteClassName;
	}-*/;

	// __rootTestSuiteClassName is part of an war included (in GWT script dependencies) file - testSuite_generatedCodeLocation.js (generated by the developer exporter)
	private native String getGeneratedScriptFileLocation()
	/*-{
		return $wnd.__generatedCodeLocation;
	}-*/;

	private native JsArrayString getFlattenedTestTreeInternal(String suiteName)
	/*-{
		return $wnd.JsUnitToJava.prototype
				.getTestTree($wnd[suiteName].prototype.suite());
	}-*/;

	private native void startTestSuiteInternal(Object testListener, String startSuiteName)
	/*-{
		$wnd.startTestSuiteInternal(testListener, startSuiteName);
	}-*/;

}
