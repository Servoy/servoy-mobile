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
import com.servoy.mobile.client.solutionmodel.JSComponent;
import com.servoy.mobile.client.solutionmodel.JSForm;
import com.servoy.mobile.client.solutionmodel.JSSolutionModel;
import com.servoy.mobile.client.solutionmodel.JSTab;
import com.servoy.mobile.client.solutionmodel.JSTabPanel;
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
	private JSSolutionModel solutionModel;
	private JSTabPanel tabPanel;
	private NavigationBar navigationBar;
	
	public TabsFormDisplay(JSSolutionModel solutionModel, JSForm form, JSTabPanel tabPanel)
	{
		this.solutionModel = solutionModel;
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
		if(navigationBar == null)
		{
			navigationBar = new NavigationBar();
		}
		
		return navigationBar;
	}
	
	private TabFormPage getTabPage(String formID)
	{
		TabFormPage tabFormPage = tabPages.get(formID);
		if(tabFormPage == null)
		{
			JsArray<JSTab> tabs = tabPanel.getTabs();
			JSForm tabForm;

			for(int i = 0; i < tabs.length(); i++)
			{
				tabForm = solutionModel.getForm(JSSolutionModel.FORM_SEARCH_BY_UUID, tabs.get(i).getContainsFormID());
				tabFormPage = new TabFormPage(solutionModel, tabForm);
				tabPages.put(formID, tabFormPage);
			}	
		}
		return tabFormPage;
	}
	
	class TabFormPage extends FormPage
	{
		public TabFormPage(JSSolutionModel solutionModel, JSForm form)
		{
			super(solutionModel, form);
		}
		
		@Override
		public JQMHeader createHeader(JSComponent headerLabel, JSComponent headerLeftButton, JSComponent headerRightButton)
		{
			JQMHeader header = super.createHeader(headerLabel, headerLeftButton, headerRightButton);
			if(tabPanel.getTabOrientation() == JSTabPanel.ORIENTATION_TOP)
			{
				header.add(getNavigatonBar());
			}
			return header;
		}
		
		@Override
		public JQMFooter createFooter(ArrayList<JSComponent> footerComponents)
		{
			JQMFooter footer = super.createFooter(footerComponents);
			if(tabPanel.getTabOrientation() == JSTabPanel.ORIENTATION_BOTTOM)
			{
				footer.add(getNavigatonBar());
			}
			return footer;
		}
	}
	
	class NavigationBar extends JQMNavBar
	{
		private HashMap<String, NavigationButton> navButtons = new HashMap<String, NavigationButton>();
		private String selectedTabFormID;
		private ClickHandler navigationClickHandler = new ClickHandler()
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
			JsArray<JSTab> tabs = tabPanel.getTabs();
			JSForm tabForm;
			NavigationButton navButton;
			String tabFormID;
			for(int i = 0; i < tabs.length(); i++)
			{
				tabFormID = tabs.get(i).getContainsFormID();
				tabForm = solutionModel.getForm(JSSolutionModel.FORM_SEARCH_BY_UUID, tabFormID);
				navButton = new NavigationButton(tabForm.getName(), tabFormID);
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
		private String formID;
		
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
