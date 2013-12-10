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

import org.timepedia.exporter.client.ExporterBaseActual.JsArrayObject;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.HandlerRegistration;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.scripting.RuntimeDataRadioSet;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;

/**
 * Radio set UI
 *
 * @author gboros
 */
public class DataRadioSet extends JQMRadioset implements IDisplayData, IFieldComponent, ISupportTitleText, IEditListenerSubject, IModificationListener
{
	private final ValueList valuelist;
	private RuntimeDataRadioSet scriptable;

	public DataRadioSet(Field field, ValueList valuelist, Executor executor, MobileClient application)
	{
		this.valuelist = valuelist;
		this.scriptable = new RuntimeDataRadioSet(application, executor, this, field);

		MobileProperties mp = field.getMobileProperties();
		// mp.getPropertyValue() returns Double
		if (mp != null && mp.getPropertyValue(IMobileProperties.RADIO_STYLE).intValue() == IMobileProperties.RADIO_STYLE_HORIZONTAL.intValue()) setHorizontal();

		setText(field.getText());
		if (valuelist != null)
		{
			valuelist.addModificationListener(this);
			fillByValueList();
		}
	}

	/**
	 * @param valuelist
	 */
	public void fillByValueList()
	{
		JsArrayString displayValues = valuelist.getDiplayValues(scriptable.getApplication().getI18nProvider());
		for (int i = 0; i < displayValues.length(); i++)
			addRadio(displayValues.get(i));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IModificationListener#valueChanged(com.servoy.mobile.client.scripting.ModificationEvent)
	 */
	@Override
	public void valueChanged(ModificationEvent e)
	{
		if (editBlurHandler != null) editBlurHandler.removeHandler();
		clear();
		fillByValueList();
		recreate(getId());
		if (editProvider != null) editBlurHandler = addBlurHandler(editProvider);
	}

	private native void recreate(String id) /*-{
		$wnd.$("#" + id).trigger("create");
	}-*/;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		removeFromParent();
		scriptable.destroy();
		scriptable = null;
		if (valuelist != null) valuelist.removeModificationListener(this);
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

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		if (valuelist != null)
		{
			if (valuelist.hasRealValues())
			{
				int valueIdx = Utils.findInArray(valuelist.getDiplayValues(scriptable.getApplication().getI18nProvider()), getSelectedValue());
				if (valueIdx > -1)
				{
					JsArrayObject objectArray = valuelist.getRealValues().cast();
					return objectArray.getObject(valueIdx);
				}
			}
			else
			{
				return getSelectedValue();
			}
		}
		return null;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		if (valuelist != null)
		{
			String selectedValue = null;

			if (valuelist.hasRealValues())
			{
				int valueIdx = Utils.findInArray(valuelist.getRealValues(), data);
				if (valueIdx > -1)
				{
					JsArrayString stringArray = valuelist.getDiplayValues(scriptable.getApplication().getI18nProvider());
					selectedValue = stringArray.get(valueIdx);
				}
			}
			else
			{
				selectedValue = data.toString();
			}

			if (selectedValue != null)
			{
				setSelectedValue(selectedValue);
				refresh();
			}
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
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	@Override
	public void setText(String text)
	{
		super.setText(text);
		setTitleTextVisible(text != null && text.length() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportDataText#setTitleText(java.lang.String)
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
		setLabelHidden(!visible);
	}

	public void refresh()
	{
		refreshIfPresent(getId());
	}

	private native void refreshIfPresent(String dataRadioSetId) /*-{
		$wnd.$("#" + dataRadioSetId).find("input[type='radio']")
				.each(
						function() {
							var radio = $wnd.$("input#"
									+ $wnd.$(this).attr("id"));
							var radioEl = radio.get()[0];
							if (radioEl
									&& $wnd.$.data(radioEl,
											"mobile-checkboxradio")) {
								$wnd.$(radio[0]).prop("checked",
										radio.attr("checked"));
								radio.checkboxradio("refresh");
							}
						});
	}-*/;
}
