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

package com.servoy.mobile.client.ui;

import java.util.HashMap;
import java.util.Map;

import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.FormController;

/**
 * @author jcompagner
 *
 */
public class WebBaseComponent
{
	@NoExport
	protected final Map<String, Object> properties = new HashMap<>();
	@NoExport
	protected final FormController controller;

	/**
	 *
	 */
	public WebBaseComponent(FormController controller)
	{
		this.controller = controller;
	}

	/**
	 * @param key
	 * @param value
	 */
	public Object putBrowserProperty(String key, Object value)
	{
		Object prevValue = properties.put(key, value);
		return prevValue;
	}
}
