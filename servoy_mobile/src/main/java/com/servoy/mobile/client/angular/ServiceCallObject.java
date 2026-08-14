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

package com.servoy.mobile.client.angular;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
public class ServiceCallObject extends JavaScriptObject
{
	protected ServiceCallObject()
	{
	}

	public final native String getServiceName() /*-{
		return this.service ? this.service : null;
	}-*/;

	public final native String getMethodName() /*-{
		return this.methodname ? this.methodname : null;
	}-*/;

	public final native JsPropertyMap<Object> getArgs() /*-{
		return this.args ? this.args : null;
	}-*/;

	public final native String getCmsgId() /*-{
		return this.cmsgid ? this.cmsgid : null;
	}-*/;

	/**
	 * Returns the smsgid from a GWT-initiated sync API call response sent back by Angular.
	 * Angular echoes back whatever smsgid GWT included in the outgoing serviceApis/componentApis message.
	 */
	public final native String getSmsgId() /*-{
		return (this.smsgid !== undefined && this.smsgid !== null) ? String(this.smsgid) : null;
	}-*/;

	/**
	 * Returns the ret value from an Angular response to a GWT-initiated sync API call.
	 */
	public final native Object getRet() /*-{
		return this.ret !== undefined ? this.ret : null;
	}-*/;

	/**
	 * Returns the err string from an Angular error response to a GWT-initiated sync API call.
	 * Angular sends {smsgid, err: '...'} when the client-side execution of the call failed.
	 */
	public final native String getErr() /*-{
		return this.err !== undefined ? String(this.err) : null;
	}-*/;
}
