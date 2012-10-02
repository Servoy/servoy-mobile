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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.JSEvent;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;
import com.sksamuel.jqm4gwt.toolbar.JQMToolBarButton;

/**
 * Form UI
 * 
 * @author gboros
 */
public class FormPage extends JQMPage
{
	protected final MobileClient application;
	private final Form form;
	private final Executor executor;
	private FormScope formScope;
	private boolean enabled = true;

	public FormPage(MobileClient application, Form form)
	{
		this.application = application;
		this.form = form;
		this.executor = new Executor(this);

		JsArray<Component> formComponents = form.getComponents();

		Component headerLabel = null, headerLeftButton = null, headerRightButton = null;
		ArrayList<Component> footerComponents = new ArrayList<Component>();
		ArrayList<Component> contentComponents = new ArrayList<Component>();

		Component component;
		BaseComponent.MobileProperties mobileProperties;

		for (int i = 0; i < formComponents.length(); i++)
		{
			component = formComponents.get(i);
			mobileProperties = component.getMobileProperties();
			if (mobileProperties != null)
			{
				if (mobileProperties.isHeaderText())
				{
					headerLabel = component;
					continue;
				}
				else if (mobileProperties.isHeaderLeftButton())
				{
					headerLeftButton = component;
					continue;
				}
				else if (mobileProperties.isHeaderRightButton())
				{
					headerRightButton = component;
					continue;
				}
				else if (mobileProperties.isFooterItem())
				{
					footerComponents.add(component);
					continue;
				}
			}
			contentComponents.add(component);

		}

		JQMHeader componentHeader = createHeader(headerLabel, headerLeftButton, headerRightButton);
		if (componentHeader != null) add(componentHeader);
		createContent(contentComponents);
		JQMFooter componentFooter = createFooter(footerComponents);
		if (componentFooter != null) add(componentFooter);
	}

	public JQMHeader createHeader(Component label, Component leftButton, Component rightButton)
	{
		String text = null;
		JQMToolBarButton leftToolbarButton = createHeaderButton(leftButton);
		JQMToolBarButton rightToolbarButton = createHeaderButton(rightButton);

		if (label != null)
		{
			GraphicalComponent gc = label.isGraphicalComponent();
			if (gc != null) text = gc.getText();
		}

		JQMHeader headerComponent = null;
		if (text != null || leftToolbarButton != null || rightToolbarButton != null)
		{
			if (text == null) text = ""; //$NON-NLS-1$
			headerComponent = new JQMHeader(text);

			if (leftToolbarButton != null)
			{
				headerComponent.setLeftButton(leftToolbarButton);
			}
			if (rightToolbarButton != null)
			{
				headerComponent.setRightButton(rightToolbarButton);
			}
		}
		return headerComponent;
	}

	private JQMToolBarButton createHeaderButton(Component component)
	{
		JQMToolBarButton headerButton = null;

		if (component != null)
		{
			GraphicalComponent gc = component.isGraphicalComponent();
			if (gc != null)
			{
				headerButton = new JQMToolBarButton(gc.getText());
				final String actionMethod = gc.getActionMethodID();
				final JQMToolBarButton button = headerButton;
				if (actionMethod != null)
				{
					headerButton.addClickHandler(new ClickHandler()
					{
						@Override
						public void onClick(ClickEvent event)
						{
							executor.fireEventCommand(JSEvent.ACTION, actionMethod, button, null);
						}
					});
				}
			}
		}

		return headerButton;
	}

	public void createContent(ArrayList<Component> contentComponents)
	{
		Solution solutionModel = application.getSolution();
		Collections.sort(contentComponents, PositionComparator.YX_COMPARATOR);
		HashMap<String, String> fieldsetLabel = new HashMap<String, String>();
		HashMap<String, DataTextField> fieldsetField = new HashMap<String, DataTextField>();
		GraphicalComponent gc;
		Field field;
		String groupID;
		for (Component c : contentComponents)
		{
			groupID = null;
			if ((gc = c.isGraphicalComponent()) != null)
			{
				groupID = gc.getGroupID();
				if (groupID != null)
				{
					fieldsetLabel.put(groupID, gc.getText());
					continue;
				}
			}
			else if ((field = c.isField()) != null)
			{
				groupID = field.getGroupID();
			}

			Widget widget = ComponentFactory.createComponent(solutionModel, c, executor);
			if (widget != null)
			{
				if (groupID != null && widget instanceof DataTextField)
				{
					fieldsetField.put(groupID, (DataTextField)widget);
				}
				add(widget);
			}
		}

		Iterator<Entry<String, DataTextField>> fieldsetFieldIte = fieldsetField.entrySet().iterator();
		Entry<String, DataTextField> fieldsetFieldEntry;
		String label;
		while (fieldsetFieldIte.hasNext())
		{
			fieldsetFieldEntry = fieldsetFieldIte.next();
			label = fieldsetLabel.get(fieldsetFieldEntry.getKey());
			if (label != null) fieldsetFieldEntry.getValue().setText(label);
		}
	}

	public JQMFooter createFooter(ArrayList<Component> footerComponents)
	{
		if (footerComponents.size() < 1) return null;
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footerComponent = new JQMFooter();
		for (Component c : footerComponents)
			footerComponent.add(ComponentFactory.createComponent(application.getSolution(), c, executor));
		return footerComponent;
	}

	public FormScope getFormScope()
	{
		if (formScope == null)
		{
			formScope = new FormScope(application, this);
		}
		return formScope;
	}

	public String getName()
	{
		return form.getName();
	}

	/**
	 * @return
	 */
	public String getDataSource()
	{
		return form.getDataSource();
	}

	/**
	 * @return
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

}
