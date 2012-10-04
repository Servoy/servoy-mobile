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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.JSEvent;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 * 
 * @author gboros
 */
public class DataTextField extends JQMText implements IDisplayData, ISupportDataText
{
	protected final Field field;
	protected final Executor executor;

	public DataTextField(Field field, Executor executor)
	{
		this.field = field;
		this.executor = executor;

		setActionCommand(field.getActionMethodID());
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

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			addKeyUpHandler(new KeyUpHandler()
			{

				@Override
				public void onKeyUp(KeyUpEvent event)
				{
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					{
						executor.fireEventCommand(JSEvent.ACTION, command, DataTextField.this, null);
					}
				}
			});
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
}
