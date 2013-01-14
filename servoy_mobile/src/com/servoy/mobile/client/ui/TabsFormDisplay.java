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

import java.util.HashMap;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.persistence.FlattenedSolution;
import com.servoy.mobile.client.persistence.Tab;
import com.servoy.mobile.client.persistence.TabPanel;
import com.sksamuel.jqm4gwt.JQMContext;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;
import com.sksamuel.jqm4gwt.toolbar.JQMNavBar;

/**
 * Form display in tab panel mode
 * 
 * @author gboros
 */
public class TabsFormDisplay extends FormDisplay implements IFormPageHeaderDecorator, IFormPageFooterDecorator
{
	private HashMap<String, FormController> tabForms;
	private String currentDisplayFormName;
	private final MobileClient application;
	private final Form form;
	private final TabPanel tabPanel;
	private NavigationBar navigationBar;

	public TabsFormDisplay(MobileClient application, Form form, TabPanel tabPanel)
	{
		this.application = application;
		this.form = form;
		this.tabPanel = tabPanel;
	}

	public void setCurrenDisplayForm(String formName)
	{
		this.currentDisplayFormName = formName;
	}

	@Override
	public FormPage getDisplayPage()
	{
		return tabForms.get(currentDisplayFormName).getPage();
	}

	private NavigationBar getNavigatonBar()
	{
		if (navigationBar == null)
		{
			navigationBar = new NavigationBar();
		}

		return navigationBar;
	}

	private FormPage getTabPage(String formID)
	{
		FormController tabFormController = tabForms.get(formID);
		if (tabFormController == null)
		{
			JsArray<Tab> tabs = tabPanel.getTabs();
			FlattenedSolution solutionModel = application.getFlattenedSolution();
			for (int i = 0; i < tabs.length(); i++)
			{
				Form tabForm = solutionModel.getFormByUUID(tabs.get(i).getContainsFormID());
				tabFormController = application.getFormManager().getForm(form.getName() + " " + tabForm.getName()); //$NON-NLS-1$
				tabFormController.getPage().setHeaderDecorator(this);
				tabFormController.getPage().setFooterDecorator(this);
				tabForms.put(formID, tabFormController);
			}
		}
		return tabFormController.getPage();
	}

	class NavigationBar extends JQMNavBar
	{
		private final HashMap<String, NavigationButton> navButtons = new HashMap<String, NavigationButton>();
		private String selectedTabFormID;
		private final ClickHandler navigationClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				NavigationButton sourceButton = (NavigationButton)event.getSource();
				selectedTabFormID = sourceButton.getFormID();
				JQMContext.changePage(getTabPage(selectedTabFormID));
			}
		};

		NavigationBar()
		{
			JsArray<Tab> tabs = tabPanel.getTabs();
			FlattenedSolution solutionModel = application.getFlattenedSolution();
			for (int i = 0; i < tabs.length(); i++)
			{
				String tabFormID = tabs.get(i).getContainsFormID();
				Form tabForm = solutionModel.getFormByUUID(tabFormID);
				NavigationButton navButton = new NavigationButton(tabForm.getName(), tabFormID);
				navButton.addClickHandler(navigationClickHandler);
				navButtons.put(tabFormID, navButton);
				add(navButton);
			}
		}

		void setSelectedTabForm(String selectedTabFormID)
		{
			this.selectedTabFormID = selectedTabFormID;
		}
	}

	class NavigationButton extends JQMButton
	{
		private final String formID;

		public NavigationButton(String text, String formID)
		{
			super(text);
			this.formID = formID;
		}

		String getFormID()
		{
			return formID;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFooterDecorator#decorateFooter(com.sksamuel.jqm4gwt.toolbar.JQMFooter)
	 */
	@Override
	public JQMFooter decorateFooter(Part footerPart, JQMFooter footer)
	{
		JQMFooter footerComponent = footer;
		if (tabPanel.getTabOrientation() == TabPanel.ORIENTATION_BOTTOM)
		{
			if (footerComponent == null) footerComponent = new JQMFooter();
			if (footerPart != null) footerComponent.setTheme(footerPart.getStyleClass());
			footerComponent.add(getNavigatonBar());
		}
		return footerComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IHeaderDecorator#decorateHeader(com.sksamuel.jqm4gwt.toolbar.JQMHeader)
	 */
	@Override
	public JQMHeader decorateHeader(Part headerPart, JQMHeader header)
	{
		JQMHeader headerComponent = header;
		if (tabPanel.getTabOrientation() == TabPanel.ORIENTATION_TOP)
		{
			if (headerComponent == null)
			{
				headerComponent = new JQMHeader(""); //$NON-NLS-1$
				if (headerPart != null) headerComponent.setTheme(headerPart.getStyleClass());
			}
			headerComponent.add(getNavigatonBar());
		}
		return headerComponent;
	}
}
