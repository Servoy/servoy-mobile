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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.ui.Executor;
import com.servoy.mobile.client.ui.IFieldComponent;

/**
 * @author gboros
 *
 */
public class AbstractRuntimeFieldComponent extends AbstractRuntimeBaseComponent<IFieldComponent, Field> implements IRuntimeField
{
	public AbstractRuntimeFieldComponent(MobileClient application, Executor executor, IFieldComponent component, Field componentPersist)
	{
		super(application, executor, component, componentPersist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptable#needEntireState()
	 */
	@Override
	public boolean needEntireState()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptable#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return componentPersist.getDataProviderID();
	}

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			component.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, this, null);
				}
			});
		}
	}

	private String changeCommand;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeField#setChangeCommand(java.lang.String)
	 */
	@Override
	public void setChangeCommand(String command)
	{
		this.changeCommand = command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeField#notifyLastNewValueWasChange(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void notifyLastNewValueWasChange(Object oldVal, Object newVal)
	{
		if (changeCommand != null) executor.fireEventCommand(IJSEvent.DATACHANGE, changeCommand, this, null);
	}

	@Override
	@Export
	public String getName()
	{
		return componentPersist.getGroupID();
	}
}
