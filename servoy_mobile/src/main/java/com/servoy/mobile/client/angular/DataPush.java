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
public final class DataPush extends JavaScriptObject
{
	protected DataPush()
	{
	}

	/**
	 * @return
	 */
	public native JsPropertyMap<Object> getChanges()/*-{
	    return this.changes;
	}-*/;

	/**
	 * @return
	 */
	public native String getFormname()/*-{
        return this.formname;
    }-*/;

	/**
	 * @return
	 */
	public native String getBeanname()/*-{
	    return this.beanname;
	}-*/;
}
