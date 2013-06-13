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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.AbstractRuntimeFieldComponent;
import com.servoy.mobile.client.scripting.IRuntimeField;
import com.servoy.mobile.client.scripting.RuntimeDataTextField;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 *
 * @author gboros
 */
public class DataTextField extends JQMText implements IDisplayData, ISupportTitleText, IFieldComponent, IEditListenerSubject, ISupportsPlaceholderComponent
{
	private AbstractRuntimeFieldComponent scriptable;
	private NumberFormat numberFormat;
	private DateTimeFormat dateFormat;
	private final int dataproviderType;

	public DataTextField(Field field, Executor executor, MobileClient application, int dataproviderType)
	{
		this.dataproviderType = dataproviderType;
		this.scriptable = createRuntimeComponent(field, executor, application);
		if (field.getFormat() != null)
		{
			if (dataproviderType == IColumnTypeConstants.NUMBER || dataproviderType == IColumnTypeConstants.INTEGER)
			{
				numberFormat = NumberFormat.getFormat(field.getFormat());
			}
			else if (dataproviderType == IColumnTypeConstants.DATETIME)
			{
				dateFormat = DateTimeFormat.getFormat(field.getFormat());
			}
			else
			{
				Log.error("Format is set: " + field.getFormat() + ", on dataprovider: " + field.getDataProviderID() + " that has a not supported type: " + dataproviderType); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
		}
	}

	protected AbstractRuntimeFieldComponent createRuntimeComponent(Field field, Executor executor, MobileClient application)
	{
		return new RuntimeDataTextField(application, executor, this, field);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		String txt = getValue();
		if ((dataproviderType == IColumnTypeConstants.NUMBER || dataproviderType == IColumnTypeConstants.INTEGER || dataproviderType == IColumnTypeConstants.DATETIME) &&
			(txt == null || txt.trim().length() == 0))
		{
			return null;
		}
		if (numberFormat != null)
		{
			Double value = Double.valueOf(numberFormat.parse(txt));
			setValueObject(value);
			return value;
		}
		else if (dateFormat != null)
		{
			Date value = dateFormat.parse(txt);
			setValueObject(value);
			return value;
		}
		return txt;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		String txt;
		if (data != null && numberFormat != null)
		{
			txt = numberFormat.format((Number)data);
		}
		else if (data != null && dateFormat != null)
		{
			txt = dateFormat.format((Date)data);
		}
		else
		{
			txt = data != null ? data.toString() : ""; //$NON-NLS-1$
		}
		if (!Utils.equalObjects(txt, getValue())) setValue(txt);
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
	}

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