package com.servoy.mobile.client.util;

import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.servoy.mobile.client.MobileClient;

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

/**
 * Failure object to combine some properties
 * @author jblok
 */
public class Failure
{
	private final String defaultMessage;
	private final Throwable e;
	private int statusCode = -1;
	private final MobileClient application;

	public Failure(MobileClient application, String msg, Throwable e, int statusCode)
	{
		this.defaultMessage = msg;
		this.e = e;
		this.statusCode = statusCode;
		this.application = application;
	}

	public Failure(MobileClient application, String msg, int statusCode)
	{
		this(application, msg, null, statusCode);
	}

	public Failure(MobileClient application, String msg, Throwable e)
	{
		this(application, msg, e, -1);
	}

	public Failure(MobileClient application, String msg)
	{
		this(application, msg, null, -1);
	}

	public String getMessage()
	{
		if (statusCode == Response.SC_UNAUTHORIZED)
		{
			return application.getI18nMessageWithFallback("authenticationFailed");
		}
		if (statusCode == Response.SC_SERVICE_UNAVAILABLE)
		{
			return application.getI18nMessageWithFallback("serviceNotAvailable");
		}
		if (e instanceof RequestTimeoutException)
		{
			return application.getI18nMessageWithFallback("requestTimeout");
		}
		return defaultMessage;
	}

	public Throwable getException()
	{
		return e;
	}

	public int getStatusCode()
	{
		return statusCode;
	}
}
