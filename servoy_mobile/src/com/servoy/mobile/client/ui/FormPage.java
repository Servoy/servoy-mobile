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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.Mobile;
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
	private JQMHeader headerComponent;
	private int scrollTop;

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

		Part headerPart = null, footerPart = null;

		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Part part = Part.castIfPossible(component);
			if (part != null)
			{
				if (part.getType() == IPartConstants.HEADER)
				{
					headerPart = part;
				}
				else if (part.getType() == IPartConstants.FOOTER)
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

		JQMHeader componentHeader = createHeader(headerPart, headerLabel, headerLeftButton, headerRightButton);
		if (componentHeader != null) add(componentHeader);
		createContent(contentComponents);
		JQMFooter componentFooter = createFooter(footerPart, footerComponents);
		if (componentFooter != null) add(componentFooter);
	}

	public JQMHeader createHeader(Part headerPart, Component label, Component leftButton, Component rightButton)
	{
		JQMButton leftToolbarButton = (JQMButton)createWidget(leftButton);
		JQMButton rightToolbarButton = (JQMButton)createWidget(rightButton);

		headerComponent = null;
		if (label != null) headerComponent = (JQMHeader)createWidget(label);

		if (leftToolbarButton != null || rightToolbarButton != null)
		{
			if (headerComponent == null) headerComponent = new JQMHeader(""); //$NON-NLS-1$
			if (leftToolbarButton != null) headerComponent.setLeftButton(leftToolbarButton);
			if (rightToolbarButton != null) headerComponent.setRightButton(rightToolbarButton);
		}

		if (headerPart != null) headerComponent.setTheme(headerPart.getStyleClass());
		if (headerDecorator != null) headerDecorator.decorateHeader(headerPart, headerComponent);

		return headerComponent;
	}

	public void createContent(ArrayList<Component> contentComponents)
	{
		Collections.sort(contentComponents, PositionComparator.YX_COMPARATOR);
		ArrayList<FormPage.RowDisplay> rowsDisplay = new ArrayList<FormPage.RowDisplay>();

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
						add(widget);
					}
				}
			}
			else
			{
				Widget widget = createWidget(rd.component);
				if (widget != null) add(widget);
			}
		}
	}

	public JQMFooter createFooter(Part footerPart, ArrayList<Component> footerComponents)
	{
		if (footerComponents.size() < 1) return null;
		Collections.sort(footerComponents, PositionComparator.XY_COMPARATOR);
		JQMFooter footerComponent = new JQMFooter();
		if (footerPart != null) footerComponent.setTheme(footerPart.getStyleClass());
		for (Component c : footerComponents)
			footerComponent.add(createWidget(c));

		if (footerDecorator != null) footerDecorator.decorateFooter(footerPart, footerComponent);

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
		if (headerComponent != null)
		{
			setDocumentTitle(headerComponent.getText());
		}
	}

	@Override
	protected void onPageShow()
	{
		if (scrollTop > 0) Mobile.silentScroll(scrollTop);
		formController.executeOnShowMethod();
	}

	@Override
	protected void onPageHide()
	{
		if (formController.isMarkedForCleanup()) formController.cleanup();
	}

	public void clearScrollTop()
	{
		scrollTop = 0;
	}

	public void saveScrollTop()
	{
		scrollTop = getBodyScrollTop();
	}

	private static native int getBodyScrollTop() /*-{
		return $wnd.$("body").scrollTop();
	}-*/;

	private native void setDocumentTitle(String text)
	/*-{
		top.document.title = text;
	}-*/;

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
