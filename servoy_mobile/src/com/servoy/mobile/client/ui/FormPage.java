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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.Form;
import com.sksamuel.jqm4gwt.DataIcon;
import com.sksamuel.jqm4gwt.IconPos;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.Mobile;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.toolbar.JQMFooter;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;
import com.sksamuel.jqm4gwt.toolbar.JQMPanel;

/**
 * Form page UI
 *
 * @author gboros
 */
public class FormPage extends JQMPage implements IFormComponent
{
	private final MobileClient application;
	private FormController formController;
	private final Form form;
	private DataAdapterList dal;

	private JQMHeader headerComponent;

	private FormPanel formNavigator;
	private JQMButton navigatorLeftButton, replacedLeftButton;
	private HandlerRegistration navigatorLeftButtonHandler;
	private boolean headerAddedByNavigator;

	private boolean enabled = true;
	private int scrollTop;
	private boolean isShow;

	public FormPage(MobileClient application, FormController formController)
	{
		this.application = application;
		this.formController = formController;
		form = formController.getForm();
		dal = new DataAdapterList(application, formController);
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
		dal.setRecord(r);
	}

	@Override
	protected void onPageBeforeShow()
	{
		application.getFormManager().setChangingFormPage(true);
		FoundSet foundSet = formController.getFormModel();
		if (foundSet != null) refreshRecord(foundSet.getSelectedRecord());
		else refreshRecord(null);
		if (headerComponent != null)
		{
			setDocumentTitle(headerComponent.getText());
		}
		if (formNavigator != null && isTablet()) formNavigator.open();
	}

	@Override
	protected void onPageShow()
	{
		application.getFormManager().setChangingFormPage(false);
		isShow = true;
		if (scrollTop > 0) Mobile.silentScroll(scrollTop);
		formController.executeOnShowMethod();
	}

	@Override
	protected void onPageHide()
	{
		isShow = false;
		if (formController.isMarkedForCleanup()) formController.cleanup();
	}

	public boolean isShow()
	{
		return isShow;
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

	public DataAdapterList getDataAdapterList()
	{
		return dal;
	}

	public void destroy()
	{
		headerComponent = null;

		formNavigator = null;
		if (navigatorLeftButtonHandler != null) navigatorLeftButtonHandler.removeHandler();
		navigatorLeftButtonHandler = null;
		navigatorLeftButton = null;
		replacedLeftButton = null;

		removeHeader();
		removeFooter();
		removePanel();
		removeFromParent();

		dal.destroy();
		formController = null;
		dal = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#addHeader(com.sksamuel.jqm4gwt.toolbar.JQMHeader)
	 */
	@Override
	public void addHeader(JQMHeader h)
	{
		headerComponent = h;
		add(headerComponent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#addFooter(com.sksamuel.jqm4gwt.toolbar.JQMFooter)
	 */
	@Override
	public void addFooter(JQMFooter f)
	{
		add(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#getDataAdapter()
	 */
	@Override
	public DataAdapterList getDataAdapter()
	{
		return dal;
	}

	@Override
	public void addNavigator(String navigatorFormName)
	{
		if (formNavigator != null)
		{
			if (formNavigator.getName().equals(navigatorFormName)) return;
			removePanel();
			formNavigator = null;
			if (navigatorLeftButtonHandler != null)
			{
				navigatorLeftButtonHandler.removeHandler();
				navigatorLeftButtonHandler = null;
			}
			if (navigatorLeftButton != null)
			{
				headerComponent.remove(navigatorLeftButton);
				navigatorLeftButton = null;
			}
			if (headerAddedByNavigator)
			{
				remove(headerComponent);
				headerComponent = null;
			}
			else headerComponent.setLeftButton(replacedLeftButton);
		}

		if (navigatorFormName != null)
		{
			formNavigator = application.getFormManager().getForm(navigatorFormName).getPanel(getName());
			formNavigator.setPositionFixed(true);

			if (isTablet())
			{
				formNavigator.setDismissible(false);
				formNavigator.setSwipeClose(false);
				formNavigator.setDisplay(JQMPanel.DISPLAY_PUSH);
			}
			else
			{
				if (headerComponent == null)
				{
					headerComponent = new JQMHeader();
				}
				navigatorLeftButton = headerComponent.setLeftButton("", DataIcon.BARS); //$NON-NLS-1$
				navigatorLeftButton.setIconPos(IconPos.NOTEXT);
				navigatorLeftButtonHandler = navigatorLeftButton.addClickHandler(new ClickHandler()
				{
					@Override
					public void onClick(ClickEvent event)
					{
						formNavigator.toggle();
					}
				});
			}

			add(formNavigator);
		}
	}

	private boolean isTablet()
	{
		return getWindowWidth() >= 720;
	}

	private native int getWindowWidth() /*-{
		return $wnd.innerWidth;
	}-*/;
}