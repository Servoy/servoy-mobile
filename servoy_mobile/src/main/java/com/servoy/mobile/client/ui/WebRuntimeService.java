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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.JsArrayHelper;
import com.servoy.mobile.client.angular.JsPlainObj;

/**
 * @author lvostinar
 *
 */
public class WebRuntimeService implements Exportable
{
	@NoExport
	private final MobileClient mobileClient;
	@NoExport
	private final ComponentSpec type;
	@NoExport
	private final String name;

	/**
	 * @param controller
	 * @param webComponent
	 * @param type
	 */
	public WebRuntimeService(MobileClient mobileClient, String name, ComponentSpec type)
	{
		this.type = type;
		this.mobileClient = mobileClient;
		this.name = name;
	}

	@Export
	public boolean hasProperty(String key)
	{
		return type.getModel().has(key);
	}

	/**
	 * @param key
	 * @param value
	 */
	@Export
	public void setProperty(String key, Object value)
	{
		// just support api for now
	}

	@Export
	public Object getProperty(String key)
	{
		// just support api for now
		return null;
	}

	@Export
	public boolean hasApi(String key)
	{
		return type.getApi().has(key);
	}


	// will this always just be a return a promise??
	@Export
	public Object executeApi(String key, Object[] args)
	{
		MobileClient.log("args: " + args);
		ApiSpec apiSpec = type.getApi().get(key);
		if (apiSpec != null)
		{
			JsPlainObj call = new JsPlainObj();

			call.set("name", name);
			call.set("call", key);
			if (args != null && args.length > 0)
			{
				Array<Object> arguments = JsArrayHelper.createArray();
				for (Object arg : args)
				{
					// TODO should we convert the arguments... based on the api.getParamter types
					arguments.push(arg);
				}
				call.set("args", arguments);
			}
			Array<Object> calls = JsArrayHelper.createArray();
			calls.push(call);
			JsPlainObj apiCalls = new JsPlainObj();
			apiCalls.set("serviceApis", calls);

			mobileClient.getAngularBridge().sendMessage(apiCalls.toJSONString());
		}
		return null;
	}
}
