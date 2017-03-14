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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.PersistUtils;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
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
	protected FormController formController;

	protected FormPage formPage;
	protected HashMap<String, FormPanel> formPanelMap = new HashMap<String, FormPanel>(); // parentForm -> formPanel

	protected JQMHeader header;
	protected JQMFooter footer;
	protected ArrayList<Widget> content;
	protected DataAdapterList dal;
	private boolean firstTime = true;

	public FormDisplay(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;
		this.dal = new DataAdapterList(application, formController);
		createComponents();
	}

	public FormController getFormController()
	{
		return formController;
	}

	protected FormPage createDisplayPage()
	{
		return new FormPage(this);
	}

	public FormPage getDisplayPage()
	{
		if (formPage == null)
		{
			formPage = createDisplayPage();
		}
		initDisplay(formPage);
		return formPage;
	}

	protected FormPanel createDisplayPanel()
	{
		return new FormPanel(this);
	}

	public FormPanel getDisplayPanel(String parentFormName)
	{
		FormPanel formPanel = formPanelMap.get(parentFormName);
		if (formPanel == null)
		{
			formPanel = createDisplayPanel();
			formPanelMap.put(parentFormName, formPanel);
		}
		initDisplay(formPanel);
		return formPanel;
	}

	public void removeDisplayPanel(String parentFormName)
	{
		formPanelMap.remove(parentFormName);
	}

	public void cleanup()
	{
		if (currentDisplay != null) cleanDisplay(currentDisplay);
		if (formPage != null)
		{
			formPage.destroy();
			formPage = null;
		}
		formPanelMap.clear();

		header = null;
		footer = null;
		content.clear();
		dal.destroy();
		formController = null;
		dal = null;
	}

	@Override
	public void initWithRecord(Record record)
	{
		if (firstTime)
		{
			firstTime = false;
			refreshRecord(record);
		}
	}

	public void refreshRecord(Record record)
	{
		dal.setRecord(record);
	}

	private IFormComponent currentDisplay;

	private void initDisplay(IFormComponent formComponent)
	{
		if (formComponent != currentDisplay)
		{
			if (currentDisplay != null) cleanDisplay(currentDisplay);
			if (header != null) formComponent.addHeader(header);
			for (Widget w : content)
			{
				formComponent.add(w);
			}
			if (footer != null) formComponent.addFooter(footer);
			currentDisplay = formComponent;
		}
	}

	private void cleanDisplay(IFormComponent formComponent)
	{
		if (header != null) formComponent.removeHeader();
		for (Widget w : content)
		{
			formComponent.removeWidget(w);
		}
		if (footer != null) formComponent.removeFooter();
	}

	private void createComponents()
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

		header = createHeader(headerPart, headerLabel, headerLeftButton, headerRightButton);
		content = createContent(contentComponents);
		footer = createFooter(footerPart, footerComponents);
	}

	protected JQMHeader createHeader(Part headerPart, Component label, Component leftButton, Component rightButton)
	{
		JQMButton leftToolbarButton = (JQMButton)createWidget(leftButton);
		JQMButton rightToolbarButton = (JQMButton)createWidget(rightButton);

		JQMHeader headerComponent = null;
		if (label != null) headerComponent = (JQMHeader)createWidget(label);
		if (leftToolbarButton != null || rightToolbarButton != null)
		{
			if (headerComponent == null) headerComponent = new JQMHeader(""); //$NON-NLS-1$
			if (leftToolbarButton != null) headerComponent.setLeftButton(leftToolbarButton);
			if (rightToolbarButton != null) headerComponent.setRightButton(rightToolbarButton);
		}

		if (headerComponent != null)
		{
			headerComponent.setTheme(headerPart != null ? headerPart.getStyleClass() : "c");
			headerComponent.setFixed(headerPart != null ? (headerPart.getPartType() == IPartConstants.TITLE_HEADER) : true);

			if (headerDecorator != null) headerDecorator.decorateHeader(headerPart, headerComponent);
		}

		return headerComponent;
	}

	protected ArrayList<Widget> createContent(ArrayList<Component> contentComponents)
	{
		ArrayList<Widget> contentList = new ArrayList<Widget>();

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
					Widget widget = createWidget(groupRow.rightComponent);
					if (widget != null)
					{
						if (widget instanceof ISupportTitleText)
						{
							dal.addFormObject(new TitleText((ISupportTitleText)widget, rowLabel, dal, application));
						}
						contentList.add(widget);
					}
				}
			}
			else
			{
				Widget widget = createWidget(rd.component);
				if (widget != null) contentList.add(widget);
			}
		}

		return contentList;
	}

	protected JQMFooter createFooter(Part footerPart, ArrayList<Component> footerComponents)
	{
		if (footerPart == null) return null;
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footerComponent = new JQMFooter();
		if (footerPart != null)
		{
			footerComponent.setTheme(footerPart.getStyleClass());
			footerComponent.setFixed(footerPart.getPartType() == IPartConstants.TITLE_FOOTER);
		}
		for (Component c : footerComponents)
			footerComponent.add(createWidget(c));

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

	private Widget createWidget(Component component)
	{
		if (component == null) return null;
		Widget w = ComponentFactory.createComponent(application, component, dal, formController);
		if (w != null) dal.addFormObject(w);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.mobile.client.ui.IFormDisplay#isShow()
	 */
	@Override
	public boolean isShow()
	{
		if (formPage != null && formPage.isShown())
		{
			return true;
		}
		else
		{
			for (FormPanel formPanel : formPanelMap.values())
			{
				if (formPanel.isShown())
				{
					return true;
				}
			}
		}

		return false;
	}

}
