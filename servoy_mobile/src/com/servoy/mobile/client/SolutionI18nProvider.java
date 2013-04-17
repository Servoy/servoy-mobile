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

package com.servoy.mobile.client;

import com.google.gwt.core.client.GWT;
import com.servoy.base.util.I18NProvider;
import com.servoy.mobile.client.persistence.FlattenedSolution;

/**
 * @author lvostinar
 *
 */
public class SolutionI18nProvider implements I18NProvider
{

	private final FlattenedSolution solution;
	private String locale;
	protected I18NMessages messages = (I18NMessages)GWT.create(I18NMessages.class);

	public SolutionI18nProvider(FlattenedSolution solution, String locale)
	{
		this.solution = solution;
		this.locale = locale;
	}

	@Override
	public String getI18NMessage(String i18nKey)
	{
		String key = locale + "." + i18nKey; //$NON-NLS-1$
		String value = solution.getI18nValue(key);
		if (value == null)
		{
			key = "." + i18nKey; //$NON-NLS-1$
			value = solution.getI18nValue(key);
		}
		return value;
	}

	@Override
	public String getI18NMessage(String i18nKey, Object[] array)
	{
		return getI18NMessage(i18nKey);
	}

	@Override
	public String getI18NMessageIfPrefixed(String i18nKey)
	{
		if (i18nKey != null && i18nKey.startsWith("i18n:"))
		{
			return getI18NMessage(i18nKey.substring(5));
		}
		return i18nKey;
	}

	@Override
	public void setI18NMessage(String i18nKey, String value)
	{
		String key = locale + "." + i18nKey;
		solution.setI18nValue(key, value);
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public String getLocale()
	{
		return locale;
	}

	public String getI18nMessageWithFallback(String key)
	{
		String message = getI18NMessage(I18NProvider.MOBILE_KEY_PREFIX + key);
		if (message == null)
		{
			message = messages.getString(key);
		}
		return message;
	}
}
