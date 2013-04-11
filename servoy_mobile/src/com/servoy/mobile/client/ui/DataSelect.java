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
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.scripting.RuntimeDataSelect;
import com.servoy.mobile.client.util.IDestroyable;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMSelect;


/**
 * Combobox/select UI
 *
 * @author gboros
 */
public class DataSelect extends JQMSelect implements IDisplayData, IFieldComponent, ISupportTitleText, IEditListenerSubject, IModificationListener,
	IDestroyable
{
	private final ValueList valuelist;
	private final RuntimeDataSelect scriptable;
	private boolean hasEmptyOption;
	private final String id;

	public DataSelect(Field field, ValueList valuelist, Executor executor, MobileClient application)
	{
		this.valuelist = valuelist;
		this.scriptable = new RuntimeDataSelect(application, executor, this, field);

		setText(field.getText());
		if (valuelist != null)
		{
			valuelist.addModificationListener(this);
			fillByValueList(true);
		}

		addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent event)
			{
				if (hasEmptyOption && getSelectedIndex() > 0)
				{
					removeOption(""); //$NON-NLS-1$
					hasEmptyOption = false;
				}
			}
		});

		id = Document.get().createUniqueId();
		getElement().setId(id);
	}

	/**
	 * @param valuelist
	 */
	private void fillByValueList(boolean addEmptyOption)
	{
		if (addEmptyOption)
		{
			addOption(""); //$NON-NLS-1$
			hasEmptyOption = true;
		}
		JsArrayString displayValues = valuelist.getDiplayValues();
		for (int i = 0; i < displayValues.length(); i++)
			addOption(displayValues.get(i));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IModificationListener#valueChanged(com.servoy.mobile.client.scripting.ModificationEvent)
	 */
	@Override
	public void valueChanged(ModificationEvent e)
	{
		clear();
		fillByValueList(true);
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		if (valuelist != null) valuelist.removeModificationListener(this);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		if (hasEmptyOption && getSelectedIndex() == 0) return null;

		if (valuelist != null)
		{
			if (valuelist.hasRealValues())
			{
				JsArrayObject objectArray = valuelist.getRealValues().cast();
				try
				{
					return objectArray.getNumberObject(getSelectedIndex());
				}
				catch (Exception e)
				{
					return objectArray.getObject(getSelectedIndex());
				}
			}
			else
			{
				return valuelist.getDiplayValues().get(getSelectedIndex());
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
			int selectedIndex = Utils.findInArray(valuelist.hasRealValues() ? valuelist.getRealValues() : valuelist.getDiplayValues(), data);
			if (selectedIndex > -1)
			{
				setSelectedIndex(selectedIndex);
				if (hasEmptyOption)
				{
					removeOption(""); //$NON-NLS-1$
					hasEmptyOption = false;
				}
			}
			else
			{
				if (!hasEmptyOption)
				{
					clear();
					fillByValueList(true);
				}
				setSelectedIndex(0);
			}
			refresh();
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
			addChangeHandler(editProvider);
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
	public void refresh()
	{
		refreshIfPresent(id);
	}

	private native void refreshIfPresent(String dataSelectId) /*-{
																var selectId = $wnd.$("#" + dataSelectId).find("select").attr("id");
																var select = $wnd.$("select#" + selectId);
																var selectEl = select.get()[0];
																if (selectEl && $wnd.$.data(selectEl, "selectmenu")) {
																select.selectmenu("refresh");
																}
																}-*/;
}
