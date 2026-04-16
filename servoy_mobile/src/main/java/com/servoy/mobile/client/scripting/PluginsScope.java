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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.angular.Handler;
import com.servoy.mobile.client.angular.Proxy;
import com.servoy.mobile.client.ui.ComponentSpec;
import com.servoy.mobile.client.ui.WebRuntimeService;

import jsinterop.base.JsPropertyMap;

/**
 * @author acostescu
 */
@Export
public class PluginsScope extends Scope implements Exportable
{

	private final MobileClient client;
	private final JavaScriptObject javascriptInstance;
	private final Map<String, Object> plugins = new HashMap<>();

	public PluginsScope(MobileClient client)
	{
		this.client = client;
		javascriptInstance = ExporterUtil.wrap(this);
		JsPropertyMap<ComponentSpec> specData = getSpecData();
		specData.forEach(pluginName -> {
			ComponentSpec type = specData.get(pluginName);
			WebRuntimeService runtimePlugin = new WebRuntimeService(client, pluginName, type);
			JavaScriptObject wrap = ExporterUtil.wrap(runtimePlugin);
			addPlugin(pluginName, Proxy.create(wrap, Handler.create()));
		});
		addPlugin("mobile", ExporterUtil.wrap(new MobilePlugin(client)));
		export(javascriptInstance);
	}

	public void addPlugin(String name, Object component)
	{
		exportProperty(javascriptInstance, name);
		plugins.put(name, component);
	}

	@Override
	public Object getValue(String variable)
	{
		return plugins.get(variable);
	}

	@Override
	public void setValue(String variable, Object value)
	{
		// ignore, readonly.
	}

	@Override
	public void setVariableType(String variable, int type)
	{
	}

	@Override
	public int getVariableType(String variable)
	{
		return -4; // media
	}

	private native void export(JavaScriptObject javaScriptObject) /*-{
		$wnd.plugins = javaScriptObject;
	}-*/;

	protected native JsPropertyMap<ComponentSpec> getSpecData()
	/*-{
		return $wnd._servicespecdata_;
	}-*/;
}
