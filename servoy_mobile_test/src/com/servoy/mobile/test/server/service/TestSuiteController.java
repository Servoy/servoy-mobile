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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.servoy.base.test.IJSUnitSuiteHandler;
import com.servoy.base.test.IJSUnitSuiteHandler.TestCycleListener;
import com.servoy.base.test.ILineMapper;
import com.servoy.base.test.ILineMapper.LineMapping;
import com.servoy.base.test.LineMapper;
import com.servoy.mobile.test.shared.service.ITestSuiteController;

/**
 * @author acostescu
 */
public class TestSuiteController extends RemoteServiceServlet implements ITestSuiteController, TestCycleListener
{

	// this constant is also defined in testing.js inside servoy_mobile_testing and MobileExporter.java; please update those as well if you change the value
	private static final String SCOPE_NAME_SEPARATOR = "_sNS_"; //$NON-NLS-1$
	protected static final Pattern DETAILED_STACK_LINE_PARSER = Pattern.compile(".*solution_\\d+\\.js:(\\d+).*"); //$NON-NLS-1$

	// see http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	private IJSUnitSuiteHandler bridge;

	private final Object requestSequenceLock = new Object();
	private int requestSequenceCounter = -1; // test listener requests should be handled in-sequence
	private static final Log log = LogFactory.getLog("gwt-log"); //$NON-NLS-1$

	private boolean testSessionEnded = false;
	private ILineMapper lineMapper;

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
	public void setFlattenedTestTree(String[] testTree)
	{
		bridge.setFlattenedTestTree(testTree);
	}

	// should never return null
	private Throwable populateThrowable(String[][] stacks)
	{
		String[] jsUnitStack = stacks[0]; // generated by jsUnit lib code - contains only function names
		String[] detailedStack = stacks[1]; // generated by browser - contains more info if browser supports that

		// jsUnitStack stack is either null, or a one-element array with the description or something like (what JSUnit offers):
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

		log.trace("Recreating throwable from jsunit stack: " + (jsUnitStack != null ? Arrays.asList(jsUnitStack) : null)); //$NON-NLS-1$
		log.trace("Recreating throwable from native stack: " + (detailedStack != null ? Arrays.asList(detailedStack) : null)); //$NON-NLS-1$
		String message = "?"; //$NON-NLS-1$
		if (jsUnitStack != null && jsUnitStack.length > 0)
		{
			message = jsUnitStack[0];
		}
		Throwable throwable = new Error(message);

		if (jsUnitStack != null && jsUnitStack.length > 1)
		{
			StackTraceElement[] stackTrace = new StackTraceElement[jsUnitStack.length - 1];

			int detailedIndexDelta = 0; // correlate jsunit stack elements with detailed stack elements if available
			if (detailedStack != null)
			{
				boolean foundServoyMethod = false;
				for (int i = 1; i < jsUnitStack.length && (!foundServoyMethod); i++)
				{
					String functionName = jsUnitStack[i];
					if (functionName.contains(SCOPE_NAME_SEPARATOR))
					{
						foundServoyMethod = true;
						Integer didI = computeDetailedIndexDelta(functionName.substring(0, functionName.length() - 2), i, detailedStack); // drop the extra "()" in function name
						if (didI == null)
						{
							log.warn("Cannot use native stack for line numbers..."); //$NON-NLS-1$
							detailedStack = null;
						}
						else detailedIndexDelta = didI.intValue();
					}
				}

				if (!foundServoyMethod)
				{
					log.warn("Cannot use native stack for line numbers... No servoy method detected in stack."); //$NON-NLS-1$
					detailedStack = null;
				}
			}

			for (int i = 1; i < jsUnitStack.length; i++)
			{
				String functionName = jsUnitStack[i];
				String scopes = "javascript"; //$NON-NLS-1$
				if (functionName.contains(SCOPE_NAME_SEPARATOR))
				{
					// this is then a function of a Servoy Solution - it also contains the scopes, not only the function name
					String[] tokens = functionName.split(SCOPE_NAME_SEPARATOR);
					functionName = tokens[tokens.length - 1];
					StringBuilder sb = new StringBuilder(jsUnitStack[i].length() - functionName.length() - SCOPE_NAME_SEPARATOR.length() + 1);
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

				String fileName = "f";
				int lineNo = 0;

				// get line number and file from native stack if available
				if (detailedStack != null && (i + detailedIndexDelta < detailedStack.length))
				{
					LineMapping details = getFileAndLine(detailedStack[i + detailedIndexDelta], getLineNumberMapper());
					if (details != null)
					{
						lineNo = (int)details.lineNumber;
						fileName = details.file;
					}
				}

				stackTrace[i - 1] = new StackTraceElement(scopes, functionName, fileName, lineNo);
			}
			throwable.setStackTrace(stackTrace);
		}
		else
		{
			throwable.setStackTrace(new StackTraceElement[0]);
		}
		return throwable;
	}

	private ILineMapper getLineNumberMapper()
	{
		if (lineMapper == null)
		{
			try
			{
				lineMapper = LineMapper.fromProperties(getServletContext().getResourceAsStream("/mobiletestclient/lineMapping.properties")); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				log.error(e);
			}
		}
		return lineMapper;
	}

	public static LineMapping getFileAndLine(String stackLine, ILineMapper lineMapper)
	{
		// detailedStack stack is either null, or a one-element array with the description or something like (what browser offers):
		// [
		//		FF:
		//		myThrowingFunction@file:///C:/Users/X/Desktop/scriptus.js:5
		//		@file:///C:/Users/X/Desktop/test.html:10
		//	
		//		Chrome:
		//		Error: my error
		//		    at Error ()
		//		    at myThrowingFunction (file:///C:/Users/X/Desktop/scriptus.js:5:19)
		//		    at file:///C:/Users/X/Desktop/test.html:10:2
		//					
		//		IE 10 (fills the stack on the line it gets thrown, not when created => +1 line number in this case):
		//		Error: my error
		//		   at myThrowingFunction (file:///C:/Users/X/Desktop/scriptus.js:6:9)
		//		   at Global code (file:///C:/Users/X/Desktop/test.html:10:2)
		//				   
		//		Safari:
		//		Error: my error
		//		    at Error ()
		//		    at myThrowingFunction (file://localhost/Users/X/Desktop/bla/scriptus.js:5:19)
		//		    at file://localhost/Users/X/Desktop/bla/test.html:10:2
		// ]
		// 
		// real Chrome assertion failure example:
		// [
		//		Error: function testRoundingFailure1() {
		//			testRoundingFailure2();
		//		}
		//		at Error.JsUnitError (<anonymous>:94:13)
		//		at Error.AssertionFailedError (<anonymous>:46:17)
		//		at Assert_fail [as fail] (<anonymous>:690:15)
		//		at http://localhost:8080/servoy_sample_mobile_TEST/mobiletestclient/solution_1380814481891.js:841:10
		//		at ffffgggg (http://localhost:8080/servoy_sample_mobile_TEST/mobiletestclient/solution_1380814274963.js:841:10)
		//		at Object.scopes_sNS_globals_sNS_testRoundingFailure2 (http://localhost:8080/servoy_sample_mobile_TEST/mobiletestclient/solution_1380810752361.js:839:9)
		//		at Object.wrapper [as testRoundingFailure2] (http://localhost:8080/servoy_sample_mobile_TEST/mobiletestclient/servoy_utils_1380810752361.js:26:17)
		//		at Assert.TestClass20_testRoundingFailure2 [as testRoundingFailure2] (http://localhost:8080/servoy_sample_mobile_TEST/mobiletestclient/testSuite_generatedCode_1380810752361.js:6:77)
		//		at Assert.TestCase_runTest [as runTest] (<anonymous>:804:16)
		//		at Assert.TestCase_runBare [as runBare] (<anonymous>:785:14)
		//		at OnTheFly.TestResult_run.OnTheFly.protect (<anonymous>:384:58)
		//		at TestResult_runProtected [as runProtected] (<anonymous>:407:11)]
		// ]

		Matcher m = DETAILED_STACK_LINE_PARSER.matcher(stackLine);
		long mobileJSLineNumber = -1;
		LineMapping devLineAndFile = null;
		if (m.matches())
		{
			mobileJSLineNumber = Long.parseLong(m.group(1));
			devLineAndFile = lineMapper.mapToDeveloperScript(mobileJSLineNumber);
		}

		return devLineAndFile;
	}

	/**
	 * Searches for the function name in detailed stack and calculates the delta
	 */
	private Integer computeDetailedIndexDelta(String functionName, int jsUnitStackIndex, String[] detailedStack)
	{
		// going from last to first cause first few entries can contain custom text (error message with multiple lines or not) and we don't want to accidentally match on that
		int i = detailedStack.length - 1;
		while (i >= 0 && (!detailedStack[i].contains(functionName)))
		{
			i--;
		}
		return i < 0 ? null : Integer.valueOf(i - jsUnitStackIndex);
	}

	@Override
	public void addError(final String testName, final String[][] throwableStack, int reqSequenceNo)
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
	public void addFailure(final String testName, final String[][] throwableStack, int reqSequenceNo)
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
