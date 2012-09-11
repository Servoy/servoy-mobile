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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.solutionmodel.JSComponent;
import com.servoy.mobile.client.solutionmodel.JSField;
import com.servoy.mobile.client.solutionmodel.JSForm;
import com.servoy.mobile.client.solutionmodel.JSGraphicalComponent;
import com.servoy.mobile.client.solutionmodel.JSItem;
import com.servoy.mobile.client.solutionmodel.JSSolutionModel;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.form.JQMFieldset;
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
	private final JSForm form;
	private final Executor executor;
	private FormScope formScope;

	public FormPage(MobileClient application, JSForm form)
	{
		this.application = application;
		this.form = form;
		this.executor = new Executor(this);

		JsArray<JSComponent> formComponents = form.getComponents();

		JSComponent headerLabel = null, headerLeftButton = null, headerRightButton = null;
		ArrayList<JSComponent> footerComponents = new ArrayList<JSComponent>();
		ArrayList<JSComponent> contentComponents = new ArrayList<JSComponent>();

		JSComponent component;
		JSItem.MobileProperties mobileProperties;

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

		add(createHeader(headerLabel, headerLeftButton, headerRightButton));
		createContent(contentComponents);
		add(createFooter(footerComponents));
	}

	public JQMHeader createHeader(JSComponent label, JSComponent leftButton, JSComponent rightButton)
	{
		String text = null;
		JQMToolBarButton leftToolbarButton = null;
		JQMToolBarButton rightToolbarButton = null;

		if (label != null)
		{
			JSGraphicalComponent gc = label.isGraphicalComponent();
			if (gc != null) text = gc.getText();
		}

		if (leftButton != null)
		{
			JSGraphicalComponent gc = leftButton.isGraphicalComponent();
			if (gc != null) leftToolbarButton = new JQMToolBarButton(gc.getText());
		}

		if (rightButton != null)
		{
			JSGraphicalComponent gc = rightButton.isGraphicalComponent();
			if (gc != null) rightToolbarButton = new JQMToolBarButton(gc.getText());
		}

		if (text == null) text = "";
		JQMHeader header = new JQMHeader(text);

		if (leftToolbarButton != null)
		{
			header.setLeftButton(leftToolbarButton);
		}
		else if (rightToolbarButton != null)
		{
			header.setLeftButton(rightToolbarButton);
		}
		return header;
	}

	public void createContent(ArrayList<JSComponent> contentComponents)
	{
		JSSolutionModel solutionModel = application.getSolutionModel();
		Collections.sort(contentComponents, PositionComparator.YX_COMPARATOR);
		HashMap<String, JQMFieldset> fieldsetMap = new HashMap<String, JQMFieldset>();
		JSGraphicalComponent gc;
		JSField field;
		String groupID;
		JQMFieldset fieldset;
		for (JSComponent c : contentComponents)
		{
			groupID = null;
			if ((gc = c.isGraphicalComponent()) != null)
			{
				groupID = gc.getGroupID();
			}
			else if ((field = c.isField()) != null)
			{
				groupID = field.getGroupID();
			}

			if (groupID != null)
			{
				fieldset = fieldsetMap.get(groupID);
				if (fieldset == null)
				{
					fieldset = new JQMFieldset();
					fieldset.setHorizontal();
					add(fieldset);
				}
				Widget widget = ComponentFactory.createComponent(solutionModel, c, executor);
				if (widget != null) fieldset.add(widget);
			}
			else
			{
				Widget widget = ComponentFactory.createComponent(solutionModel, c, executor);
				if (widget != null) add(widget);
			}
		}
	}

	public JQMFooter createFooter(ArrayList<JSComponent> footerComponents)
	{
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footer = new JQMFooter();
		for (JSComponent c : footerComponents)
			footer.add(ComponentFactory.createComponent(application.getSolutionModel(), c, executor));
		return footer;
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
}
