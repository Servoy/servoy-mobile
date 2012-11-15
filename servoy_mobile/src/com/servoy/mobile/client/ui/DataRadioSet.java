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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dto.ValueListDescription;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;

/**
 * Radio set UI
 *
 * @author gboros
 */
public class DataRadioSet extends JQMRadioset implements IDisplayData, IFieldComponent, ISupportDataText
{
	private static final int HORIZONTAL = 1;

	private final Field field;
	private final ValueListDescription valuelist;
	private final Executor executor;

	public DataRadioSet(Field field, ValueListDescription valuelist, Executor executor)
	{
		this.field = field;
		this.valuelist = valuelist;
		this.executor = executor;

		if (field.getMobileProperties() != null && field.getMobileProperties().getRadioStyle() == HORIZONTAL) setHorizontal();

		setText(field.getText());
		if (valuelist != null)
		{
			JsArrayString displayValues = valuelist.getDiplayValues();
			for (int i = 0; i < displayValues.length(); i++)
				addRadio(displayValues.get(i));
		}
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return field.getDataProviderID();
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

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#needEditListener()
	 */
	@Override
	public boolean needEditListener()
	{
		return true;
	}

	private EditProvider editProvider;

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#addEditListener(com.servoy.mobile.client.dataprocessing.IEditListener)
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

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, DataRadioSet.this, null);
				}
			});
		}
	}

	private String changeCommand;

	public void setChangeCommand(final String command)
	{
		this.changeCommand = command;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#notifyLastNewValueWasChange(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void notifyLastNewValueWasChange(Object oldVal, Object newVal)
	{
		if (changeCommand != null) executor.fireEventCommand(IJSEvent.DATACHANGE, changeCommand, DataRadioSet.this, null);
	}

	private DataText dataText;

	/*
	 * @see com.servoy.mobile.client.ui.ISupportDataText#setDataTextComponent(com.servoy.mobile.client.persistence.GraphicalComponent)
	 */
	@Override
	public void setDataTextComponent(GraphicalComponent component)
	{
		if (component != null) dataText = new DataText(this, component);
	}

	/*
	 * @see com.servoy.mobile.client.ui.ISupportDataText#getDataTextDisplay()
	 */
	@Override
	public IDisplayData getDataTextDisplay()
	{
		return dataText;
	}

	/* (non-Javadoc)
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#needEntireState()
	 */
	@Override
	public boolean needEntireState()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setTagResolver(com.servoy.j2db.util.ITagResolver)
	 */
	@Override
	public void setTagResolver(ITagResolver resolver)
	{
		// TODO Auto-generated method stub
	}
}
