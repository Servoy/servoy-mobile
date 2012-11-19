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
import java.util.List;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dto.ValueListDescription;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckbox;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckset;

/**
 * Checkbox set UI
 *
 * @author gboros
 */
public class DataCheckboxSet extends JQMCheckset implements IDisplayData, IFieldComponent, ISupportDataText
{
	private final Field field;
	private final ValueListDescription valuelist;
	private final Executor executor;
	private final MobileClient application;

	private final List<DataCheckboxSetItem> items = new ArrayList<DataCheckboxSetItem>();

	public DataCheckboxSet(Field field, ValueListDescription valuelist, Executor executor, MobileClient application)
	{
		this.field = field;
		this.valuelist = valuelist;
		this.executor = executor;
		this.application = application;
		if (valuelist != null)
		{
			JsArrayString displayValues = valuelist.getDiplayValues();
			JsArrayMixed realValues = valuelist.hasRealValues() ? valuelist.getRealValues() : null;
			String displayValue;
			for (int i = 0; i < displayValues.length(); i++)
			{
				displayValue = displayValues.get(i);
				items.add(new DataCheckboxSetItem(addCheck(field.getUUID() + Integer.toString(i), displayValue), realValues != null ? realValues.getString(i)
					: displayValue));
			}
		}
		else
		{
			items.add(new DataCheckboxSetItem(addCheck(field.getUUID(), field.getText()), null));
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
			StringBuilder value = new StringBuilder();
			int checksSize = items.size();
			for (int i = 0; i < checksSize; i++)
			{
				if (items.get(i).checkbox.isSelected())
				{
					if (value.length() > 0) value.append('\n');
					value.append(items.get(i).realValue);
				}
			}
			return value.toString();
		}
		else
		{
			return Integer.valueOf(items.get(0).checkbox.isSelected() ? 1 : 0);
		}
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		if (valuelist != null)
		{
			String[] newValue = data instanceof String ? ((String)data).split("\n") : null; //$NON-NLS-1$

			int itemsSize = items.size();
			DataCheckboxSetItem item;
			for (int i = 0; i < itemsSize; i++)
			{
				item = items.get(i);
				item.checkbox.setValue(Boolean.valueOf(Utils.findInArray(newValue, item.realValue) != -1));
				refreshCheckbox(item.checkbox.getId());
			}
		}
		else
		{
			items.get(0).checkbox.setValue(Boolean.valueOf((data instanceof Number && ((Number)data).intValue() > 0) ||
				(data instanceof String && "true".equals(data.toString())))); //$NON-NLS-1$
			refreshCheckbox(items.get(0).checkbox.getId());
		}
	}

	private native void refreshCheckbox(String id) /*-{
		$wnd.$("#" + id).checkboxradio('refresh');
	}-*/;

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
					executor.fireEventCommand(IJSEvent.ACTION, command, DataCheckboxSet.this, null);
				}
			});
		}
	}

	private String changeCommand;

	public void setChangeCommand(final String command)
	{
		this.changeCommand = command;
	}

	private class DataCheckboxSetItem
	{
		final JQMCheckbox checkbox;
		final Object realValue;

		DataCheckboxSetItem(JQMCheckbox checkbox, Object realValue)
		{
			this.checkbox = checkbox;
			this.realValue = realValue;
		}
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#notifyLastNewValueWasChange(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void notifyLastNewValueWasChange(Object oldVal, Object newVal)
	{
		if (changeCommand != null) executor.fireEventCommand(IJSEvent.DATACHANGE, changeCommand, DataCheckboxSet.this, null);
	}

	private DataText dataText;

	/*
	 * @see com.servoy.mobile.client.ui.ISupportDataText#setDataTextComponent(com.servoy.mobile.client.persistence.GraphicalComponent)
	 */
	@Override
	public void setDataTextComponent(GraphicalComponent component)
	{
		if (component != null) dataText = new DataText(this, component, application);
	}

	/*
	 * @see com.servoy.mobile.client.ui.ISupportDataText#getDataTextDisplay()
	 */
	@Override
	public IDisplayData getDataTextDisplay()
	{
		return dataText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#needEntireState()
	 */
	@Override
	public boolean needEntireState()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setTagResolver(com.servoy.j2db.util.ITagResolver)
	 */
	@Override
	public void setTagResolver(ITagResolver resolver)
	{
		// TODO Auto-generated method stub
	}
}
