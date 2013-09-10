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

package com.servoy.mobile.client.scripting.solutionmodel;

import org.timepedia.exporter.client.NoExport;

import com.servoy.base.persistence.IMobileProperties.MobileProperty;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;

/**
 * @author acostescu
 */
public class JSBase
{

	private AbstractBase ab;
	private final JSSolutionModel model;
	private final JSBase parent;

	public JSBase(AbstractBase ab, JSSolutionModel model, JSBase parent)
	{
		this.ab = ab;
		this.model = model;
		this.parent = parent;
	}

	public JSBase getParent()
	{
		return parent;
	}

	/**
	 * @return true if this call resulted in an actual cloning operation, false otherwise.
	 */
	@NoExport
	protected boolean cloneIfNeeded()
	{
		boolean hasBeenClonedNow = false;
		if (parent != null)
		{
			hasBeenClonedNow = parent.cloneIfNeeded();
			if (hasBeenClonedNow || !getBase().isClone())
			{
				ab = parent.getBase().getChild(ab.getUUID());
				ab.markAsCopy();
			}
		}
		return hasBeenClonedNow;
	}

	@NoExport
	protected <T> T getMobileProperty(MobileProperty<T> property)
	{
		MobileProperties mp = getBase().getMobileProperties();
		if (mp == null)
		{
			return null;
		}
		return mp.getPropertyValue(property);
	}

	@NoExport
	protected <T> void putMobileProperty(MobileProperty<T> property, T value)
	{
		if (value == null)
		{
			MobileProperties mp = getBase().getMobileProperties();
			if (mp != null)
			{
				cloneIfNeeded();
				mp.setPropertyValue(property, value);
			}
		}
		else
		{
			cloneIfNeeded();
			getBase().getOrCreateMobileProperties().setPropertyValue(property, value);
		}
	}

	@NoExport
	protected void setBase(AbstractBase ab)
	{
		this.ab = ab;
	}

	@NoExport
	public AbstractBase getBase()
	{
		return ab;
	}

	protected JSSolutionModel getSolutionModel()
	{
		return model;
	}

	/**
	*  This is called at every setOnAction , setOnLoad ... to validate if the method assigned to the event handler
	*   is in the same form of the component/form
	*/
	@NoExport
	public void verifyEventHandler(String eventHandlerName, String componentName, JSMethod method, String formName)
	{
		if (method.getScopeName() == null && method.getParentForm() != null && !formName.equals(method.getParentForm().getName()))
		{
			String componentMessage = componentName == null ? "" : " for '" + componentName + "'";
			throw new IllegalArgumentException("Cannot set " + eventHandlerName + componentMessage + " in form " + formName +
				". The method is declared in a different form '" + (method).getParentForm().getName() + "'");
		}
	}

}
