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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.persistence.WebComponent;
import com.servoy.mobile.client.util.Utils;

import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
public class WebRuntimeComponent implements Exportable, IRuntimeComponent
{
	@NoExport
	private final WebComponent webComponent;
	@NoExport
	private final ComponentSpec type;
	@NoExport
	private final Map<String, Object> properties = new HashMap<>();
	@NoExport
	private final FormController controller;

	@NoExport
	private JsPropertyMap<PropertySpec> dataproviderProperties;

	/**
	 * @param controller
	 * @param webComponent
	 * @param type
	 */
	public WebRuntimeComponent(FormController controller, WebComponent webComponent, ComponentSpec type)
	{
		this.controller = controller;
		this.webComponent = webComponent;
		this.type = type;
	}

	/**
	 * @return
	 */
	@NoExport
	public JsPropertyMap<Object> getJSON()
	{
		return webComponent.getJSON();
	}


	@NoExport
	public Any getJSONProperty(String property)
	{
		return webComponent.getJSON().getAsAny(property);
	}


	/**
	 * @return
	 */
	@Export(value = "getName")
	public String getName()
	{
		return webComponent.getName();
	}


	/**
	 *
	 */
	@NoExport
	public ComponentSpec getType()
	{
		return type;
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
		Object prevValue = properties.put(key, value);
		if (!Utils.equalObjects(value, prevValue))
		{
			JsPlainObj componentData = new JsPlainObj();
			componentData.set(key, controller.getView().convertValue(key, value, this));
			JsPlainObj formData = new JsPlainObj();
			formData.set(getName(), componentData);
			controller.getView().sendComponentData(formData);
		}
	}

	@Export
	public Object getProperty(String key)
	{
		Object object = properties.get(key);
		if (object == null)
		{
			object = getJSONProperty(key);
		}
		return object;
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
			return this.controller.getView().sendApiCall(this, key, args, apiSpec);
		}
		return null;
	}


	/**
	 * @return
	 */
	@SuppressWarnings("nls")
	@NoExport
	public JsPropertyMap<PropertySpec> getDataproviderProperties()
	{
		if (dataproviderProperties == null)
		{
			dataproviderProperties = Js.uncheckedCast(JsPropertyMap.of());
			type.getModel().forEach(property -> {
				PropertySpec propertyType = type.getModel().get(property);
				if ("dataprovider".equals(propertyType.getType()))
				{
					dataproviderProperties.set(property, propertyType);
				}

			});
		}
		return dataproviderProperties;
	}

	/**
	 * @param key
	 * @param value
	 */
	public Object putBrowserProperty(String key, Object value)
	{
		// todo value should be converted
		Object prevValue = properties.put(key, value);
		return prevValue;
	}
}
