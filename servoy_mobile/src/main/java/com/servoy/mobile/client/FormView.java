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

package com.servoy.mobile.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.servoy.mobile.client.angular.Array;
import com.servoy.mobile.client.angular.Handler;
import com.servoy.mobile.client.angular.JsArrayHelper;
import com.servoy.mobile.client.angular.JsDate;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.angular.Proxy;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.persistence.WebComponent;
import com.servoy.mobile.client.properties.CssPositionConvertor;
import com.servoy.mobile.client.properties.DataProviderConvertor;
import com.servoy.mobile.client.properties.FormatConvertor;
import com.servoy.mobile.client.properties.IPropertyConverter;
import com.servoy.mobile.client.scripting.ElementScope;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.ui.ApiSpec;
import com.servoy.mobile.client.ui.ComponentSpec;
import com.servoy.mobile.client.ui.IFormDisplay;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebBaseComponent;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Any;
import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
@SuppressWarnings("nls")
public class FormView extends WebBaseComponent implements IFormDisplay, IModificationListener
{
	private static final String MSG = "msg";
	private static final String COMPONENT_CALLS = "componentApis";


	private static final Map<String, IPropertyConverter> converters = new HashMap<>();

	static
	{
		converters.put("format", new FormatConvertor());
		converters.put("cssPosition", new CssPositionConvertor());
		converters.put("dataprovider", new DataProviderConvertor());
	}


	public static final String CONVERSION_CL_SIDE_TYPE_KEY = "_T";
	public static final String VALUE_KEY = "_V";

	// should be the same as in FormElement
	public static final String SVY_NAME_PREFIX = "svy_";

	private final Map<String, WebRuntimeComponent> components = new HashMap<String, WebRuntimeComponent>();
	private final List<Part> parts = new ArrayList<Part>();
	private final Map<String, Object> properties = new HashMap<>(); // form properties

	private Record record;
	private boolean fistShow = true;

	public FormView(FormController controller)
	{
		super(controller);
		createComponents();

		controller.getApplication().getScriptEngine().getGlobalScopeModificationDelegate().addModificationListener(this);
		controller.getFormScope().addModificationListener(this);
	}

	private void createComponents()
	{
		Form form = controller.getForm();
		ElementScope elementScope = controller.getFormScope().getElementScope();
		JsArray<Component> formComponents = form.getComponents();
		JsPropertyMap<ComponentSpec> specData = getSpecData();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Part part = Part.castIfPossible(component);
			if (part != null)
			{
				parts.add(part);
			}
			else
			{
				WebComponent webComponent = WebComponent.castIfPossible(component);
				if (webComponent != null)
				{
					String name = component.getName();
					if (name == null)
					{
						name = SVY_NAME_PREFIX + component.getUUID().replace('-', '_');
					}
					ComponentSpec type = specData.get(webComponent.getTypeName());
					WebRuntimeComponent runtimeComponent = new WebRuntimeComponent(this.controller, webComponent, type);
					components.put(name, runtimeComponent);
					JavaScriptObject wrap = ExporterUtil.wrap(runtimeComponent);
					elementScope.addComponent(name, Proxy.create(wrap, Handler.create()));

				}
			}
		}
	}

	@Override
	public WebRuntimeComponent getComponent(String beanName)
	{
		return components.get(beanName);
	}

	@Override
	public FormController getFormController()
	{
		return controller;
	}

	@Override
	public void setVisible(boolean visible)
	{
		if (visible && fistShow)
		{
			JsPlainObj formData = new JsPlainObj();
			components.values().forEach(webComponent -> {
				JsPlainObj componentData = new JsPlainObj();
				webComponent.getType().getModel().forEach(key -> {
					Object value = webComponent.getProperty(key);
					value = convertServerValue(key, value, webComponent);
					if (value != null)
					{
						componentData.set(key, value);
					}
				});
				formData.set(webComponent.getName(), componentData);
			});
			sendComponentData(formData);
			fistShow = false;
		}
	}

	public void refreshRecord(Record rec)
	{
		this.record = rec;
		components.values().forEach(c -> {
			JsPropertyMap<PropertySpec> dataproviderProperties = c.getDataproviderProperties();
			dataproviderProperties.forEach(key -> {
				Any dataprovider = c.getJSONProperty(key);
				if (dataprovider != null)
				{
					Object recordValue = getRecordValue(rec, dataprovider.asString());
					c.setProperty(key, convertServerValue(key, recordValue, c));
				}
			});
		});
	}

	@Override
	public void cleanup()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isShow()
	{
		return controller.getVisible();
	}


	@Override
	public void valueChanged(ModificationEvent e)
	{
		MobileClient.log("modification event: " + e);
		refreshRecord(record);
	}

	public Object getRecordValue(Record rec, String dataproviderID)
	{
		Object recordValue = null;
		if (dataproviderID != null)
		{
			String[] globalVariableScope = GlobalScope.getVariableScope(dataproviderID);

			if (globalVariableScope[0] != null)
			{
				recordValue = controller.getApplication().getScriptEngine().getGlobalScope(globalVariableScope[0]).getValue(globalVariableScope[1]);
			}
			else if (controller.getFormScope().hasVariable(dataproviderID))
			{
				recordValue = controller.getFormScope().getVariableValue(dataproviderID);
			}
			else if (rec != null)
			{
				recordValue = rec.getValue(dataproviderID);
			}
		}
		return recordValue;
	}

	public void setRecordValue(Record rec, String dataproviderID, Object value)
	{
		if (dataproviderID != null)
		{
			String[] globalVariableScope = GlobalScope.getVariableScope(dataproviderID);

			if (globalVariableScope[0] != null)
			{
				controller.getApplication().getScriptEngine().getGlobalScope(globalVariableScope[0]).setValue(globalVariableScope[1], value);
			}
			else if (controller.getFormScope().hasVariable(dataproviderID))
			{
				controller.getFormScope().setVariableValue(dataproviderID, value);
			}
			else if (rec != null)
			{
				rec.setValue(dataproviderID, value);
			}
		}
	}

	public Object convertServerValue(final String key, final Object value, WebRuntimeComponent component)
	{
		Object returnValue = value;
		ComponentSpec componentType = component.getType();
		PropertySpec propertyType = componentType.getModel().get(key);
		IPropertyConverter converter = null;
		if (propertyType != null)
		{
			String type = propertyType.getType();

			converter = converters.get(type);
		}
		else
		{
			converter = converters.get(key); // this can be stuff like cssPosition
		}
		if (converter != null)
		{
			returnValue = converter.convertForClient(value, component, propertyType, controller, record);
		}
		// some default conversions
		else if (value instanceof Date)
		{
			JsPlainObj conversionData = new JsPlainObj();
			conversionData.set(VALUE_KEY, JsDate.create(((Date)value).getTime()).toISOString());
			conversionData.set(CONVERSION_CL_SIDE_TYPE_KEY, "svy_date");

			returnValue = conversionData;
		}
		return returnValue;
	}


	@Override
	public Object convertClientValue(String key, Object value, WebRuntimeComponent component)
	{
		Object converted = value;
		ComponentSpec componentType = component.getType();
		PropertySpec propertyType = componentType.getModel().get(key);
		IPropertyConverter converter = null;
		if (propertyType != null)
		{
			String type = propertyType.getType();

			converter = converters.get(type);
		}
		else
		{
			converter = converters.get(key); // this can be stuff like cssPosition
		}
		if (converter != null)
		{
			converted = converter.convertFromClient(key, value, component, propertyType, controller);
		}
		return converted;
	}

	public void sendComponentData(JsPlainObj formData)
	{
		JsPlainObj forms = new JsPlainObj();
		forms.set(controller.getName(), formData);
		JsPlainObj msg = new JsPlainObj();
		msg.set("forms", forms);
		JsPlainObj call = new JsPlainObj();
		call.set(MSG, msg);

		controller.getApplication().getAngularBridge().sendMessage(call.toJSONString());
	}

	public Object sendApiCall(WebRuntimeComponent webRuntimeComponent, String key, Object[] args, ApiSpec api)
	{
		JsPlainObj call = new JsPlainObj();

		call.set("api", key);
		call.set("bean", webRuntimeComponent.getName());
		call.set("form", controller.getName());
		if (api.getDelayUntilFormLoads())
		{
			call.set("delayUntilFormLoads", true);
		}
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
		apiCalls.set(COMPONENT_CALLS, calls);

		controller.getApplication().getAngularBridge().sendMessage(apiCalls.toJSONString());
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.mobile.client.ui.IFormDisplay#pushChanges(com.servoy.mobile.client.ui.WebRuntimeComponent, java.lang.String)
	 */
	@Override
	public void pushChanges(WebRuntimeComponent component, String key)
	{
		Any dataprovider = component.getJSONProperty(key);
		if (dataprovider != null)
		{
			setRecordValue(record, dataprovider.asString(), component.getProperty(key));
		}

	}


	protected native JsPropertyMap<ComponentSpec> getSpecData()
	/*-{
		return $wnd._specdata_;
	}-*/;


}
