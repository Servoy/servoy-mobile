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
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IFieldConstants;
import com.servoy.base.persistence.constants.IFormConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Portal;
import com.servoy.mobile.client.persistence.TabPanel;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.servoy.mobile.client.scripting.IRuntimeField;
import com.sksamuel.jqm4gwt.HasTheme;

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
	public static IFormDisplay createFormDisplay(MobileClient application, FormController formController)
	{
		Form form = formController.getForm();
		int viewType = form.getView();
		if (viewType == IFormConstants.VIEW_TYPE_TABLE || viewType == IFormConstants.VIEW_TYPE_TABLE_LOCKED)
		{
			return new ListFormDisplay(application, formController);
		}

		JsArray<Component> formComponents = form.getComponents();
		Component component;
		AbstractBase.MobileProperties mobileProperties;
		TabPanel tabPanel = null;
		for (int i = 0; i < formComponents.length(); i++)
		{
			component = formComponents.get(i);
			mobileProperties = component.getMobileProperties();
			if (mobileProperties != null && mobileProperties.getPropertyValue(IMobileProperties.FORM_TAB_PANEL).booleanValue() &&
				(tabPanel = TabPanel.castIfPossible(component)) != null)
			{
				return new TabsFormDisplay(application, form, tabPanel);
			}
		}

		return new SimpleFormDisplay(application, formController);
	}

	/**
	 * Create component
	 * @param solutionModel the solution model
	 * @param component the solution model component to create UI for
	 *
	 * @return UI component
	 */
	public static Widget createComponent(MobileClient application, Component component, DataAdapterList dal, FormController formController)
	{
		Widget componentWidget = null;
		GraphicalComponent gc = GraphicalComponent.castIfPossible(component);
		if (gc != null)
		{
			AbstractBase.MobileProperties mobileProperties = gc.getMobileProperties();
			if (mobileProperties != null)
			{
				if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_TEXT).booleanValue())
				{
					componentWidget = new DataFormHeader(gc, formController.getExecutor(), application);
				}
				else if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_LEFT_BUTTON).booleanValue())
				{
					componentWidget = new DataFormHeaderButton(gc, DataFormHeaderButton.ORIENTATION_LEFT, formController.getExecutor(), application);
				}
				else if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_RIGHT_BUTTON).booleanValue())
				{
					componentWidget = new DataFormHeaderButton(gc, DataFormHeaderButton.ORIENTATION_RIGHT, formController.getExecutor(), application);
				}
			}

			if (componentWidget == null)
			{
				if (gc.isButton())
				{
					componentWidget = new DataButton(gc, formController.getExecutor(), application);
				}
				else
				{
					componentWidget = new DataLabel(gc, formController.getExecutor(), application);
				}
			}

			if (componentWidget instanceof IRuntimeComponentProvider) ((IRuntimeComponentProvider)componentWidget).getRuntimeComponent().setActionCommand(
				gc.getOnActionMethodCall());
		}
		else
		{
			Field field = Field.castIfPossible(component);
			if (field != null)
			{
				ValueList valuelist = null;
				String valuelistID = field.getValuelistID();
				if (valuelistID != null)
				{
					valuelist = application.getFlattenedSolution().getValueListByUUID(valuelistID);
				}
				switch (field.getDisplayType())
				{
					case IFieldConstants.TEXT_FIELD :
						componentWidget = new DataTextField(field, formController.getExecutor(), application);
						break;
					case IFieldConstants.TEXT_AREA :
						componentWidget = new DataTextArea(field, formController.getExecutor(), application);
						break;
					case IFieldConstants.COMBOBOX :
						componentWidget = new DataSelect(field, valuelist, formController.getExecutor(), application);
						break;
					case IFieldConstants.RADIOS :
						componentWidget = new DataRadioSet(field, valuelist, formController.getExecutor(), application);
						break;
					case IFieldConstants.CHECKS :
						componentWidget = new DataCheckboxSet(field, valuelist, formController.getExecutor(), application);
						break;
					case IFieldConstants.CALENDAR :
						componentWidget = new DataTextField(field, formController.getExecutor(), application);
						break;
					case IFieldConstants.PASSWORD :
						componentWidget = new DataPassword(field, formController.getExecutor(), application);
						break;
				}

				if (componentWidget instanceof IRuntimeComponentProvider)
				{
					IRuntimeComponent scriptable = ((IRuntimeComponentProvider)componentWidget).getRuntimeComponent();
					scriptable.setActionCommand(field.getActionMethodCall());
					if (scriptable instanceof IRuntimeField)
					{
						((IRuntimeField)scriptable).setChangeCommand(field.getDataChangeMethodCall());
						((IRuntimeField)scriptable).setPlaceholderText(field.getPlaceholderText());
					}
				}
			}
			else
			{
				Portal portal = Portal.castIfPossible(component);
				if (portal != null)
				{
					AbstractBase.MobileProperties mobileProperties = component.getMobileProperties();
					if (mobileProperties != null && mobileProperties.getPropertyValue(IMobileProperties.LIST_COMPONENT).booleanValue())
					{
						componentWidget = new FormListComponent(portal, formController, dal, application);
					}
				}
			}
		}

		if (componentWidget instanceof IComponent)
		{
			((IComponent)componentWidget).setEnabled(component.getEnabled());
			((IComponent)componentWidget).setVisible(component.getVisible());
		}

		if (componentWidget instanceof HasTheme)
		{
			((HasTheme< ? >)componentWidget).setTheme(component.getStyleClass());
		}

		return componentWidget;
	}
}