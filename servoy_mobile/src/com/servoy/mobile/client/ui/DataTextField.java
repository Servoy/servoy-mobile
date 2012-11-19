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
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 *
 * @author gboros
 */
public class DataTextField extends JQMText implements IDisplayData, ISupportDataText, IFieldComponent, IEditListenerSubject
{
	protected final Field field;
	protected final Executor executor;
	private final MobileClient application;

	public DataTextField(Field field, Executor executor, MobileClient application)
	{
		this.field = field;
		this.executor = executor;
		this.application = application;
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
						executor.fireEventCommand(IJSEvent.ACTION, command, DataTextField.this, null);
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
		if (changeCommand != null) executor.fireEventCommand(IJSEvent.DATACHANGE, changeCommand, DataTextField.this, null);
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
