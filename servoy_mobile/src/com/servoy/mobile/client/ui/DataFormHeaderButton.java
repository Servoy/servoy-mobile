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

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.RuntimeDataFormHeaderButton;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.DataIcon;
import com.sksamuel.jqm4gwt.button.JQMButton;

/**
 * @author gboros
 *
 */
public class DataFormHeaderButton extends JQMButton implements IDisplayData, IGraphicalComponent
{
	public static int ORIENTATION_LEFT = 0;
	public static int ORIENTATION_RIGHT = 1;

	private final int orientation;
	private RuntimeDataFormHeaderButton scriptable;

	public DataFormHeaderButton(GraphicalComponent gc, int orientation, Executor executor, MobileClient application)
	{
		super(application.getI18nProvider().getI18NMessageIfPrefixed(gc.getText() != null ? gc.getText() : "")); //$NON-NLS-1$
		MobileProperties mp = gc.getMobileProperties();
		if (mp != null)
		{
			DataIcon dataIcon = Utils.stringToDataIcon(mp.getPropertyValue(IMobileProperties.DATA_ICON));
			if (dataIcon != null) setIcon(dataIcon);
		}
		this.orientation = orientation;
		this.scriptable = new RuntimeDataFormHeaderButton(application, executor, this, gc);
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

	public int getOrientation()
	{
		return orientation;
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
	 * @see com.servoy.mobile.client.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		removeFromParent();
		scriptable.destroy();
		scriptable = null;
	}
}
