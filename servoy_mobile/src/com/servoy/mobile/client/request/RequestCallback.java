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

import com.google.gwt.http.client.Response;

/**
 * @author lvostinar
 *
 */
public interface RequestCallback
{

	/**
	 * Called when a pending {@link com.google.gwt.http.client.Request} completes
	 * normally.  Note this method is called even when the status code of the 
	 * HTTP response is not "OK", 200. 
	 * 
	 * @param request the object that generated this event
	 * @param response an instance of the
	 *        {@link com.google.gwt.http.client.Response} class 
	 */
	void onResponseReceived(Request request, Response response);

	/**
	 * Called when a {@link com.google.gwt.http.client.Request} does not complete
	 * normally.  A {@link com.google.gwt.http.client.RequestTimeoutException RequestTimeoutException} is
	 * one example of the type of error that a request may encounter.
	 * 
	 * @param request the request object which has experienced the error
	 *     condition, may be null if the request was never generated
	 * @param exception the error that was encountered
	 */
	void onError(Request request, Throwable exception);
}