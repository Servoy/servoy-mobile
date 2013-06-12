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

package com.servoy.mobile.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.PersistUtils;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

/**
 * @author gboros
 *
 */
public class FormDisplay implements IFormDisplay
{
	protected IFormPageHeaderDecorator headerDecorator;
	protected IFormPageFooterDecorator footerDecorator;

	protected final MobileClient application;
	protected final FormController formController;

	protected FormPage formPage;
	protected HashMap<String, FormPanel> formPanelMap = new HashMap<String, FormPanel>(); // parentForm -> formPanel

	public FormDisplay(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;
	}

	public FormPage getDisplayPage()
	{
		if (formPage == null)
		{
			formPage = new FormPage(application, formController);
			initDisplay(formPage);

		}
		return formPage;
	}

	public FormPanel getDisplayPanel(String parentFormName)
	{
		FormPanel formPanel = null;
		if (formPanelMap.get(parentFormName) == null)
		{
			formPanel = new FormPanel(application, formController);
			formPanelMap.put(parentFormName, formPanel);
			initDisplay(formPanel);
		}
		return formPanel;
	}

	public void cleanup()
	{
		formPanelMap.clear();
	}

	public void refreshRecord(Record record)
	{
		if (formPage != null) formPage.refreshRecord(record);
		Iterator<FormPanel> formPanelIte = formPanelMap.values().iterator();
		while (formPanelIte.hasNext())
			formPanelIte.next().refreshRecord(record);
	}

	public void initDisplay(IFormComponent formComponent)
	{
		Form form = formController.getForm();
		JsArray<Component> formComponents = form.getComponents();

		Component headerLabel = null, headerLeftButton = null, headerRightButton = null;
		ArrayList<Component> footerComponents = new ArrayList<Component>();
		ArrayList<Component> contentComponents = new ArrayList<Component>();

		Part headerPart = null, footerPart = null;

		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Part part = Part.castIfPossible(component);
			if (part != null)
			{
				if (PersistUtils.isHeaderPart(part.getPartType()))
				{
					headerPart = part;
				}
				else if (PersistUtils.isFooterPart(part.getPartType()))
				{
					footerPart = part;
				}
			}
			else
			{
				AbstractBase.MobileProperties mobileProperties = component.getMobileProperties();
				if (mobileProperties != null)
				{
					if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_TEXT).booleanValue())
					{
						headerLabel = component;
						continue;
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_LEFT_BUTTON).booleanValue())
					{
						headerLeftButton = component;
						continue;
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.HEADER_RIGHT_BUTTON).booleanValue())
					{
						headerRightButton = component;
						continue;
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.FOOTER_ITEM).booleanValue())
					{
						footerComponents.add(component);
						continue;
					}
				}

				contentComponents.add(component);
			}
		}

		JQMHeader componentHeader = createHeader(formComponent, headerPart, headerLabel, headerLeftButton, headerRightButton);
		if (componentHeader != null) formComponent.addHeader(componentHeader);
		createContent(formComponent, contentComponents);
		JQMFooter componentFooter = createFooter(formComponent, footerPart, footerComponents);
		if (componentFooter != null) formComponent.addFooter(componentFooter);
	}

	public JQMHeader createHeader(IFormComponent formComponent, Part headerPart, Component label, Component leftButton, Component rightButton)
	{
		JQMButton leftToolbarButton = (JQMButton)createWidget(formComponent, leftButton);
		JQMButton rightToolbarButton = (JQMButton)createWidget(formComponent, rightButton);

		JQMHeader headerComponent = null;
		if (label != null) headerComponent = (JQMHeader)createWidget(formComponent, label);

		if (leftToolbarButton != null || rightToolbarButton != null)
		{
			if (headerComponent == null) headerComponent = new JQMHeader(""); //$NON-NLS-1$
			if (leftToolbarButton != null) headerComponent.setLeftButton(leftToolbarButton);
			if (rightToolbarButton != null) headerComponent.setRightButton(rightToolbarButton);
		}

		if (headerPart != null)
		{
			headerComponent.setTheme(headerPart.getStyleClass());
			headerComponent.setFixed(headerPart.getPartType() == IPartConstants.TITLE_HEADER);
		}
		if (headerDecorator != null) headerDecorator.decorateHeader(headerPart, headerComponent);

		return headerComponent;
	}

	public void createContent(IFormComponent formComponent, ArrayList<Component> contentComponents)
	{
		Collections.sort(contentComponents, PositionComparator.YX_COMPARATOR);
		ArrayList<FormDisplay.RowDisplay> rowsDisplay = new ArrayList<FormDisplay.RowDisplay>();

		String groupID;
		for (Component c : contentComponents)
		{
			groupID = c.getGroupID();

			if (groupID != null)
			{
				GroupDisplay groupRow = null;
				for (RowDisplay rd : rowsDisplay)
				{
					if (rd instanceof GroupDisplay && ((GroupDisplay)rd).groupID.equals(groupID))
					{
						groupRow = (GroupDisplay)rd;
						break;
					}
				}
				if (groupRow != null)
				{
					MobileProperties mp = c.getMobileProperties();
					if (mp != null && mp.getPropertyValue(IMobileProperties.COMPONENT_TITLE).booleanValue())
					{
						groupRow.setRightComponent(groupRow.component);
						groupRow.component = c;
					}
					else groupRow.setRightComponent(c);
				}
				else rowsDisplay.add(new GroupDisplay(groupID, c));
			}
			else
			{
				rowsDisplay.add(new RowDisplay(c));
			}
		}

		for (RowDisplay rd : rowsDisplay)
		{
			if (rd instanceof GroupDisplay)
			{
				GroupDisplay groupRow = (GroupDisplay)rd;
				GraphicalComponent rowLabel = GraphicalComponent.castIfPossible(groupRow.component);
				if (rowLabel != null)
				{
					Widget widget = createWidget(formComponent, groupRow.rightComponent);
					if (widget != null)
					{
						if (widget instanceof ISupportTitleText)
						{
							formComponent.getDataAdapter().addFormObject(
								new TitleText((ISupportTitleText)widget, rowLabel, formComponent.getDataAdapter(), application));
						}
						formComponent.add(widget);
					}
				}
			}
			else
			{
				Widget widget = createWidget(formComponent, rd.component);
				if (widget != null) formComponent.add(widget);
			}
		}
	}

	public JQMFooter createFooter(IFormComponent formComponent, Part footerPart, ArrayList<Component> footerComponents)
	{
		if (footerComponents.size() < 1) return null;
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footerComponent = new JQMFooter();
		if (footerPart != null)
		{
			footerComponent.setTheme(footerPart.getStyleClass());
			footerComponent.setFixed(footerPart.getPartType() == IPartConstants.TITLE_FOOTER);
		}
		for (Component c : footerComponents)
			footerComponent.add(createWidget(formComponent, c));

		if (footerDecorator != null) footerDecorator.decorateFooter(footerPart, footerComponent);

		return footerComponent;
	}

	public void setHeaderDecorator(IFormPageHeaderDecorator headerDecorator)
	{
		this.headerDecorator = headerDecorator;
	}

	public void setFooterDecorator(IFormPageFooterDecorator footerDecorator)
	{
		this.footerDecorator = footerDecorator;
	}

	private Widget createWidget(IFormComponent formComponent, Component component)
	{
		if (component == null) return null;
		Widget w = ComponentFactory.createComponent(application, component, formComponent.getDataAdapter(), formController);
		if (w != null) formComponent.getDataAdapter().addFormObject(w);
		if (w instanceof IRuntimeComponentProvider)
		{
			IRuntimeComponent runtimeComponent = ((IRuntimeComponentProvider)w).getRuntimeComponent();
			String runtimeComponentName = runtimeComponent.getName();
			if (runtimeComponentName != null)
			{
				formController.getFormScope().getElementScope().addComponent(runtimeComponentName, runtimeComponent);
			}
		}
		return w;
	}

	private class RowDisplay
	{
		Component component;

		RowDisplay(Component component)
		{
			this.component = component;
		}
	}

	private class GroupDisplay extends RowDisplay
	{
		String groupID;
		Component rightComponent;

		GroupDisplay(String groupID, Component leftComponent)
		{
			super(leftComponent);
			this.groupID = groupID;
		}

		void setRightComponent(Component rightComponent)
		{
			this.rightComponent = rightComponent;
		}
	}
}
