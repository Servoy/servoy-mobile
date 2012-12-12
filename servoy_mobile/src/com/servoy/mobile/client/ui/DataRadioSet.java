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
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.RuntimeDataRadioSet;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;

/**
 * Radio set UI
 *
 * @author gboros
 */
public class DataRadioSet extends JQMRadioset implements IDisplayData, IFieldComponent, ISupportDataText, IEditListenerSubject
{
	private static final int HORIZONTAL = 1;

	private final ValueList valuelist;
	private final RuntimeDataRadioSet scriptable;

	public DataRadioSet(Field field, ValueList valuelist, Executor executor, MobileClient application)
	{
		this.valuelist = valuelist;
		this.scriptable = new RuntimeDataRadioSet(application, executor, this, field);

		if (field.getMobilePropertiesCopy() != null && field.getMobilePropertiesCopy().getRadioStyle() == HORIZONTAL) setHorizontal();

		setText(field.getText());
		if (valuelist != null)
		{
			JsArrayString displayValues = valuelist.getDiplayValues();
			for (int i = 0; i < displayValues.length(); i++)
				addRadio(displayValues.get(i));
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
				int valueIdx = Utils.findInArray(valuelist.getDiplayValues(), getSelectedValue());
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
					JsArrayString stringArray = valuelist.getDiplayValues();
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
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportDataText#setDataText(java.lang.String)
	 */
	@Override
	public void setDataText(String dataText)
	{
		setText(dataText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportDataText#getDataText()
	 */
	@Override
	public String getDataText()
	{
		return getText();
	}
}
