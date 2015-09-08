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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.sksamuel.jqm4gwt.DataIcon;
import com.sksamuel.jqm4gwt.IconPos;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.Mobile;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
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
	protected IFormDisplay formDisplay;

	private JQMHeader headerComponent;

	private FormPanel formNavigator;
	private JQMButton navigatorLeftButton, replacedLeftButton;
	private HandlerRegistration navigatorLeftButtonHandler;
	private boolean headerAddedByNavigator;

	private boolean enabled = true;
	private int scrollTop;
	private boolean isShow;

	public FormPage(IFormDisplay formDisplay)
	{
		this.formDisplay = formDisplay;
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


	@Override
	protected void onPageBeforeShow()
	{
		formDisplay.getFormController().getApplication().getFormManager().setChangingFormPage(true);
		FoundSet foundSet = formDisplay.getFormController().getFormModel();
		if (foundSet != null) formDisplay.initWithRecord(foundSet.getSelectedRecord());
		else formDisplay.initWithRecord(null);

		if (headerComponent != null)
		{
			setDocumentTitle(headerComponent.getText());
		}
		if (formNavigator != null && isTablet()) formNavigator.open();
	}

	@Override
	protected void onPageShow()
	{
		removeTransitionClass();
		formDisplay.getFormController().getApplication().getFormManager().setChangingFormPage(false);
		isShow = true;
		if (scrollTop > 0) Mobile.silentScroll(scrollTop);
		formDisplay.getFormController().executeOnShowMethod();
	}

	private native void removeTransitionClass() /*-{
		$wnd.$.mobile.pageContainer
				.removeClass("ui-mobile-viewport-transitioning");
	}-*/;

	@Override
	protected void onPageHide()
	{
		isShow = false;
		if (formDisplay.getFormController().isMarkedForCleanup()) formDisplay.getFormController().cleanup();
	}

	public boolean isShown()
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

	public void destroy()
	{
		headerComponent = null;

		if (formNavigator != null)
		{
			IFormDisplay formNavigatorView = formNavigator.getFormDisplay();
			formNavigatorView.removeDisplayPanel(formDisplay.getFormController().getName());
			formNavigator = null;
		}
		if (navigatorLeftButtonHandler != null) navigatorLeftButtonHandler.removeHandler();
		navigatorLeftButtonHandler = null;
		navigatorLeftButton = null;
		replacedLeftButton = null;

		removePanel();
		removeFromParent();
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


	@Override
	public void addNavigator(String navigatorFormName)
	{
		if (formNavigator != null)
		{
			if (formNavigator.getName().equals(navigatorFormName))
			{
				FormController fc = formDisplay.getFormController();
				fc.getApplication().getFormManager().getForm(navigatorFormName).getView().getDisplayPanel(fc.getName());
				return;
			}
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
				headerAddedByNavigator = false;
			}
			else headerComponent.setLeftButton(replacedLeftButton);
		}

		if (navigatorFormName != null)
		{
			FormController fc = formDisplay.getFormController();
			formNavigator = fc.getApplication().getFormManager().getForm(navigatorFormName).getView().getDisplayPanel(fc.getName());
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
					headerComponent = new JQMHeader("");
					headerComponent.setTheme("b");
					headerComponent.setFixed(true);
					add(headerComponent);
					headerAddedByNavigator = true;
				}
				navigatorLeftButton = headerComponent.setLeftButton("", DataIcon.BARS); //$NON-NLS-1$
				navigatorLeftButton.setIconPos(IconPos.NOTEXT);
				navigatorLeftButtonHandler = navigatorLeftButton.addTapHandler(new TapHandlerForPageSwitchWithBlur()
				{

					@Override
					public void onTapAfterBlur(TapEvent event)
					{
						formNavigator.toggle();

					}
				});
			}

			add(formNavigator);
		}
	}

	public void closeNavigator()
	{
		if (formNavigator != null && !isTablet()) formNavigator.close();
	}

	public void removeWidget(Widget w)
	{
		remove(w);
	}

	private boolean isTablet()
	{
		return getWindowWidth() >= 720;
	}

	private native int getWindowWidth() /*-{
		return $wnd.innerWidth;
	}-*/;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IFormComponent#getFormDisplay()
	 */
	@Override
	public IFormDisplay getFormDisplay()
	{
		return formDisplay;
	}
}