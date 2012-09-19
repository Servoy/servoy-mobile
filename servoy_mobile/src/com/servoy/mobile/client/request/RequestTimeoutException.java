/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

package com.servoy.mobile.client.request;

import com.google.gwt.http.client.RequestException;

/**
 * @author lvostinar
 *
 */
public class RequestTimeoutException extends RequestException
{
	private static String formatMessage(int timeoutMillis)
	{
		return "A request timeout has expired after " + Integer.toString(timeoutMillis) + " ms";
	}

	/**
	 * Time, in milliseconds, of the timeout.
	 */
	private final int timeoutMillis;

	/**
	 * Request object which experienced the timed out.
	 */
	private final Request request;

	/**
	 * Constructs a timeout exception for the given {@link Request}.
	 * 
	 * @param request the request which timed out
	 * @param timeoutMillis the number of milliseconds which expired
	 */
	public RequestTimeoutException(Request request, int timeoutMillis)
	{
		super(formatMessage(timeoutMillis));
		this.request = request;
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 * Returns the {@link Request} instance which timed out.
	 * 
	 * @return the {@link Request} instance which timed out
	 */
	public Request getRequest()
	{
		return request;
	}

	/**
	 * Returns the request timeout value in milliseconds.
	 * 
	 * @return the request timeout value in milliseconds
	 */
	public int getTimeoutMillis()
	{
		return timeoutMillis;
	}
}
