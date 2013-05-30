/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.mobile.client.util;

import java.util.HashSet;
import java.util.Set;

import com.allen_sauer.gwt.log.client.Log;

/**
 * @author jcompagner
 *
 */
public class BrowserSupport
{
	private static Set<String> supportedTypes;

	private static Set<String> getSupportedTypes()
	{
		if (supportedTypes == null)
		{
			supportedTypes = new HashSet<String>();
			String[] set = fillSupportedTypes();
			for (String type : set)
			{
				supportedTypes.add(type);
			}
		}
		return supportedTypes;
	}

	private static native String[] fillSupportedTypes()
	/*-{
		var inputs = [ 'search', 'tel', 'url', 'email', 'datetime', 'date',
				'month', 'week', 'time', 'datetime-local', 'number', 'color',
				'range' ], len = inputs.length, uiSupport = [];

		for ( var i = 0; i < len; i++) {
			var input = document.createElement('input');
			input.setAttribute('type', inputs[i]);
			var notText = input.type !== 'text';

			if (notText && input.type !== 'search' && input.type !== 'tel') {
				input.value = 'testing';
				if (input.value !== 'testing' || input.type == "url"
						|| input.type == "email") {
					uiSupport.push(input.type);
				}
			}
		}
		return uiSupport;
	}-*/;


	public static boolean isSupportedType(String type)
	{
		return getSupportedTypes().contains(type);
	}
}
