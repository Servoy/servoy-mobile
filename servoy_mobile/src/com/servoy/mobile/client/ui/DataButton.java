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
 * Button UI
 *
 * @author gboros
 */
public class DataButton extends JQMButton implements IDisplayData, IGraphicalComponent
{
	private final GraphicalComponent gc;
	private ITagResolver tagResolver;
	private final Executor executor;
	private final MobileClient application;

	public DataButton(GraphicalComponent gc, Executor executor, MobileClient application)
	{
		super(application.getI18nProvider().getI18NMessageIfPrefixed(gc.getText() != null ? gc.getText() : ""));
		this.gc = gc;
		setTheme("b"); //$NON-NLS-1$
		MobileProperties mp = gc.getMobileProperties();
		if (mp != null)
		{
			DataIcon dataIcon = Utils.stringToDataIcon(mp.getDataIcon());
			if (dataIcon != null) setIcon(dataIcon);
		}
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
					executor.fireEventCommand(IJSEvent.ACTION, command, DataButton.this, null);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return gc.getDataProviderID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return getText();
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setTagResolver(com.servoy.j2db.util.ITagResolver)
	 */
	@Override
	public void setTagResolver(ITagResolver resolver)
	{
		tagResolver = resolver;
	}
}
