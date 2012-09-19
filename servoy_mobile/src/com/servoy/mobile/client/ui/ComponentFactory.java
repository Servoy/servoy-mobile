package com.servoy.mobile.client.ui;

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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.persistence.Tab;
import com.servoy.mobile.client.persistence.TabPanel;
import com.servoy.mobile.client.util.Utils;

/**
 * Create UI objects based on solution model objects
 * 
 * @author gboros
 */
public class ComponentFactory
{
	/**
	 * Create form display
	 * @param solutionModel the solution model
	 * @param form the solution model form to create the display for
	 * 
	 * @return form display
	 */
	public static IFormDisplay createFormDisplay(MobileClient application, Form form)
	{
		int viewType = form.getView();
		if (viewType == Form.VIEW_TYPE_LIST || viewType == Form.VIEW_TYPE_LIST_LOCKED)
		{
			return new ListFormDisplay(application, form);
		}

		JsArray<Component> formComponents = form.getComponents();
		Component component;
		BaseComponent.MobileProperties mobileProperties;
		TabPanel tabPanel = null;
		for (int i = 0; i < formComponents.length(); i++)
		{
			component = formComponents.get(i);
			mobileProperties = component.getMobileProperties();
			if (mobileProperties != null && mobileProperties.isFormTabPanel() && (tabPanel = component.isTabPanel()) != null)
			{
				return new TabsFormDisplay(application, form, tabPanel);
			}
		}

		return new SimpleFormDisplay(application, form);
	}

	/**
	 * Create component
	 * @param solutionModel the solution model
	 * @param component the solution model component to create UI for
	 * 
	 * @return UI component
	 */
	public static Widget createComponent(Solution solutionModel, Component component, Executor executor)
	{
		Widget componentWidget = null;
		String sizeProperty = null;
		GraphicalComponent gc = component.isGraphicalComponent();
		if (gc != null)
		{
			if (gc.isShowClick() && gc.getActionMethodID() != null)
			// button
			{
				componentWidget = new DataButton(gc, executor);
				((DataButton)componentWidget).setActionCommand(gc.getActionMethodID());
			}
			else
			// label
			{
				componentWidget = new DataLabel(gc);
			}
			sizeProperty = gc.getSize();
		}
		else
		{
			Field field = component.isField();
			if (field != null)
			{
				switch (field.getDisplayType())
				{
					case Field.DISPLAY_TYPE_TEXT_FIELD :
						componentWidget = new DataTextField(field);
						break;
					case Field.DISPLAY_TYPE_TEXT_AREA :
						componentWidget = new DataTextArea(field);
						break;
					case Field.DISPLAY_TYPE_COMBOBOX :
						componentWidget = new DataSelect(field);
						break;
					case Field.DISPLAY_TYPE_RADIOS :
						componentWidget = new DataRadioSet(field);
						break;
					case Field.DISPLAY_TYPE_CHECKS :
						componentWidget = new DataCheckboxSet(field);
						break;
					case Field.DISPLAY_TYPE_CALENDAR :
						componentWidget = new DataTextField(field);
						break;
					case Field.DISPLAY_TYPE_LIST_BOX :
						componentWidget = new DataList(field);
						break;
					case Field.DISPLAY_TYPE_PASSWORD :
						componentWidget = new DataPassword(field);
						break;
				}

				sizeProperty = field.getSize();
			}
			else
			{
				TabPanel tabPanel = component.isTabPanel();
				BaseComponent.MobileProperties mobileProperties = component.getMobileProperties();
				if (tabPanel != null && mobileProperties != null && mobileProperties.isListTabPanel())
				{
					JsArray<Tab> tabs = tabPanel.getTabs();

					if (tabs.length() > 0)
					{
						componentWidget = new FormList(solutionModel.getFormByUUID(tabs.get(0).getContainsFormID()));
						sizeProperty = tabPanel.getSize();
					}
				}
			}
		}

		if (sizeProperty != null)
		{
			int[] wh = Utils.splitAsIntegers(sizeProperty);
			if (wh != null && wh.length == 2) componentWidget.setPixelSize(wh[0], wh[1]);
		}

		return componentWidget;
	}
}