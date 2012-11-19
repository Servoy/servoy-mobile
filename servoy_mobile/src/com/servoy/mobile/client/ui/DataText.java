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

import com.google.gwt.user.client.ui.HasText;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.j2db.util.TagParser;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * @author gboros
 *
 */
public class DataText implements IDisplayData
{
	private final GraphicalComponent textComponent;
	private final HasText parentComponent;
	private ITagResolver tagResolver;
	private final MobileClient application;

	public DataText(HasText parentComponent, GraphicalComponent textComponent, MobileClient application)
	{
		this.parentComponent = parentComponent;
		this.textComponent = textComponent;
		this.application = application;
		parentComponent.setText(application.getI18nProvider().getI18NMessageIfPrefixed(textComponent.getText() != null ? textComponent.getText() : ""));
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return textComponent.getDataProviderID();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return textComponent.getText();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		String txt;
		if (textComponent.getDataProviderID() == null)
		{
			txt = TagParser.processTags(textComponent.getText(), tagResolver, application.getI18nProvider());
		}
		else
		{
			txt = data != null ? data.toString() : ""; //$NON-NLS-1$
		}

		parentComponent.setText(txt);
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
		return textComponent.isDisplaysTags();
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
