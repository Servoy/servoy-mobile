/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

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
package com.servoy.mobile.client.properties;

import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.base.util.I18NProvider;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.angular.JSON;

import jsinterop.base.JsPropertyMap;


/**
 * @author jcompagner
 *
 * This is a copy of the FormataParser from Servoy Shared!
 *
 */
@SuppressWarnings("nls")
public class FormatParser
{

	public static ParsedFormat parseFormatProperty(String formatProperty)
	{
		return parseFormatProperty(formatProperty, null, null);
	}

	public static ParsedFormat parseFormatProperty(String formatProperty, String defaultFormat)
	{
		return parseFormatProperty(formatProperty, defaultFormat, null);
	}

	public static ParsedFormat parseFormatProperty(String formatProperty, String defaultFormat, I18NProvider i18nProvider)
	{
		if (formatProperty != null && formatProperty.startsWith("{") && formatProperty.endsWith("}"))
		{
			// json
			JsPropertyMap<Object> props = JSON.parse(formatProperty);

			String uiConverterName = null;
			JsPropertyMap<String> uiConverterProperties = null;
			JsPropertyMap<String> converterInfo = props.getAsAny("converter").cast();
			if (converterInfo != null)
			{
				uiConverterName = converterInfo.get("name");
				uiConverterProperties = converterInfo.getAsAny("properties").cast();
			}

			boolean useLocalDateTime = Boolean.TRUE.equals(props.get("useLocalDateTime"));

			String formatString = (String)props.get("format");
			if (formatString == null) formatString = defaultFormat;
			if (formatString != null)
			{
				return parseFormatString(formatString, uiConverterName, uiConverterProperties, useLocalDateTime, i18nProvider);
			}

			// all in json
			boolean allUpperCase = Boolean.TRUE.equals(props.get("allUpperCase"));
			boolean allLowerCase = Boolean.TRUE.equals(props.get("allLowerCase"));
			boolean numberValidator = Boolean.TRUE.equals(props.get("numberValidator"));
			boolean raw = Boolean.TRUE.equals(props.get("raw"));
			boolean mask = Boolean.TRUE.equals(props.get("mask"));
			String editOrPlaceholder = (String)props.get("editOrPlaceholder");
			String displayFormat = (String)props.get("displayFormat");
			if (i18nProvider != null)
			{
				editOrPlaceholder = i18nProvider.getI18NMessageIfPrefixed(editOrPlaceholder);
				displayFormat = i18nProvider.getI18NMessageIfPrefixed(displayFormat);
			}
			Integer maxLength = (Integer)props.get("maxLength");
			String allowedCharacters = (String)props.get("allowedCharacters");

			return new ParsedFormat(allUpperCase, allLowerCase, numberValidator, raw, mask, editOrPlaceholder, displayFormat, maxLength, uiConverterName,
				uiConverterProperties, allowedCharacters, useLocalDateTime);
		}

		// plain format string
		return parseFormatString(formatProperty, null, null, false, i18nProvider);
	}

	/**
	 * Parsers a format string, current supported formats:
	 *
	 * numbers/integers: display, display|edit
	 * date: display, display|edit, display|mask, display|placeholder|mask
	 * text: |U , |L , |#, display, display|raw, display|placeholder, display|placeholder|raw
	 *
	 * @param format
	 */
	private static ParsedFormat parseFormatString(String fmtString, String uiConverterName, JsPropertyMap<String> uiConverterProperties,
		boolean useLocalDateTime,
		I18NProvider i18nProvider)
	{
		String formatString = fmtString == null || fmtString.length() == 0 ? null : fmtString;
		boolean allLowerCase = false;
		boolean allUpperCase = false;
		boolean numberValidator = false;
		Integer maxLength = null;
		boolean raw = false;
		boolean mask = false;

		String displayFormat = null;
		String editOrPlaceholder = null;

		if (formatString != null)
		{
			int index = formatString.indexOf("|");
			if (index == -1)
			{
				displayFormat = formatString;
			}
			else
			{
				displayFormat = formatString.substring(0, index);
				editOrPlaceholder = formatString.substring(index + 1);
				if (displayFormat.length() == 0 && (editOrPlaceholder.length() == 1 || editOrPlaceholder.startsWith("U[") ||
					editOrPlaceholder.startsWith("L[") || editOrPlaceholder.startsWith("#[")))
				{
					if (editOrPlaceholder.charAt(0) == 'U')
					{
						allUpperCase = true;
					}
					else if (editOrPlaceholder.charAt(0) == 'L')
					{
						allLowerCase = true;
					}
					else if (editOrPlaceholder.charAt(0) == '#')
					{
						numberValidator = true;
					}
					if (editOrPlaceholder.length() > 1)
					{
						maxLength = Integer.valueOf(editOrPlaceholder.substring(2, editOrPlaceholder.length() - 1));
					}
					displayFormat = null;
					editOrPlaceholder = null;
				}
				else
				{
					String ml = editOrPlaceholder;
					index = ml.indexOf("|#(");
					if (index != -1 && ml.endsWith(")"))
					{
						editOrPlaceholder = ml.substring(0, index);
						ml = ml.substring(index + 1);
					}
					if (ml.startsWith("#("))
					{
						try
						{
							maxLength = Integer.valueOf(ml.substring(2, ml.length() - 1));
							if (ml == editOrPlaceholder)
							{
								editOrPlaceholder = "";
							}
						}
						catch (Exception e)
						{
							MobileClient.log(e.getMessage());
						}
					}
					if (editOrPlaceholder.endsWith("raw"))
					{
						raw = true;
						editOrPlaceholder = trim(editOrPlaceholder.substring(0, editOrPlaceholder.length() - "raw".length()));
					}
					if (editOrPlaceholder.endsWith("mask"))
					{
						mask = true;
						editOrPlaceholder = trim(editOrPlaceholder.substring(0, editOrPlaceholder.length() - "mask".length()));
						// re test raw
						if (editOrPlaceholder.endsWith("raw"))
						{
							raw = true;
							editOrPlaceholder = trim(editOrPlaceholder.substring(0, editOrPlaceholder.length() - "raw".length()));
						}
					}
					else editOrPlaceholder = trim(editOrPlaceholder);
				}
			}
		}
		if (i18nProvider != null)
		{
			editOrPlaceholder = i18nProvider.getI18NMessageIfPrefixed(editOrPlaceholder);
			displayFormat = i18nProvider.getI18NMessageIfPrefixed(displayFormat);
		}
		return new ParsedFormat(allUpperCase, allLowerCase, numberValidator, raw, mask, editOrPlaceholder, displayFormat, maxLength, uiConverterName,
			uiConverterProperties == null ? null : uiConverterProperties, null, useLocalDateTime);
	}

	/**
	 * @param eFormat
	 * @return
	 */
	private static String trim(String eFormat)
	{
		String tmp = eFormat.trim();
		if (tmp.startsWith("|")) tmp = tmp.substring(1);
		if (tmp.endsWith("|")) tmp = tmp.substring(0, tmp.length() - 1);
		return tmp;
	}

	/**
	 * Immutable parsed format.
	 *
	 * @author rgansevles
	 *
	 */
	public static class ParsedFormat
	{
		private final boolean allUpperCase;
		private final boolean allLowerCase;
		private final boolean numberValidator;
		private final boolean raw;
		private final boolean mask;

		private final String editOrPlaceholder;
		private final String displayFormat;
		private Integer maxLength;

		private final String uiConverterName;
		private final JsPropertyMap<String> uiConverterProperties;
		private final String allowedCharacters;

		private final boolean useLocalDateTime;

		public ParsedFormat(boolean allUpperCase, boolean allLowerCase, boolean numberValidator, boolean raw, boolean mask, String editOrPlaceholder,
			String displayFormat, Integer maxLength, String uiConverterName, JsPropertyMap<String> uiConverterProperties, String allowedCharacters,
			boolean useLocalDateTime)
		{
			this.allUpperCase = allUpperCase;
			this.allLowerCase = allLowerCase;
			this.numberValidator = numberValidator;
			this.raw = raw;
			this.mask = mask;

			this.editOrPlaceholder = editOrPlaceholder == null || editOrPlaceholder.length() == 0 ? null : editOrPlaceholder;
			this.displayFormat = displayFormat == null || displayFormat.length() == 0 ? null : displayFormat;
			this.maxLength = maxLength;

			this.uiConverterName = uiConverterName;
			this.uiConverterProperties = uiConverterProperties; // constructor is private, all callers should wrap with unmodifiable map
			this.allowedCharacters = allowedCharacters == null || allowedCharacters.length() == 0 ? null : allowedCharacters;

			this.useLocalDateTime = useLocalDateTime;
		}

		public JsPropertyMap<Object> toJsObject(int uiType)
		{
			JsPropertyMap<Object> map = JsPropertyMap.of();
			String type = IColumnTypeConstants.getDisplayTypeString(uiType);

			boolean isMask = isMask();
			boolean isAllUppercase = isAllUpperCase();
			boolean isAllLowercase = isAllLowerCase();
			String placeHolder = null;
			if (getPlaceHolderString() != null) placeHolder = getPlaceHolderString();
			else if (getPlaceHolderCharacter() != 0) placeHolder = Character.toString(getPlaceHolderCharacter());
			String editMask = getEditFormat();
			if (isMask && type.equals("DATETIME"))
			{
				editMask = getDateMask();
				if (placeHolder == null) placeHolder = getDisplayFormat();
			}
			else if (getDisplayFormat() != null && type.equals("TEXT"))
			{
				isMask = true;
				editMask = getDisplayFormat();
			}
			map.set("type", type);
			map.set("isMask", Boolean.valueOf(isMask));
			map.set("isRaw", Boolean.valueOf(isRaw()));
			map.set("edit", editMask);
			map.set("placeHolder", placeHolder);
			map.set("useLocalDateTime", Boolean.valueOf(useLocalDateTime()));
			map.set("allowedCharacters", getAllowedCharacters());
			map.set("display", getDisplayFormat());
			map.set("isNumberValidator", Boolean.valueOf(isNumberValidator()));
			if (getMaxLength() != null)
			{
				map.set("maxLength", getMaxLength());
			}
			if (isAllUppercase) map.set("uppercase", Boolean.valueOf(isAllUppercase));
			else if (isAllLowercase) map.set("lowercase", Boolean.valueOf(isAllLowercase));

//			if (type.equals("NUMBER") || type.equals("INTEGER") || isNumberValidator())
//			{
//				BaseWebObject webObject = dataConverterContext.getWebObject();
//				Locale clientLocale;
//				if (webObject instanceof IContextProvider)
//				{
//					clientLocale = ((IContextProvider)webObject).getDataConverterContext().getApplication().getLocale();
//				}
//				else
//				{
//					Debug.warn("Cannot get client locale for : " + webObject.toString() + " , using system default");
//					clientLocale = Locale.getDefault();
//				}
//				DecimalFormatSymbols dfs = RoundHalfUpDecimalFormat.getDecimalFormatSymbols(clientLocale);
			//
//				// the commented out values are already available client-side in numeral.localeData(); they are taken from there now
////				map.set("decimalSeparator", String.valueOf(dfs.getDecimalSeparator()));
////				map.set("groupingSeparator", String.valueOf(dfs.getGroupingSeparator()));
////				map.set("currencySymbol", dfs.getCurrencySymbol());
			//
//				map.set("percent", String.valueOf(dfs.getPercent()));
//			}

			return map;
		}

		/**
		 * @return the maxLength
		 */
		public Integer getMaxLength()
		{
			return maxLength;
		}

		public void updateMaxLength(Integer maxLength)
		{
			this.maxLength = maxLength;
		}

		public String getDateMask()
		{
			if (!mask) return null;
			StringBuilder maskPattern = new StringBuilder(displayFormat.length());
			int counter = 0;
			while (counter < displayFormat.length())
			{
				char ch = displayFormat.charAt(counter++);
				switch (ch)
				{
					case 'y' :
					case 'M' :
					case 'w' :
					case 'W' :
					case 'D' :
					case 'd' :
					case 'F' :
					case 'H' :
					case 'k' :
					case 'K' :
					case 'h' :
					case 'm' :
					case 's' :
					case 'S' :
						maskPattern.append('#');
						break;
					case 'a' :
						maskPattern.append('?');
						break;
					default :
						maskPattern.append(ch);
				}

			}
			return maskPattern.toString();
		}

		public boolean hasEditFormat()
		{
			// if it is a mask format then the editorplaceholder is always the place holder.
			// currently we dont have display and edit (with mask) support
			return !mask && editOrPlaceholder != null && !editOrPlaceholder.equals(displayFormat);
		}

		public boolean isEmpty()
		{
			return !allUpperCase && !allLowerCase && !numberValidator && displayFormat == null && maxLength == null;
		}

		public char getPlaceHolderCharacter()
		{
			if (editOrPlaceholder != null && editOrPlaceholder.length() > 0) return editOrPlaceholder.charAt(0);
			return 0;
		}

		public String getPlaceHolderString()
		{
			if (editOrPlaceholder != null && editOrPlaceholder.length() > 1) return editOrPlaceholder;
			return null;
		}

		/**
		 * @return the displayFormat
		 */
		public String getDisplayFormat()
		{
			return displayFormat;
		}

		/**
		 * @return the editFormat
		 */
		public String getEditFormat()
		{
			return editOrPlaceholder;
		}

		/**
		 * @return the allLowerCase
		 */
		public boolean isAllLowerCase()
		{
			return allLowerCase;
		}

		/**
		 * @return the allUpperCase
		 */
		public boolean isAllUpperCase()
		{
			return allUpperCase;
		}

		/**
		 * @return the mask
		 */
		public boolean isMask()
		{
			return mask;
		}

		/**
		 * @return the numberValidator
		 */
		public boolean isNumberValidator()
		{
			return numberValidator;
		}

		/**
		 * @return the raw
		 */
		public boolean isRaw()
		{
			return raw;
		}

		public String getUIConverterName()
		{
			return uiConverterName;
		}

		public JsPropertyMap<String> getUIConverterProperties()
		{
			return uiConverterProperties;
		}

		public ParsedFormat getCopy(String newUIConverterName, JsPropertyMap<String> newUIConverterProperties)
		{
			return new ParsedFormat(this.allUpperCase, this.allLowerCase, this.numberValidator, this.raw, this.mask, this.editOrPlaceholder, this.displayFormat,
				this.maxLength, newUIConverterName, newUIConverterProperties == null ? null : newUIConverterProperties,
				allowedCharacters, useLocalDateTime);
		}

		/**
		 * @return
		 */
		public String getAllowedCharacters()
		{
			return allowedCharacters;
		}

		public boolean useLocalDateTime()
		{
			return useLocalDateTime;
		}
	}
}
