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

/**
 * @author jcompagner
 *
 */
public class JsDate extends com.google.gwt.core.client.JsDate
{
	/**
	 * Non directly instantiable, use one of the {@link #create()} methods.
	 */
	protected JsDate()
	{
	}

	/**
	 * Creates a new date with the specified internal representation, which is the
	 * number of milliseconds since midnight on January 1st, 1970. This is the
	 * same representation returned by {@link #getTime()}.
	 */
	public static native JsDate create(double milliseconds) /*-{
	    return new Date(milliseconds);
	}-*/;

	/**
	 * Returns a date and time string in UTC.
	 */
	public final native String toISOString() /*-{
	    return this.toISOString();
	  }-*/;
}
