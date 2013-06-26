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

package com.servoy.mobile.test.server.service;

import java.util.Arrays;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.servoy.base.test.IJSUnitSuiteHandler;
import com.servoy.base.test.IJSUnitSuiteHandler.TestCycleListener;
import com.servoy.mobile.test.shared.service.ITestSuiteController;

/**
 * @author acostescu
 */
public class TestSuiteController extends RemoteServiceServlet implements ITestSuiteController, TestCycleListener
{

	// this constant is also defined in testing.js inside servoy_mobile_testing; please update that as well if you change the value
	private static final String SCOPE_NAME_SEPARATOR = "_sNS_"; //$NON-NLS-1$

	// see http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	private IJSUnitSuiteHandler bridge;

	private final Object requestSequenceLock = new Object();
	private int requestSequenceCounter = -1; // test listener requests should be handled in-sequence
	private static final Log log = LogFactory.getLog("gwt-log"); //$NON-NLS-1$

	private boolean testSessionEnded = false;

	public TestSuiteController()
	{

	}

	@Override
	public void destroy()
	{
		if (!testSessionEnded && bridge != null) bridge.reportUnexpectedThrowable("The RPC servlet was destroyed before test session end...", null); //$NON-NLS-1$
		testSessionEnded();
		increaseSequenceCounter(); // to notify all waiting requests...
		super.destroy();
	}

	// TODO these 2 methods (getId and bridgeIDVerified) could actually only be 1 "boolean checkId(int)" - the check can be done right here then the listener registration performed right away
	@Override
	public int getId()
	{
		try
		{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup(IJSUnitSuiteHandler.JAVA_OBJECTS_CONTEXT_JNDI_PATH);
			@SuppressWarnings("unchecked")
			Map<String, Object> sharedMap = (Map<String, Object>)envContext.lookup(IJSUnitSuiteHandler.SERVOY_SHARED_MAP_JNDI_PATH);
			bridge = (IJSUnitSuiteHandler)sharedMap.get(IJSUnitSuiteHandler.SERVOY_BRIDGE_KEY);

			if (bridge == null)
			{
				log.error("Cannot locate (lookup) JUnit bridge handler. Is the server-side test environment setup correctly?"); //$NON-NLS-1$
				return -1; // won't match normally
			}
			testSessionEnded = false;
			requestSequenceCounter = -1;
		}
		catch (NamingException e)
		{
			log.error(
				"Cannot locate (lookup) JUnit bridge handler. The container (Tomcat, jetty, ...) probably doesn't have this resource mapped. It must be provided thorugh JNDI by the junit run / eclipse junit run / ... !",
				e);
			return -1; // won't match normally
		}
		return bridge.getId();
	}

	@Override
	public String[] bridgeIDVerified()
	{
		bridge.registerRunStartListener(this);
		return bridge.getCredentials();
	}

	@Override
	public void reportUnexpectedThrowable(String msg, Throwable t, int reqSequenceNo)
	{
		if (reqSequenceNo > 0) waitForSequence(reqSequenceNo); // this method can be called even before "runClientTests"; then it will be seq. 0 and it must have effect, without waiting
		if (bridge != null) bridge.reportUnexpectedThrowable(msg, t); // else servlet was destroyed while this was still waiting to happen; strange...
	}

	private void testSessionEnded()
	{
		testSessionEnded = true;
		bridge = null;
	}

	@Override
	public void reportUnexpectedThrowableMessage(String msg, String throwableMessage, int reqSequenceNo)
	{
		if (reqSequenceNo > 0) waitForSequence(reqSequenceNo); // this method can be called even before "runClientTests"; then it will be seq. 0 and it must have effect, without waiting
		if (bridge != null)
		{
			Throwable t = new Throwable(throwableMessage);
			t.setStackTrace(new StackTraceElement[0]); // stack trace is not really useful cause it was generated here
			bridge.reportUnexpectedThrowable(msg, t);
		} // else servlet was destroyed while this was still waiting to happen; strange...
	}

	@Override
	public String[] getJsUnitJavascriptCode()
	{
		return bridge.getJsUnitJavascriptCode();
	}

	@Override
	public String[] getSolutionJsUnitJavascriptCode()
	{
		return bridge.getSolutionJsUnitJavascriptCode();
	}

	@Override
	public void setFlattenedTestTree(String[] testTree)
	{
		bridge.setFlattenedTestTree(testTree);
	}

	// TODO can this be enhanced somehow to get more info from the browser js engine directly?
	// should never return null; it doesn't do much; best case scenario, for assertion failed errors it will give function names only.
	private Throwable populateThrowable(String[] throwableStack)
	{
		// stack is either null, or a one-element array with the description or something like (what JSUnit offers):
		// [
		//		[The description here]
		//		Assert_assertTrue(msg,cond), 
		//		scopes_sNS_globals_sNS_testRoundingFailure1()(f:0)
		//		TestClass0_testThatMustFailWithFailure(), 
		//		TestCase_runTest(), 
		//		TestCase_runBare(), 
		//		TestCase_runBare(), 
		//		TestResult_runProtected(test,p), 
		//		TestResult_run(test), 
		//		TestCase_run(result), 
		//		TestSuite_runTest(test,result), 
		//		TestSuite_run(result)
		// ]
		log.trace("Recreating throwable from stack: " + (throwableStack != null ? Arrays.asList(throwableStack) : null)); //$NON-NLS-1$
		String message = "?"; //$NON-NLS-1$
		if (throwableStack != null && throwableStack.length > 0)
		{
			message = throwableStack[0];
		}
		Throwable throwable = new Error(message);

		if (throwableStack != null && throwableStack.length > 1)
		{
			StackTraceElement[] stackTrace = new StackTraceElement[throwableStack.length - 1];
			for (int i = 1; i < throwableStack.length; i++)
			{
				String functionName = throwableStack[i];
				String scopes = "javascript"; //$NON-NLS-1$
				if (functionName.contains(SCOPE_NAME_SEPARATOR))
				{
					// this is then a function of a Servoy Solution - it also contains the scopes, not only the function name
					String[] tokens = functionName.split(SCOPE_NAME_SEPARATOR);
					functionName = tokens[tokens.length - 1];
					StringBuilder sb = new StringBuilder(throwableStack[i].length() - functionName.length() - SCOPE_NAME_SEPARATOR.length() + 1);
					for (int j = 0; j < tokens.length - 1; j++)
					{
						sb.append(tokens[j]);
						sb.append('.');
					}
					if (sb.length() > 1)
					{
						sb.setLength(sb.length() - 1); // remove last '.'
						scopes = sb.toString();
					}
				}
				stackTrace[i - 1] = new StackTraceElement(scopes, functionName, "f", 0); //$NON-NLS-1$
			}
			throwable.setStackTrace(stackTrace);
		}
		else
		{
			throwable.setStackTrace(new StackTraceElement[0]);
		}
		return throwable;
	}

	@Override
	public void addError(final String testName, final String[] throwableStack, int reqSequenceNo)
	{
		executeInSequence(new Runnable()
		{
			@Override
			public void run()
			{
				bridge.addError(testName, populateThrowable(throwableStack));
			}

		}, reqSequenceNo);
	}

	@Override
	public void addFailure(final String testName, final String[] throwableStack, int reqSequenceNo)
	{
		executeInSequence(new Runnable()
		{
			@Override
			public void run()
			{
				bridge.addFailure(testName, populateThrowable(throwableStack));
			}
		}, reqSequenceNo);
	}

	@Override
	public void startTest(final String testName, int reqSequenceNo)
	{
		executeInSequence(new Runnable()
		{
			@Override
			public void run()
			{
				bridge.startTest(testName);
			}
		}, reqSequenceNo);
	}

	@Override
	public void endTest(final String testName, int reqSequenceNo)
	{
		executeInSequence(new Runnable()
		{
			@Override
			public void run()
			{
				bridge.endTest(testName);
			}
		}, reqSequenceNo);
	}

	@Override
	public boolean isStopped()
	{
		return testSessionEnded ? true : (bridge != null ? bridge.isStopped() : false);
	}

	@Override
	public void doneTesting(int reqSequenceNo)
	{
		executeInSequence(new Runnable()
		{
			@Override
			public void run()
			{
				bridge.doneTesting();
			}
		}, reqSequenceNo);
	}

	public void started()
	{
		synchronized (requestSequenceLock)
		{
			requestSequenceCounter = 0;
			requestSequenceLock.notifyAll();
		}
	}

	public void finished()
	{
		synchronized (requestSequenceLock)
		{
			testSessionEnded();
			requestSequenceCounter = -1;
			requestSequenceLock.notifyAll();
		}
	}

	private void executeInSequence(Runnable r, int reqSequenceNo)
	{
		waitForSequence(reqSequenceNo);
		try
		{
			if (bridge != null) r.run(); // else servlet was destroyed; strange...
		}
		finally
		{
			increaseSequenceCounter();
		}
	}

	private void waitForSequence(int reqSequenceNo)
	{
		synchronized (requestSequenceLock)
		{
			while (requestSequenceCounter != reqSequenceNo && bridge != null) // quit waiting if servlet was destroyed...
			{
				try
				{
					requestSequenceLock.wait();
				}
				catch (InterruptedException e)
				{
					log.error(e);
				}
			}
		}
	}

	private void increaseSequenceCounter()
	{
		synchronized (requestSequenceLock)
		{
			requestSequenceCounter++;
			requestSequenceLock.notifyAll();
		}
	}

}
