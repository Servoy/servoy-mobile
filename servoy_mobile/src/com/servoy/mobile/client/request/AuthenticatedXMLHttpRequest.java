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

import com.google.gwt.xhr.client.XMLHttpRequest;

/**
 * @author lvostinar
 *
 */
public class AuthenticatedXMLHttpRequest extends XMLHttpRequest
{
	protected AuthenticatedXMLHttpRequest()
	{
	}

	public static native AuthenticatedXMLHttpRequest create() /*-{
		// Don't check window.XMLHttpRequest, because it can
		// cause cross-site problems on IE8 if window's URL
		// is javascript:'' .
		if ($wnd.XMLHttpRequest) {
			return new $wnd.XMLHttpRequest();
		} else {
			try {
				return new $wnd.ActiveXObject('MSXML2.XMLHTTP.3.0');
			} catch (e) {
				return new $wnd.ActiveXObject("Microsoft.XMLHTTP");
			}
		}
	}-*/;

	public final native void addWithCredentials()
	/*-{
		this.withCredentials = true;
		;
	}-*/;
}
