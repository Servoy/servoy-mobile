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

import com.servoy.base.util.ITagResolver;
import com.servoy.base.util.TagParser;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * @author gboros
 *
 */
public class TitleText implements IDisplayData
{
	private final ISupportTitleText parentComponent;
	private final GraphicalComponent textComponent;
	private final ITagResolver tagResolver;
	private final MobileClient application;

	public TitleText(ISupportTitleText parentComponent, GraphicalComponent textComponent, ITagResolver tagResolver, MobileClient application)
	{
		this.parentComponent = parentComponent;
		this.textComponent = textComponent;
		this.tagResolver = tagResolver;
		this.application = application;

		parentComponent.setTitleText(textComponent.getVisible() ? application.getI18nProvider().getI18NMessageIfPrefixed(
			textComponent.getText() != null ? textComponent.getText() : "") : ""); //$NON-NLS-1$ //$NON-NLS-2$);
		if (parentComponent instanceof DataLabel) parentComponent.setTitleTextVisible(textComponent.getVisible());
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return parentComponent.getTitleText();
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
			txt = TagParser.processTags(application.getI18nProvider().getI18NMessageIfPrefixed(textComponent.getText()), tagResolver,
				application.getI18nProvider());
		}
		else
		{
			txt = data != null ? data.toString() : ""; //$NON-NLS-1$
		}

		parentComponent.setTitleText(txt);
	}

	public String getDataProviderID()
	{
		return textComponent.getDataProviderID();
	}

	public boolean needEntireState()
	{
		return textComponent.getDisplaysTags();
	}
}