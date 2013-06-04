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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.IRuntimeField;
import com.servoy.mobile.client.scripting.RuntimeDataCalenderField;
import com.servoy.mobile.client.util.BrowserSupport;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 *
 * @author gboros
 */
public class DataCalendarField extends JQMText implements IDisplayData, ISupportTitleText, IFieldComponent, IEditListenerSubject, ISupportsPlaceholderComponent
{
	private RuntimeDataCalenderField scriptable;

	private final String type;
	private final String format;

	public DataCalendarField(Field field, Executor executor, MobileClient application)
	{
		this.scriptable = new RuntimeDataCalenderField(application, executor, this, field);

		String frmt = field.getFormat();
		if (frmt == null)
		{
			// if no format is given, set the type just date and get the format based on if the native dates are used or not. 
			this.type = "date"; //$NON-NLS-1$
			if (BrowserSupport.isSupportedType(type))
			{
				this.format = "yyyy-MM-dd"; //$NON-NLS-1$
			}
			else
			{
				// no native date then just get the default date_medium pattern.
				this.format = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).getPattern();
			}
		}
		else
		{
			boolean hasDate = frmt.indexOf('y') != -1 || frmt.indexOf('M') != -1 || frmt.indexOf('d') != -1;
			boolean hasTime = frmt.indexOf('h') != -1 || frmt.indexOf('H') != -1 || frmt.indexOf('k') != -1 || frmt.indexOf('K') != -1 ||
				frmt.indexOf('m') != -1;
			if (hasDate && hasTime)
			{
				this.type = "datetime-local"; //$NON-NLS-1$
				if (BrowserSupport.isSupportedType(type)) this.format = "yyyy-MM-dd'T'HH:mm"; //$NON-NLS-1$
				else this.format = frmt;
			}
			else if (hasTime)
			{
				this.type = "time"; //$NON-NLS-1$
				if (BrowserSupport.isSupportedType(type)) this.format = "HH:mm"; //$NON-NLS-1$
				else this.format = frmt;
			}
			else
			{
				this.type = "date"; //$NON-NLS-1$
				if (BrowserSupport.isSupportedType(type)) this.format = "yyyy-MM-dd"; //$NON-NLS-1$
				else this.format = frmt;
			}

		}
		setType(type);
	}

	public String getFormat()
	{
		return format;
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
			DateTimeFormat dtf = DateTimeFormat.getFormat(getFormat());
			return dtf.parse(stringValue);
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
			DateTimeFormat dtf = DateTimeFormat.getFormat(getFormat());
			String parsed = dtf.format((Date)data);
			setValue(parsed);
			if (!BrowserSupport.isSupportedType(type))
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
		if (!BrowserSupport.isSupportedType(type))
		{
			init(getId(), getFormat().toLowerCase()); // java format maps through lowercase
		}
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
