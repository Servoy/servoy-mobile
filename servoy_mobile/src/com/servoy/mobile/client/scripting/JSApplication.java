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

/**
 * @author jcompagner
 *
 */
public class JSApplication
{
	public JSApplication()
	{
		export();
	}

	public native void output(Object output)
	/*-{
		$wnd.alert(output);
	}-*/;

	private native void export()
	/*-{
		$wnd.application = this;
		$wnd.application.output = function(output) {
			if (typeof (output) == 'number') {
				output = new Number(output);
			} else if (typeof (output) == 'boolean') {
				if (output)
					output = "true";
				else
					output = "false";
			}
			$wnd.application.@com.servoy.mobile.client.scripting.JSApplication::output(Ljava/lang/Object;)(output);
		}
	}-*/;

}
