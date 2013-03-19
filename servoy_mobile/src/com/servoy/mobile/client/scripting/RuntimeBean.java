/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Bean;
import com.servoy.mobile.client.ui.BeanComponent;
import com.servoy.mobile.client.ui.Executor;

/**
 * @author gboros
 *
 */
public class RuntimeBean extends AbstractRuntimeBaseComponent<BeanComponent, Bean>
{
	public RuntimeBean(MobileClient application, Executor executor, BeanComponent component, Bean componentPersist)
	{
		super(application, executor, component, componentPersist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeComponent#needEntireState()
	 */
	@Override
	public boolean needEntireState()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeComponent#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeComponent#setActionCommand(java.lang.String)
	 */
	@Override
	public void setActionCommand(String command)
	{
		// handled by the bean itself
	}

	@Getter
	public String getText()
	{
		return component.getText();
	}

	@Setter
	public void setText(String text)
	{
		component.setText(text);
	}

}
