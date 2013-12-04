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
import com.google.gwt.event.shared.HandlerRegistration;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.IModificationListener;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.ModificationEvent;
import com.servoy.mobile.client.scripting.RuntimeDataCheckboxSet;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckbox;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckset;

/**
 * Checkbox set UI
 *
 * @author gboros
 */
public class DataCheckboxSet extends JQMCheckset implements IDisplayData, IFieldComponent, ISupportTitleText, IEditListenerSubject, IModificationListener
{
	private final ValueList valuelist;

	private final List<DataCheckboxSetItem> items = new ArrayList<DataCheckboxSetItem>();
	private RuntimeDataCheckboxSet scriptable;

	public DataCheckboxSet(Field field, ValueList valuelist, Executor executor, MobileClient application)
	{
		setId();
		this.valuelist = valuelist;
		this.scriptable = new RuntimeDataCheckboxSet(application, executor, this, field);

		if (valuelist != null)
		{
			valuelist.addModificationListener(this);
			fillByValueList();
		}
		else
		{
			JQMCheckbox cb = new JQMCheckbox(field.getUUID(), field.getText());
			addCheckbox(cb);
			items.add(new DataCheckboxSetItem(cb, null));
		}
	}

	/**
	 * @param field
	 * @param valuelist
	 */
	private void fillByValueList()
	{
		JsArrayString displayValues = valuelist.getDiplayValues(scriptable.getApplication().getI18nProvider());
		JsArrayMixed realValues = valuelist.hasRealValues() ? valuelist.getRealValues() : null;
		String displayValue;
		JQMCheckbox cb;
		for (int i = 0; i < displayValues.length(); i++)
		{
			displayValue = displayValues.get(i);
			cb = new JQMCheckbox(scriptable.getComponentPersist().getUUID() + Integer.toString(i), displayValue);
			addCheckbox(cb);
			items.add(new DataCheckboxSetItem(cb, realValues != null ? realValues.getString(i) : displayValue));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IModificationListener#valueChanged(com.servoy.mobile.client.scripting.ModificationEvent)
	 */
	@Override
	public void valueChanged(ModificationEvent e)
	{
		removeChangeListeners();
		clear();
		items.clear();
		fillByValueList();
		recreate(getId());
		addChangeListeners();
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
		removeChangeListeners();
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
				if (items.get(i).checkbox.isChecked())
				{
					if (value.length() > 0) value.append('\n');
					value.append(items.get(i).realValue);
				}
			}
			return value.toString();
		}
		else
		{
			return Integer.valueOf(items.get(0).checkbox.isChecked() ? 1 : 0);
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
		var cbr = $wnd.$("#" + id);
		var cbrEl = cbr.get()[0];
		if (cbrEl && $wnd.$.data(cbrEl, "mobile-checkboxradio")) {
			$wnd.$(cbr[0]).prop("checked", cbr.attr("checked"));
			cbr.checkboxradio("refresh");
		}
	}-*/;

	private EditProvider editProvider;
	private final List<HandlerRegistration> changeHandlers = new ArrayList<HandlerRegistration>();

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
			addChangeListeners();
		}
	}

	private void addChangeListeners()
	{
		if (editProvider != null && items != null && items.size() > 0)
		{
			for (int i = 0; i < items.size(); i++)
			{
				HandlerRegistration handler = items.get(i).checkbox.addValueChangeHandler(editProvider);
				if (handler != null)
				{
					changeHandlers.add(handler);
				}
			}
		}
	}

	private void removeChangeListeners()
	{
		if (changeHandlers != null)
		{
			for (HandlerRegistration handler : changeHandlers)
			{
				handler.removeHandler();
			}
			changeHandlers.clear();
		}
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
}
