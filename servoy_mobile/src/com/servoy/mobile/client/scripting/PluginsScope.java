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

import java.util.HashMap;
import java.util.Map;

import com.servoy.mobile.client.MobileClient;

/**
 * @author lvostinar
 *
 */
public class PluginsScope extends Scope
{
	protected final Map<String, Object> plugins = new HashMap<String, Object>();

	public PluginsScope(MobileClient client)
	{
		exportPlugins(client);
	}

	private void exportPlugins(MobileClient client)
	{
		export();
		plugins.put("mobile", new MobilePlugin(client));
		exportProperty("mobile");
	}

	private native void export()
	/*-{
		$wnd.plugins = this;
	}-*/;

	@Override
	public void setVariableType(String variable, int type)
	{

	}

	@Override
	public int getVariableType(String variable)
	{
		return 0;
	}

	@Override
	public Object getValue(String variable)
	{
		return plugins.get(variable);
	}

	@Override
	public void setValue(String variable, Object vaue)
	{

	}

}
