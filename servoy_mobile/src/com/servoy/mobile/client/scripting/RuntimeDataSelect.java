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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.ui.DataSelect;
import com.servoy.mobile.client.ui.Executor;

/**
 * @author gboros
 *
 */
public class RuntimeDataSelect extends AbstractRuntimeFieldComponent
{
	public RuntimeDataSelect(MobileClient application, Executor executor, DataSelect component, Field componentPersist)
	{
		super(application, executor, component, componentPersist);
	}

	@Override
	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			((DataSelect)component).addChangeHandler(new ChangeHandler()
			{
				@Override
				public void onChange(ChangeEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, this, null);
				}
			});
		}
	}
}
