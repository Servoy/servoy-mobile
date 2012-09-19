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
import java.util.HashMap;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Solution;
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
public class TabsFormDisplay implements IFormDisplay
{
	private HashMap<String, TabFormPage> tabPages;
	private String currentDisplayFormName;
	private final MobileClient application;
	private final TabPanel tabPanel;
	private NavigationBar navigationBar;

	public TabsFormDisplay(MobileClient application, Form form, TabPanel tabPanel)
	{
		this.application = application;
		this.tabPanel = tabPanel;
	}

	public void setCurrenDisplayForm(String formName)
	{
		this.currentDisplayFormName = formName;
	}

	@Override
	public FormPage getDisplayPage()
	{
		return tabPages.get(currentDisplayFormName);
	}

	private NavigationBar getNavigatonBar()
	{
		if (navigationBar == null)
		{
			navigationBar = new NavigationBar();
		}

		return navigationBar;
	}

	private TabFormPage getTabPage(String formID)
	{
		TabFormPage tabFormPage = tabPages.get(formID);
		if (tabFormPage == null)
		{
			JsArray<Tab> tabs = tabPanel.getTabs();
			Solution solutionModel = application.getSolution();
			for (int i = 0; i < tabs.length(); i++)
			{
				Form tabForm = solutionModel.getFormByUUID(tabs.get(i).getContainsFormID());
				tabFormPage = new TabFormPage(application, tabForm);
				tabPages.put(formID, tabFormPage);
			}
		}
		return tabFormPage;
	}

	class TabFormPage extends FormPage
	{
		public TabFormPage(MobileClient application, Form form)
		{
			super(application, form);
		}

		@Override
		public JQMHeader createHeader(Component headerLabel, Component headerLeftButton, Component headerRightButton)
		{
			JQMHeader headerComponent = super.createHeader(headerLabel, headerLeftButton, headerRightButton);
			if (tabPanel.getTabOrientation() == TabPanel.ORIENTATION_TOP)
			{
				if (headerComponent == null) headerComponent = new JQMHeader(""); //$NON-NLS-1$
				headerComponent.add(getNavigatonBar());
			}
			return headerComponent;
		}

		@Override
		public JQMFooter createFooter(ArrayList<Component> footerComponents)
		{
			JQMFooter footerComponent = super.createFooter(footerComponents);
			if (tabPanel.getTabOrientation() == TabPanel.ORIENTATION_BOTTOM)
			{
				if (footerComponent == null) footerComponent = new JQMFooter();
				footerComponent.add(getNavigatonBar());
			}
			return footerComponent;
		}
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
			Solution solutionModel = application.getSolution();
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
}
