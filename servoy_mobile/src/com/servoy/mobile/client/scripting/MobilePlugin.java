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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.servoy.mobile.client.MobileClient;

/**
 * @author lvostinar
 *
 */
@Export
public class MobilePlugin implements Exportable
{
	private final MobileClient client;

	public MobilePlugin(MobileClient client)
	{
		this.client = client;
	}

	public boolean isOnline()
	{
		return client.isOnline();
	}

	public void syncData()
	{
		client.sync();
	}

	public native void getCurrentPosition(String successCallback, String errorHandler, String options)
	/*-{
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(eval('$wnd.'
					+ successCallback), errorHandler != null ? eval('$wnd.'
					+ errorHandler) : null, options);
		} else {
			alert("Sorry, browser does not support geolocation!");
		}
	}-*/;
}
