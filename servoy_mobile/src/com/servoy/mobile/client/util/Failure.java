package com.servoy.mobile.client.util;

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
	private String msg;
	private Throwable e;
	private int statusCode = -1;

	public Failure(String msg,Throwable e,int statusCode)
	{
		this.msg = msg;
		this.e = e;
		this.statusCode = statusCode;
	}
	public Failure(String msg,int statusCode)
	{
		this(msg, null, statusCode);
	}
	public Failure(String msg,Throwable e)
	{
		this(msg, e, -1);
	}
	public Failure(String msg)
	{
		this(msg, null, -1);
	}
	
	public String getMessage() 
	{
		return msg;
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
