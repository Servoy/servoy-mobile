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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.persistence.WebComponent;
import com.servoy.mobile.client.scripting.GlobalScope;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.ui.IFormDisplay;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.JsPropertyMap;

/**
 * @author jcompagner
 *
 */
public class FormView implements IFormDisplay, IModificationListener
{
	// should be the same as in FormElement
	public static final String SVY_NAME_PREFIX = "svy_";

	private final FormController controller;

	private final Map<String, WebRuntimeComponent> components = new HashMap<String, WebRuntimeComponent>();
	private final List<Part> parts = new ArrayList<Part>();

	private Record record;

	public FormView(FormController controller)
	{
		this.controller = controller;
		createComponents();

		controller.getApplication().getScriptEngine().getGlobalScopeModificationDelegate().addModificationListener(this);
		controller.getFormScope().addModificationListener(this);
	}

	private void createComponents()
	{
		Form form = controller.getForm();
		JsArray<Component> formComponents = form.getComponents();
		JsPropertyMap<Object> specData = getSpecData();
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
					JsPropertyMap<String> type = specData.getAsAny(webComponent.getTypeName()).cast();
					components.put(name, new WebRuntimeComponent(this.controller, webComponent, type));
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
	public void removeDisplayPanel(String parentFormName)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public FormController getFormController()
	{
		return controller;
	}

	@Override
	public void initWithRecord(Record record)
	{
	}

	public void refreshRecord(Record record)
	{
		this.record = record;
		components.values().forEach(c -> {
			JsPropertyMap<String> type = c.getType();
			type.forEach(key -> {
				String typeName = type.get(key);
				if (typeName != null && typeName.equals("dataprovider"))
				{
					String dataprovider = c.getJSONProperty(key);
					if (dataprovider != null)
					{
						Object recordValue = getRecordValue(record, dataprovider);
						c.setProperty(key, recordValue);
					}
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

	public Object getRecordValue(Record record, String dataproviderID)
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
			else if (record != null)
			{
				recordValue = record.getValue(dataproviderID);
			}
		}
		return recordValue;
	}


	protected native JsPropertyMap<Object> getSpecData()
	/*-{
		return $wnd._specdata_;
	}-*/;


}
