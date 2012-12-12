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
import com.servoy.j2db.persistence.constants.IFieldConstants;
import com.servoy.j2db.persistence.constants.IFormConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Tab;
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
			mobileProperties = component.getMobilePropertiesCopy();
			if (mobileProperties != null && mobileProperties.isFormTabPanel() && (tabPanel = component.isTabPanel()) != null)
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
	public static Widget createComponent(MobileClient application, Component component, DataAdapterList dal, Executor executor)
	{
		Widget componentWidget = null;
		GraphicalComponent gc = component.isGraphicalComponent();
		AbstractBase.MobileProperties mobileProperties;
		if (gc != null)
		{
			mobileProperties = gc.getMobilePropertiesCopy();

			if (mobileProperties != null)
			{
				if (mobileProperties.isHeaderText())
				{
					componentWidget = new DataFormHeader(gc, executor, application);
				}
				else if (mobileProperties.isHeaderLeftButton())
				{
					componentWidget = new DataFormHeaderButton(gc, DataFormHeaderButton.ORIENTATION_LEFT, executor, application);
				}
				else if (mobileProperties.isHeaderRightButton())
				{
					componentWidget = new DataFormHeaderButton(gc, DataFormHeaderButton.ORIENTATION_RIGHT, executor, application);
				}
			}

			if (componentWidget == null)
			{
				if (gc.isButton())
				{
					componentWidget = new DataButton(gc, executor, application);
				}
				else
				{
					componentWidget = new DataLabel(gc, executor, application);
				}
			}

			if (componentWidget instanceof IRuntimeComponentProvider) ((IRuntimeComponentProvider)componentWidget).getRuntimeComponent().setActionCommand(
				gc.getOnActionMethodCall());
		}
		else
		{
			Field field = component.isField();
			if (field != null)
			{
				ValueList valuelist = null;
				String valuelistID = field.getValuelistID();
				if (valuelistID != null)
				{
					valuelist = application.getSolution().getValueListByUUID(valuelistID);
				}
				switch (field.getDisplayType())
				{
					case IFieldConstants.TEXT_FIELD :
						componentWidget = new DataTextField(field, executor, application);
						break;
					case IFieldConstants.TEXT_AREA :
						componentWidget = new DataTextArea(field, executor, application);
						break;
					case IFieldConstants.COMBOBOX :
						componentWidget = new DataSelect(field, valuelist, executor, application);
						break;
					case IFieldConstants.RADIOS :
						componentWidget = new DataRadioSet(field, valuelist, executor, application);
						break;
					case IFieldConstants.CHECKS :
						componentWidget = new DataCheckboxSet(field, valuelist, executor, application);
						break;
					case IFieldConstants.CALENDAR :
						componentWidget = new DataTextField(field, executor, application);
						break;
					case IFieldConstants.PASSWORD :
						componentWidget = new DataPassword(field, executor, application);
						break;
				}

				if (componentWidget instanceof IRuntimeComponentProvider)
				{
					IRuntimeComponent scriptable = ((IRuntimeComponentProvider)componentWidget).getRuntimeComponent();
					scriptable.setActionCommand(field.getActionMethodCall());
					if (scriptable instanceof IRuntimeField) ((IRuntimeField)scriptable).setChangeCommand(field.getDataChangeMethodCall());
				}
			}
			else
			{
				TabPanel tabPanel = component.isTabPanel();
				mobileProperties = component.getMobilePropertiesCopy();
				if (tabPanel != null && mobileProperties != null && mobileProperties.isListTabPanel())
				{
					JsArray<Tab> tabs = tabPanel.getTabs();

					if (tabs.length() > 0)
					{
						Tab tab = tabs.get(0);
						Form form = application.getSolution().getFormByUUID(tab.getContainsFormID());
						FormController formController = application.getFormManager().getForm(form.getName());
						String relationName = tab.getRelationName();

						componentWidget = new FormList(formController, dal, application.getSolution().getRelation(relationName));
					}
				}
			}
		}

		if (componentWidget instanceof IComponent)
		{
			((IComponent)componentWidget).setEnabled(component.isEnabled());
			((IComponent)componentWidget).setVisible(component.isVisible());
		}

		if (componentWidget instanceof HasTheme)
		{
			((HasTheme)componentWidget).setTheme(component.getStyleClass());
		}

		return componentWidget;
	}
}