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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.j2db.util.TagParser;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.BaseComponent.MobileProperties;
import com.servoy.mobile.client.persistence.GraphicalComponent;
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

	private final GraphicalComponent gc;
	private final int orientation;
	private final Executor executor;
	private ITagResolver tagResolver;
	private final MobileClient application;

	public DataFormHeaderButton(GraphicalComponent gc, int orientation, Executor executor, MobileClient application)
	{
		super(application.getI18nProvider().getI18NMessageIfPrefixed(gc.getText() != null ? gc.getText() : "")); //$NON-NLS-1$
		MobileProperties mp = gc.getMobileProperties();
		if (mp != null)
		{
			DataIcon dataIcon = Utils.stringToDataIcon(mp.getDataIcon());
			if (dataIcon != null) setIcon(dataIcon);
		}
		this.gc = gc;
		this.orientation = orientation;
		this.executor = executor;
		this.application = application;
	}

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, DataFormHeaderButton.this, null);
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
		return gc.getDataProviderID();
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
		String txt;
		if (gc.getDataProviderID() == null)
		{
			txt = TagParser.processTags(gc.getText(), tagResolver, application.getI18nProvider());
		}
		else
		{
			txt = data != null ? data.toString() : ""; //$NON-NLS-1$
		}

		setText(txt);
	}

	public int getOrientation()
	{
		return orientation;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#notifyLastNewValueWasChange(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void notifyLastNewValueWasChange(Object oldVal, Object newVal)
	{
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#needEntireState()
	 */
	@Override
	public boolean needEntireState()
	{
		return gc.isDisplaysTags();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setTagResolver(com.servoy.j2db.util.ITagResolver)
	 */
	@Override
	public void setTagResolver(ITagResolver resolver)
	{
		tagResolver = resolver;
	}
}
