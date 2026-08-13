/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2024 Servoy BV

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

package com.servoy.mobile.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.JsArrayHelper;

/**
 * @author jcompagner
 *
 */
public class ApiSpec extends JavaScriptObject implements IApiParameters
{
	protected ApiSpec()
	{
	}

	public final native Array<Parameter> getParameters()/*-{
        return this.parameters;
    }-*/;

	public final native boolean getDelayUntilFormLoads()/*-{
        return this.delayUntilFormLoads;
    }-*/;

	public final native boolean getDiscardPreviouslyQueuedSimilarCalls()/*-{
	    return this.discardPreviouslyQueuedSimilarCalls;
	}-*/;

	/**
	 * Returns the return type name declared in the spec (e.g. {@code "string"}, {@code "date"}),
	 * or {@code null} when the API has no declared return value.
	 * The spec stores this as {@code returns.type} on the raw api JSON object.
	 */
	public final native String getReturnType()/*-{
	    return (this.returns && this.returns.type) ? this.returns.type : null;
	}-*/;

	/**
	 * Returns {@code true} when the API spec declares a non-void return value.
	 */
	public final boolean hasReturnValue()
	{
		return getReturnType() != null;
	}

	/**
	 * Mirror of {@code WebObjectApiFunctionDefinition.isAsync()}: returns {@code true} when the
	 * spec declares {@code "async": true} or {@code "async-now": true}.
	 * Note: the hyphen in "async-now" requires bracket notation in JSNI.
	 */
	public final native boolean isAsync()/*-{
	    return this.async === true || this['async-now'] === true;
	}-*/;

	/**
	 * Mobile-client mirror of {@code BaseWindow.isAsyncApiCall()}.
	 * A call is async (fire-and-forget, no sync return) when it has no return type AND is
	 * marked async/async-now.  A call with a declared return type is always treated as sync
	 * regardless of the async flag — you need a sync round-trip to get the return value back.
	 *
	 * @see org.sablo.websocket.BaseWindow#isAsyncApiCall
	 */
	public final boolean isAsyncApiCall()
	{
		return getReturnType() == null && isAsync();
	}

	@Override
	public final int getDefinedArgsCount()
	{
		Array<Parameter> params = getParameters();
		return params != null ? params.getLength() : 0;
	}

	@Override
	public final boolean isVarArgs()
	{
		Array<Parameter> params = getParameters();
		if (Array.isEmpty(params)) return false;
		return params.getAt(params.getLength() - 1).isVarArgs();
	}

	/**
	 * Mobile-client mirror of BaseWindow.processVarArgsIfNeeded() on the server side.
	 *
	 * If the last declared parameter is a varargs type (its spec type ends with "..."),
	 * all arguments from position <code>definedArgsCount-1</code> onwards are collected
	 * into a JS array and placed at that position, and the returned array is truncated to
	 * <code>definedArgsCount</code> entries — exactly matching what the real server does
	 * before sending a componentApis / serviceApis message to the Angular client.
	 *
	 * @see org.sablo.websocket.BaseWindow#processVarArgsIfNeeded
	 */
	public static Object[] processVarArgsIfNeeded(Object[] arguments, IApiParameters parameters)
	{
		if (parameters != null && arguments != null && arguments.length >= parameters.getDefinedArgsCount() && parameters.isVarArgs())
		{
			int definedArgsCount = parameters.getDefinedArgsCount();
			// collect all varargs into a JS array at the varargs position
			Array<Object> varArgs = JsArrayHelper.createArray();
			for (int i = definedArgsCount - 1; i < arguments.length; i++)
			{
				varArgs.push(arguments[i]);
			}
			Object[] newArguments = new Object[definedArgsCount];
			for (int i = 0; i < definedArgsCount - 1; i++)
			{
				newArguments[i] = arguments[i];
			}
			newArguments[definedArgsCount - 1] = varArgs;
			return newArguments;
		}
		return arguments;
	}

}
