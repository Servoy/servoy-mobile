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
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

/**
 * Form UI
 * 
 * @author gboros
 */
public class FormPage extends JQMPage
{
	protected final MobileClient application;
	protected final Form form;
	protected final FormController formController;
	private boolean enabled = true;
	protected final DataAdapterList dal;
	private IFormPageHeaderDecorator headerDecorator;
	private IFormPageFooterDecorator footerDecorator;


	public FormPage(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;
		this.form = formController.getForm();

		dal = new DataAdapterList(application, formController);
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
		JQMButton leftToolbarButton = (JQMButton)createWidget(leftButton);
		JQMButton rightToolbarButton = (JQMButton)createWidget(rightButton);

		JQMHeader headerComponent = null;
		if (label != null)
		{
			headerComponent = (JQMHeader)createWidget(label);
		}

		if (leftToolbarButton != null || rightToolbarButton != null)
		{
			if (headerComponent == null)
			{
				headerComponent = new JQMHeader(""); //$NON-NLS-1$
				headerComponent.setTheme("b"); //$NON-NLS-1$
			}

			if (leftToolbarButton != null)
			{
				headerComponent.setLeftButton(leftToolbarButton);
			}
			if (rightToolbarButton != null)
			{
				headerComponent.setRightButton(rightToolbarButton);
			}
		}

		if (headerDecorator != null) headerDecorator.decorateHeader(headerComponent);

		return headerComponent;
	}

	public void createContent(ArrayList<Component> contentComponents)
	{
		Collections.sort(contentComponents, PositionComparator.YX_COMPARATOR);
		HashMap<String, GraphicalComponent> fieldsetLabel = new HashMap<String, GraphicalComponent>();
		HashMap<String, ISupportDataText> fieldsetField = new HashMap<String, ISupportDataText>();
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
					fieldsetLabel.put(groupID, gc);
					continue;
				}
			}
			else if ((field = c.isField()) != null)
			{
				groupID = field.getGroupID();
			}

			Widget widget = createWidget(c);
			if (widget != null)
			{
				if (groupID != null && widget instanceof ISupportDataText)
				{
					fieldsetField.put(groupID, (ISupportDataText)widget);
				}
				add(widget);
			}
		}

		Iterator<Entry<String, ISupportDataText>> fieldsetFieldIte = fieldsetField.entrySet().iterator();
		Entry<String, ISupportDataText> fieldsetFieldEntry;
		while (fieldsetFieldIte.hasNext())
		{
			fieldsetFieldEntry = fieldsetFieldIte.next();
			gc = fieldsetLabel.get(fieldsetFieldEntry.getKey());
			if (gc != null)
			{
				fieldsetFieldEntry.getValue().setDataTextComponent(gc);
				dal.addFormObject(fieldsetFieldEntry.getValue().getDataTextDisplay());
			}
		}
	}

	public JQMFooter createFooter(ArrayList<Component> footerComponents)
	{
		if (footerComponents.size() < 1) return null;
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footerComponent = new JQMFooter();
		for (Component c : footerComponents)
			footerComponent.add(createWidget(c));

		if (footerDecorator != null) footerDecorator.decorateFooter(footerComponent);

		return footerComponent;
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

	public void refreshRecord(Record r)
	{
		if (r != null) dal.setRecord(r);
	}


	@Override
	protected void onPageBeforeShow()
	{
		FoundSet foundSet = formController.getFormModel();
		if (foundSet != null) refreshRecord(foundSet.getSelectedRecord());

	}

	private Widget createWidget(Component component)
	{
		if (component == null) return null;
		Widget w = ComponentFactory.createComponent(application, component, dal, formController.getExecutor());
		if (w != null) dal.addFormObject(w);
		if (w instanceof IRuntimeComponentProvider && component.getName() != null)
		{
			IRuntimeComponent runtimeComponent = ((IRuntimeComponentProvider)w).getRuntimeComponent();
			formController.getFormScope().getElementScope().addComponent(component.getName(), runtimeComponent);
		}
		return w;
	}

	public void setHeaderDecorator(IFormPageHeaderDecorator headerDecorator)
	{
		this.headerDecorator = headerDecorator;
	}

	public void setFooterDecorator(IFormPageFooterDecorator footerDecorator)
	{
		this.footerDecorator = footerDecorator;
	}

	public DataAdapterList getDataAdapterList()
	{
		return dal;
	}
}
