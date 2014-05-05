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

import com.google.gwt.core.client.JsArrayString;
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

	public SolutionI18nProvider(FlattenedSolution solution, String locale)
	{
		this.solution = solution;
		this.locale = locale;
	}

	@Override
	public String getI18NMessage(String i18nKey)
	{
		return getI18NMessage(i18nKey, null);
	}

	@Override
	public String getI18NMessage(String i18nKey, Object[] array)
	{
		String key = locale + "." + i18nKey; //$NON-NLS-1$
		String value = solution.getI18nValue(key);
		if (value == null)
		{
			// fallback to language only if it is defined . ex:  locale is de_AT , but only 'de' i18n key is defined
			String[] languageAndCountry = locale.split("_");
			if (languageAndCountry.length > 1)
			{
				key = languageAndCountry[0] + '.' + i18nKey;
				value = solution.getI18nValue(key);

			}
			if (value == null)
			{
				key = "." + i18nKey; //$NON-NLS-1$
				value = solution.getI18nValue(key);
			}

		}
		if (value == null)
		{
			return '!' + i18nKey + '!';
		}
		return format(value, array);
	}

	public String format(final String format, final Object[] args)
	{
		if (null == args || 0 == args.length) return format;
		JsArrayString array = newArray();
		for (Object arg : args)
		{
			array.push(String.valueOf(arg));
		}
		return nativeFormat(format, array);
	}

	private static native JsArrayString newArray()/*-{
		return [];
	}-*/;

	private static native String nativeFormat(final String format, final JsArrayString args)/*-{
		return format.replace(/{(\d+)}/g, function(match, number) {
			return typeof args[number] != 'undefined' ? args[number] : match;
		});
	}-*/;

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

	public void setLocale(String locale, String country)
	{
		if (locale != null)
		{
			if (country != null)
			{
				this.locale = locale + '_' + country;
			}
			else
			{
				this.locale = locale;
			}
		}
	}

	public String getLocale()
	{
		return locale;
	}

	public String getI18nMessageWithFallback(String key)
	{
		// just look the key among exported ones, all keys are now exported
		return getI18NMessage(I18NProvider.MOBILE_KEY_PREFIX + key);
	}

	public String getLanguage()
	{
		if (locale != null)
		{
			if (locale.contains("_"))
			{
				return locale.split("_")[0];
			}
			else
			{
				return locale;
			}
		}
		return null;
	}
}
