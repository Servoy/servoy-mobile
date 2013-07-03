package com.servoy.mobile.test.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.servoy.base.test.IJSUnitSuiteHandler;
import com.servoy.mobile.client.FormManager;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.test.shared.service.ITestSuiteController;
import com.servoy.mobile.test.shared.service.ITestSuiteControllerAsync;

/**
 * Mobile client that is able to automate some tasks for unit testing.
 * 
 * @author acostescu
 */
public class TestMobileClient extends MobileClient
{

	private ITestSuiteControllerAsync rpcController;
	private boolean bridgeIdChecked = false;
	private int bumpedSeq = 0;

	@Override
	public void onModuleLoad()
	{
		rpcController = (ITestSuiteControllerAsync)GWT.create(ITestSuiteController.class);
		runSafe(new Runnable()
		{
			@Override
			public void run()
			{
				if (rpcController == null) throw new RuntimeException("Cannot locate GWT RPC Servlet for controlling the test-suite."); //$NON-NLS-1$

				Log.info("[MobileJSUnitClient] initializing"); //$NON-NLS-1$
				GWT.create(JSUnitTestListener.class);
				GWT.create(JSUnitTestListenerHandler.TestResultBridge.class);
				TestMobileClient.super.initialize();
				Log.info("[MobileJSUnitClient] initialized; waiting for jQuery mobile page activation"); //$NON-NLS-1$
			}
		}, "Cannot initialize test client..."); //$NON-NLS-1$

	}

	// useful so that any unexpected exception can show up in the unit test view in developer, instead of just being logged in the browser
	public void runSafe(Runnable runnable, String somethingWentWrongMsg)
	{
		try
		{
			runnable.run();
		}
		catch (RuntimeException x)
		{
			reportUnexpectedThrowable(somethingWentWrongMsg, x);
			throw x;
		}
		catch (Error x)
		{
			reportUnexpectedThrowable(somethingWentWrongMsg, x);
			throw x;
		}
	}

	public int bumpedSequenceNumber()
	{
		return bumpedSeq++;
	}

	/**
	 * If there is any critical failure while testing, call this method.<BR>
	 * CAREFUL! This will normally terminate the test session server-side.
	 * @param msg a user friendly message explaining what went wrong.
	 * @param t the throwable that caused it. Can be null;
	 */
	public void reportUnexpectedThrowable(final String msg, final Throwable t)
	{
		final int seq = bumpedSequenceNumber(); // use the same seq. number to try throwable version and if that fails stringified throwable
		final boolean shouldLog[] = { !(t instanceof JavaScriptException) };
		if (rpcController != null)
		{
			rpcController.reportUnexpectedThrowable(msg, t, seq, new AsyncCallback<Void>()
			{
				@Override
				public void onSuccess(Void result)
				{
					// good
				}

				@Override
				public void onFailure(Throwable caught)
				{
					rpcController.reportUnexpectedThrowableMessage(msg,
						t != null ? t.getClass().getName() + ": " + t.getMessage() : "", seq, new AsyncCallback<Void>() //$NON-NLS-1$ //$NON-NLS-2$
						{
							@Override
							public void onSuccess(Void result)
							{
								// good
							}

							@Override
							public void onFailure(Throwable c)
							{
								// too bad; just log it, even if it's a JavaScriptException
								shouldLog[0] = true;
							}
						});
					if (shouldLog[0])
					{
						Log.error("reportUnexpectedThrowable cannot send throwable serverSide: " + msg, t); //$NON-NLS-1$
						Log.error("Cannot report unexpectedThrowable to test suite controller because of: ", caught); //$NON-NLS-1$
					} // JavaScriptException we already know it can't be serialised, and this exception is common; don't complain about it if it was sent successfully as a message
				}
			});
		}
		else
		{
			Log.error("Cannot report unexpectedThrowable to test suite controller because the controller is null: " + msg, t); //$NON-NLS-1$
		}
	}

	@Override
	protected void onStartPageShown()
	{
		if (bridgeIdChecked)
		{
			super.onStartPageShown();
		}
		else
		{
			bridgeIdChecked = true;
			rpcController.getId(new AsyncCallback<Integer>()
			{
				@Override
				public void onFailure(Throwable caught)
				{
					reportUnexpectedThrowable("Cannot check bridge id", caught); //$NON-NLS-1$
				}

				@Override
				public void onSuccess(final Integer result)
				{
					runSafe(new Runnable()
					{
						@Override
						public void run()
						{
							// check that &bid=[bridgeObjId] url argument matches the bridge id - to avoid trouble when running multiple times in developer
							if (result.toString().equals(Window.Location.getParameter(IJSUnitSuiteHandler.BRIDGE_ID_ARG)))
							{
								continueWithValidBridgeID();
							}
							else
							{
								Log.error("Cannot find correct bridge instance. (id is out of sync)"); //$NON-NLS-1$
							}
						}

					}, "Cannot login or sync with service solution..."); //$NON-NLS-1$
				}
			});
		}
	}

	private void continueWithValidBridgeID()
	{
		rpcController.bridgeIDVerified(new AsyncCallback<String[]>()
		{

			@Override
			public void onFailure(Throwable caught)
			{
				reportUnexpectedThrowable("Cannot report bridge ID verification", caught); //$NON-NLS-1$
			}

			@Override
			public void onSuccess(final String[] result)
			{
				runSafe(new Runnable()
				{

					@Override
					public void run()
					{
						Log.info("[MobileJSUnitClient] logging in automatically & syncing"); //$NON-NLS-1$

						// automatically login in case of test client
						if (getFlattenedSolution().getMustAuthenticate() && !getOfflineDataProxy().hasCredentials() && result != null)
						{
							setUncheckedLoginCredentials(result[0], result[1]);
						}
						// avoid trial page for testing
						getFlattenedSolution().setSkipConnect(true);

						// this will do a sync if necessary or simply show the first form if not
						TestMobileClient.super.onStartPageShown();
					}
				}, "Cannot login/sync/show first form (as needed)."); //$NON-NLS-1$
			}
		});
	}

	@Override
	public void sync(JavaScriptObject successCallback, JavaScriptObject errorHandler, boolean useUncheckedCredentials)
	{
		super.sync(successCallback, errorHandler, true); // always try to use supplied unchecked credentials (to log in automatically, even if sync is called later in test code)
	}

	@Override
	protected TestFormManager createFormManager()
	{
		return new TestFormManager(this);
	}

	@Override
	public void showFirstForm()
	{
		super.showFirstForm();

		// prepare and run tests
		if (!firstFormFirstShow)
		{
			runSafe(new Runnable()
			{
				@Override
				public void run()
				{
					new SolutionTestSuite(TestMobileClient.this, rpcController).runCurrentSolutionTestSuite();
				}
			}, "Problems running the solution test suite."); //$NON-NLS-1$
		}
		else
		{
			// login screen shown in mobile client?! - it usually doesn't even get this far (sync goes directly to formManager.showLogin(...) instead of showing first form)
			reportUnexpectedThrowable("Login screen was shown in test mobile client... Are the credentials (from launch configuration) correct?", null);
		}
	}

	@Override
	public void error(String msg)
	{
		// avoid modal dialogs
		Log.error("[MobileJSUnitClient] (client) Error message:" + msg); //$NON-NLS-1$
	}

	protected static class TestFormManager extends FormManager
	{

		protected TestFormManager(TestMobileClient mc)
		{
			super(mc);
		}

		@Override
		public void showLogin(JavaScriptObject successCallback, JavaScriptObject errorHandler)
		{
			// this is not acceptable in testing
			((TestMobileClient)getApplication()).reportUnexpectedThrowable(
				"Login page was shown in test mobile client... Are the auto-login credentials correct? Is the service solution available?", null);
		}
	}

}
