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
import com.google.gwt.user.client.Element;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.RuntimeDataTextArea;
import com.sksamuel.jqm4gwt.events.JQMComponentEvents;
import com.sksamuel.jqm4gwt.events.JQMHandlerRegistration;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMTextArea;

/**
 * Text area field UI
 * 
 * @author gboros
 */
public class DataTextArea extends JQMTextArea implements IDisplayData, ISupportTitleText, IFieldComponent, IEditListenerSubject, ISupportsPlaceholderComponent
{
	private RuntimeDataTextArea scriptable;

	public DataTextArea(Field field, Executor executor, MobileClient application)
	{
		this.scriptable = new RuntimeDataTextArea(application, executor, this, field);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return getValue();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		setValue(data != null ? data.toString() : ""); //$NON-NLS-1$
	}

	private EditProvider editProvider;
	private HandlerRegistration editBlurHandler;

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IEditListenerSubject#addEditListener(com.servoy.mobile.client.dataprocessing.IEditListener)
	 */
	@Override
	public void addEditListener(IEditListener editListener)
	{
		if (editProvider == null)
		{
			editProvider = new EditProvider(this);
			editProvider.addEditListener(editListener);
			editBlurHandler = addBlurHandler(editProvider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptableProvider#getScriptObject()
	 */
	@Override
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#setTitleText(java.lang.String)
	 */
	@Override
	public void setTitleText(String titleText)
	{
		setText(titleText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#getTitleText()
	 */
	@Override
	public String getTitleText()
	{
		return getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#setTitleTextVisible(boolean)
	 */
	@Override
	public void setTitleTextVisible(boolean visible)
	{
		// not supported
	}

	@Override
	public HandlerRegistration addTapHandler(TapHandler handler)
	{
		HandlerRegistration defaultRegistration = addHandler(handler, TapEvent.getType());
		return new JQMHandlerRegistration(getElement(), JQMComponentEvents.TAP_EVENT, defaultRegistration);
	}

	public void setPlaceholderText(String placeholder)
	{
		Element element = flow.getWidget(1).getElement();
		if (placeholder != null)
		{
			element.setAttribute("placeholder", placeholder); //$NON-NLS-1$
		}
		else
		{
			element.removeAttribute("placeholder"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		removeFromParent();
		scriptable.destroy();
		scriptable = null;

		if (editProvider != null)
		{
			editProvider.clean();
			editProvider = null;
		}
		if (editBlurHandler != null)
		{
			editBlurHandler.removeHandler();
			editBlurHandler = null;
		}
	}
}
