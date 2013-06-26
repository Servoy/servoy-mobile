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

package com.servoy.mobile.test.shared.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The contract of client-server communication that helps generate and drive the JUnit test suite serverside
 * based on what happens in the browser...
 * 
 * @author acostescu
 */
@RemoteServiceRelativePath("testSuiteController")
public interface ITestSuiteController extends RemoteService
{

	int getId();

	/**
	 * Returns either null for no credentials or a String[2] where index 0 is 'username' and index 1 is 'password' - for automatic
	 * test client authentication.
	 * @return see description.
	 */
	String[] bridgeIDVerified();

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void reportUnexpectedThrowable(String msg, Throwable t, int reqSequenceNo);

	/**
	 * This one will only get called if there was a problem calling {@link #reportUnexpectedThrowable(String, Throwable)} from the client (such as
	 * a GWT RPC serialization problem); it's better then to just report the message in junit instead of failing completely.
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void reportUnexpectedThrowableMessage(String msg, String throwableMessage, int reqSequenceNo);

	String[] getJsUnitJavascriptCode();

	void setFlattenedTestTree(String[] testTree);

	// TODO ac add a method to tell the client (browser side) which tests should run (with TestTarget in a shared proj?)

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void addError(String testName, String[] throwableStack, int reqSequenceNo);

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void addFailure(String testName, String[] throwableStack, int reqSequenceNo);

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void startTest(String testName, int reqSequenceNo);

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void endTest(String testName, int reqSequenceNo);

	boolean isStopped();

	/**
	 * @param reqSequenceNo as calls to methods in this interface can come through multiple threads async, this param. allows the client to specify the sequence in which calls will be processed.
	 * (useful when calls happen via browser AJAX async requests...) The first sequenceNumber that will be handled is 0, followed by 1, 2, ... .
	 */
	void doneTesting(int reqSequenceNo);

	/**
	 * Returns either - suggesting that the mobile client should build the suite itself null or a String[2] where index 0 is
	 * the test suite name and index 1 is the whole javascript code of the solution js unit testsuite.
	 * @return see description.
	 */
	String[] getSolutionJsUnitJavascriptCode();

}
