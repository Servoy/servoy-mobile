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

import java.util.Date;

import com.google.gwt.event.dom.client.TouchEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.IRuntimeField;
import com.servoy.mobile.client.scripting.RuntimeDataCalenderField;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 *
 * @author gboros
 */
public class DataCalendarField extends JQMText implements IDisplayData, ISupportTitleText, IFieldComponent, IEditListenerSubject, ISupportsPlaceholderComponent
{
	private RuntimeDataCalenderField scriptable;

	public DataCalendarField(Field field, Executor executor, MobileClient application)
	{
		this.scriptable = new RuntimeDataCalenderField(application, executor, this, field);
		setType("date");
	}

	public String getFormat()
	{
		if (!supportsNativeDate())
		{
			return "dd-MM-yyyy";
		}
		else
		{
			// native date is always in this format. 
			return "yyyy-MM-dd";
		}
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		String stringValue = getValue();
		if (stringValue != null && !stringValue.trim().equals("")) //$NON-NLS-1$
		{
			DateTimeFormat format = DateTimeFormat.getFormat(getFormat());
			return format.parse(stringValue);
		}
		return null;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		if (data instanceof Date)
		{
			DateTimeFormat format = DateTimeFormat.getFormat(getFormat());
			String parsed = format.format((Date)data);
			setValue(parsed);
			if (!supportsNativeDate())
			{
				setDate(((Date)data).getTime());
			}
		}
		else
		{
			setValue(data != null ? data.toString() : null);
		}
	}

	private EditProvider editProvider;

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
			addBlurHandler(editProvider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptableProvider#getScriptObject()
	 */
	@Override
	public IRuntimeField getRuntimeComponent()
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

	public void setPlaceholderText(String placeholder)
	{
		if (isOrWasAttached())
		{
			setPlaceholder(getId(), placeholder);
		}
	}

	@Override
	protected void onLoad()
	{
		super.onLoad();
		setPlaceholder(getId(), scriptable.getApplication().getI18nProvider().getI18NMessageIfPrefixed(getRuntimeComponent().getPlaceholderText()));
		if (!supportsNativeDate())
		{
			init(getId(), getFormat().toLowerCase()); // java format maps through lowercase
		}
	}

	/**
	 * Returns if this calender field should use native date fields or not.
	 * Currently we say yes if Touch is supported.
	 * @return
	 */
	public boolean supportsNativeDate()
	{
		return TouchEvent.isSupported();
	}

	private native void init(String inputId, String format)
	/*-{
		this.picker = $wnd.$("#" + inputId).pickadate({
			format : format
		}).pickadate('picker');
	}-*/;

	private native void setDate(double date)
	/*-{
		this.picker.set("select", date)
		this.picker.set('highlight', date)
	}-*/;

	private native void setPlaceholder(String inputId, String placeholder) /*-{
		if (placeholder != null) {
			$wnd.$("#" + inputId).attr("placeholder", placeholder);
		} else {
			$wnd.$("#" + inputId).removeAttr("placeholder");
		}
	}-*/;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		scriptable.destroy();
		scriptable = null;
	}
}
