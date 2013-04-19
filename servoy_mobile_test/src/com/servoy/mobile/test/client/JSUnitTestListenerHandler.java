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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.servoy.mobile.test.shared.service.ITestSuiteControllerAsync;

/**
 * Class that is able to get relevant information from JS objects such as Test, TestResult, failure exceptions and error exceptions and
 * forward it to the server.
 * @author acostescu
 */
public class JSUnitTestListenerHandler
{

	private final ITestSuiteControllerAsync rpcController;
	private final AsyncCallback<Void> reportErrorCallback;
	private final TestMobileClient application;

	public JSUnitTestListenerHandler(ITestSuiteControllerAsync rpcController, final TestMobileClient application)
	{
		this.rpcController = rpcController;
		this.application = application;
		reportErrorCallback = new AsyncCallback<Void>()
		{
			public void onSuccess(Void v)
			{
				// good
			}

			public void onFailure(Throwable caught)
			{
				application.reportUnexpectedThrowable("Cannot send test progress/status to servlet...", caught); //$NON-NLS-1$
			}
		};
	}

	private native String getTestName(JavaScriptObject test)
	/*-{
		return test == null ? "null" : test.getName();
	}-*/;

	private String[] getThrowableStack(JavaScriptObject throwable)
	{
		String[] result = null;
		JsArrayString ts = getThrowableStackInternal(throwable);

		if (ts != null)
		{
			result = new String[ts.length()];
			for (int i = 0; i < ts.length(); i++)
			{
				result[i] = ts.get(i);
			}
		}
		return result;
	}

	private native JsArrayString getThrowableStackInternal(JavaScriptObject throwable)
	/*-{
		try {
			throw "";
		} catch (e) {
		}
		if (throwable != null) {
			if (throwable.mCallStack != null) {
				var stack = throwable.mCallStack.getStack();
				if (stack != null) stack.unshift(throwable.toString());
				return stack == null ? [ throwable.toString() ] : stack;
			} else {
				return [ '' + throwable ];
			}
		}
		return null;
	}-*/;

	public void addError(JavaScriptObject test, JavaScriptObject throwable)
	{
		rpcController.addError(getTestName(test), getThrowableStack(throwable), application.bumpedSequenceNumber(), reportErrorCallback);
	}

	public void addFailure(JavaScriptObject test, JavaScriptObject assertionfailederror)
	{
		rpcController.addFailure(getTestName(test), getThrowableStack(assertionfailederror), application.bumpedSequenceNumber(), reportErrorCallback);
	}

	public void endTest(JavaScriptObject test)
	{
		rpcController.endTest(getTestName(test), application.bumpedSequenceNumber(), reportErrorCallback);
	}

	public void startTest(JavaScriptObject test)
	{
		rpcController.startTest(getTestName(test), application.bumpedSequenceNumber(), reportErrorCallback);
	}

	public void setJSResult(JavaScriptObject result)
	{
		new TestResultBridge(result).pollAfterTimeout();
	}

	/**
	 * Only used to check server side result stop flag by polling the server.
	 */
	@Export
	public class TestResultBridge implements Exportable
	{
		public static final int poolInterval = 500; // ms
		private final JavaScriptObject result;

		private TestResultBridge(JavaScriptObject result)
		{
			this.result = result;
		}

		public void poll()
		{
			rpcController.isStopped(new AsyncCallback<Boolean>()
			{
				@Override
				public void onFailure(Throwable caught)
				{
					pollAfterTimeout();
					application.reportUnexpectedThrowable("Cannot poll for stopped state.", caught); // should we not do this? (as it will try to terminate the test session) //$NON-NLS-1$
				}

				@Override
				public void onSuccess(Boolean stopped)
				{
					if (stopped.booleanValue())
					{
						// was stopped by user from developer ui
						application.runSafe(new Runnable()
						{
							@Override
							public void run()
							{
								stopIt(result);
							}
						}, "Error stopping javascript test suite"); //$NON-NLS-1$
					}
					else
					{
						pollAfterTimeout();
					}
				}
			});
		}

		private void pollAfterTimeout()
		{
			setTimeout(ExporterUtil.wrap(this), poolInterval);
		}

		private native void setTimeout(JavaScriptObject thiis, int interval)
		/*-{
			$wnd.setTimeout(function() {
				thiis.poll();
			}, interval);
		}-*/;

		private native void stopIt(JavaScriptObject r)
		/*-{
			r.stop();
		}-*/;

	}

}
