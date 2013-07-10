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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for accessing the test suite controller RPC GWT Servlet from the client.
 * 
 * @author acostescu
 */
public interface ITestSuiteControllerAsync
{

	void getId(AsyncCallback<Integer> callback);

	void reportUnexpectedThrowable(String msg, Throwable t, int reqSequenceNo, AsyncCallback<Void> callback);

	void reportUnexpectedThrowableMessage(String msg, String throwableMessage, int reqSequenceNo, AsyncCallback<Void> callback);

	void getJsUnitJavascriptCode(AsyncCallback<String[]> callback);

	void setFlattenedTestTree(String[] testTree, AsyncCallback<Void> callback);

	void addError(String testName, String[] throwableStack, int reqSequenceNo, AsyncCallback<Void> callback);

	void addFailure(String testName, String[] throwableStack, int reqSequenceNo, AsyncCallback<Void> callback);

	void startTest(String testName, int reqSequenceNo, AsyncCallback<Void> callback);

	void endTest(String testName, int reqSequenceNo, AsyncCallback<Void> callback);

	void isStopped(AsyncCallback<Boolean> callback);

	void doneTesting(int reqSequenceNo, AsyncCallback<Void> callback);

	void bridgeIDVerified(AsyncCallback<String[]> asyncCallback);

}
