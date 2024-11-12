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

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.persistence.WebComponent;
import com.servoy.mobile.client.util.Utils;

import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
public class WebRuntimeComponent
{

	private final WebComponent webComponent;
	private final JsPropertyMap<String> type;
	private final Map<String, Object> properties = new HashMap<>();
	private final FormController controller;

	/**
	 * @param controller
	 * @param webComponent
	 * @param type
	 */
	public WebRuntimeComponent(FormController controller, WebComponent webComponent, JsPropertyMap<String> type)
	{
		this.controller = controller;
		this.webComponent = webComponent;
		this.type = type;
	}


	public String getJSONProperty(String property)
	{
		return webComponent.getJSON().getAsAny(property).asString();
	}


	/**
	 * @return
	 */
	public String getName()
	{
		return webComponent.getName();
	}


	/**
	 *
	 */
	public JsPropertyMap<String> getType()
	{
		return type;
	}


	/**
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("nls")
	public void setProperty(String key, Object value)
	{
		Object prevValue = properties.put(key, value);
		if (!Utils.equalObjects(value, prevValue))
		{
			JsPlainObj componentData = new JsPlainObj();
			componentData.set(key, value);
			JsPlainObj form = new JsPlainObj();
			form.set(getName(), componentData);
			JsPlainObj forms = new JsPlainObj();
			forms.set(controller.getName(), form);
			JsPlainObj msg = new JsPlainObj();
			msg.set("forms", forms);
			JsPlainObj call = new JsPlainObj();
			call.set("msg", msg);

			controller.getApplication().getAngularBridge().sendMessage(call.toJSONString());
		}
	}


}
