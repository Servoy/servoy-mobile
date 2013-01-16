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
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.ui.Executor;
import com.servoy.mobile.client.ui.IComponent;
import com.servoy.mobile.client.ui.runtime.HasRuntimeEnabled;
import com.servoy.mobile.client.ui.runtime.HasRuntimeName;
import com.servoy.mobile.client.ui.runtime.HasRuntimeVisible;

/**
 * @author gboros
 *
 */
public abstract class AbstractRuntimeBaseComponent<C extends IComponent, P extends Component> implements IRuntimeComponent, HasRuntimeName, HasRuntimeEnabled,
	HasRuntimeVisible, Exportable
{
	protected final MobileClient application;
	protected final Executor executor;
	protected final C component;
	protected final P componentPersist;


	public AbstractRuntimeBaseComponent(MobileClient application, Executor executor, C component, P componentPersist)
	{
		this.application = application;
		this.executor = executor;
		this.component = component;
		this.componentPersist = componentPersist;
	}

	/**
	 * @return the component
	 */
	public C getComponent()
	{
		return component;
	}

	/**
	 * @return the component persist
	 */
	public P getComponentPersist()
	{
		return componentPersist;
	}

	@Override
	@Export
	public String getName()
	{
		return componentPersist.getName();
	}

	@Override
	@Getter
	public boolean isEnabled()
	{
		return component.isEnabled();
	}

	@Override
	@Setter
	public void setEnabled(boolean b)
	{
		component.setEnabled(b);
	}

	@Override
	@Getter
	public boolean isVisible()
	{
		return component.isVisible();
	}

	@Override
	@Setter
	public void setVisible(boolean b)
	{
		component.setVisible(b);
	}
}
