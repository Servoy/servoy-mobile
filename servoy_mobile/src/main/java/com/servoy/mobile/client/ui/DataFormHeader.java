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

package com.servoy.mobile.client.ui;

import com.google.gwt.event.shared.HandlerRegistration;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.RuntimeDataFormHeader;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

/**
 * @author gboros
 *
 */
public class DataFormHeader extends JQMHeader implements IDisplayData, IGraphicalComponent
{
	private RuntimeDataFormHeader scriptable;

	/**
	 * @param text
	 */
	public DataFormHeader(GraphicalComponent gc, Executor executor, MobileClient application)
	{
		super(application.getI18nProvider().getI18NMessageIfPrefixed(gc.getText() != null ? gc.getText() : "")); //$NON-NLS-1$
		this.scriptable = new RuntimeDataFormHeader(application, executor, this, gc);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return getText();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		scriptable.setText(data);
	}

	@Override
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	/**
	 * NOT SUPPORTED!
	 */
	@Override
	public HandlerRegistration addTapHandler(TapHandler handler)
	{
		return null;
	}

	@Override
	public void destroy()
	{
		removeFromParent();
		scriptable.destroy();
		scriptable = null;
	}
}
