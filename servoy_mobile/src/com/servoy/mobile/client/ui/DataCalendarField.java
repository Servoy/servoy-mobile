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

import java.util.Date;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.dataprocessing.IEditListener;
import com.servoy.mobile.client.dataprocessing.IEditListenerSubject;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.IRuntimeField;
import com.servoy.mobile.client.scripting.RuntimeDataCalenderField;
import com.servoy.mobile.client.util.BrowserSupport;
import com.sksamuel.jqm4gwt.form.elements.JQMText;

/**
 * Text field UI
 *
 * @author gboros
 */
public class DataCalendarField extends JQMText implements IDisplayData, ISupportTitleText, IFieldComponent, IEditListenerSubject, ISupportsPlaceholderComponent
{
	private RuntimeDataCalenderField scriptable;

	private String type;
	private String format;

	public DataCalendarField(Field field, Executor executor, MobileClient application)
	{
		this.scriptable = new RuntimeDataCalenderField(application, executor, this, field);

		String frmt = field.getFormat();
		String[] dateFrmt = new String[2];
		String[] timeFrmt = new String[2];
		if (frmt == null) frmt = "yyyy-MM-dd";
		boolean hasDate = frmt != null && (frmt.indexOf('y') != -1 || frmt.indexOf('M') != -1 || frmt.indexOf('d') != -1);
		boolean hasTime = frmt != null &&
			(frmt.indexOf('h') != -1 || frmt.indexOf('H') != -1 || frmt.indexOf('k') != -1 || frmt.indexOf('K') != -1 || frmt.indexOf('m') != -1);

		boolean disableNativeDates = application.getFoundSetManager().getUserProperty("disablenativedates") != null ||
			Window.Location.getParameter("disablenativedates") != null;
			if (hasDate && hasTime)
			{
				this.type = "datetime-local"; //$NON-NLS-1$
			if (!disableNativeDates && BrowserSupport.isSupportedType(type)) this.format = "yyyy-MM-dd'T'HH:mm"; //$NON-NLS-1$
			else
			{
				dateFrmt = convertFormatToJQMDate(frmt);
				String format = dateFrmt[0].replaceAll("%Y", "yyyy"); // year
				format = format.replaceAll("%m", "MM");
				format = format.replaceAll("%d", "dd");
				this.format = format;
			}
			}
			else if (hasTime)
			{
				this.type = "time"; //$NON-NLS-1$
			if (!disableNativeDates && BrowserSupport.isSupportedType(type)) this.format = "HH:mm"; //$NON-NLS-1$
			else
			{
				timeFrmt = convertFormatToJQMTime(frmt);
				String format = timeFrmt[0].replaceAll("%k", "HH");
				format = format.replaceAll("%l", "KK");
				format = format.replaceAll("%M", "mm");
				format = format.replaceAll("%p", "a");
				this.format = format;
			}
		}
			else
			{
				this.type = "date"; //$NON-NLS-1$
			if (!disableNativeDates && BrowserSupport.isSupportedType(type)) this.format = "yyyy-MM-dd"; //$NON-NLS-1$
			else
			{
				dateFrmt = convertFormatToJQMDate(frmt);
				String format = dateFrmt[0].replaceAll("%Y", "yyyy"); // year
				format = format.replaceAll("%m", "MM");
				format = format.replaceAll("%d", "dd");
				this.format = format;
			}
		}
		if (disableNativeDates) this.type = "text";
		
		setType(type);
		if (!BrowserSupport.isSupportedType(type))
		{
			String language = "en";
			if (application.getI18nProvider() != null && application.getI18nProvider().getLanguage() != null)
			{
				language = application.getI18nProvider().getLanguage();
			}

			String themeStr = "\"theme\":true,\"themeHeader\":\"b\",\"themeDate\":\"b\",\"themeDatePick\":\"a\",\"themeDateToday\":\"a\",\"themeButton\":\"b\",\"themeInput\":\"b\"";
			String mode = "calbox";
			String timeFormat = "";
			String dateFormat = "";
			if (hasDate && (this.format.indexOf('d') != -1))
			{
				mode = "calbox";
				dateFormat = ",\"overrideDateFieldOrder\":" + dateFrmt[1] + ",\"overrideDateFormat\":\"" + dateFrmt[0] + "\"";
			}
			else
			{
				mode = "timebox";
				timeFormat = "";
				if (this.format.indexOf('k') != -1 || this.format.indexOf('h') != -1)
				{
					timeFormat = ",\"overrideTimeFormat\":12,";
				}
				else
				{
					timeFormat = ",\"overrideTimeFormat\":24,";
				}
				timeFormat = timeFormat + "\"overrideTimeOutput\":\"" + timeFrmt[0] + "\", \"overrideTimeFieldOrder\":" + timeFrmt[1];
			}
			String modeStr = "\"mode\":\"" + mode + "\""; // datebox
			input.getElement().setAttribute("data-role", "datebox");
			input.getElement().setAttribute("data-options",
				"{" + modeStr + ",\"useFocus\": true," + themeStr + ",\"useLang\":\"" + language + "\"" + timeFormat + dateFormat + "}");
		}
	}

	public String getFormat()
	{
		return format;
	}

	/**
	 * json members values for jqm datepicker initialization params
	 *  [0] overrideDateFormat 
	 *  [1] overrideDateFieldOrder
	 * @param frmt
	 * @return 
	 */
	private String[] convertFormatToJQMDate(String frmt)
	{
		String jqmFormat = frmt.replaceAll("y+", "%Y"); // year
		jqmFormat = jqmFormat.replaceAll("m+", "%i"); // minutes
		jqmFormat = jqmFormat.replaceAll("M+", "%m"); // month
		jqmFormat = jqmFormat.replaceAll("d+", "%d"); // day
		jqmFormat = jqmFormat.replaceAll("a+", "AM"); // resolve am pm 
		String overwriteFormat = jqmFormat.replaceAll("s+", "%a"); // second
		String threeTermsRegex = ".*?(%Y|%m|%d)(.*?)(%Y|%m|%d)(.*?)(%Y|%m|%d).*";
		String twoTermsRegex = ".*?(%Y|%m|%d)(.*?)(%Y|%m|%d).*";
		String oneTermRegex = ".*?(%Y|%m|%d).*";

		int count = 0;
		int year = frmt.indexOf('y');
		int month = frmt.indexOf('M');
		int day = frmt.indexOf('d');

		if (year != -1) count++;
		if (month != -1) count++;
		if (day != -1) count++;
		String[] ret = new String[2];
		if (count == 1)
		{
			ret[0] = overwriteFormat.replaceAll(oneTermRegex, "$1");
			String[] s = overwriteFormat.replaceAll(oneTermRegex, "$1").replaceAll("%", "").toLowerCase().split("\\s");
			ret[1] = "[\"" + s[0] + "\"]";
			return ret;
		}
		else if (count == 2)
		{
			ret[0] = overwriteFormat.replaceAll(twoTermsRegex, "$1$2$3");
			String[] s = overwriteFormat.replaceAll(twoTermsRegex, "$1 $3").replaceAll("%", "").toLowerCase().split("\\s");
			ret[1] = "[\"" + s[0] + "\" , \"" + s[1] + "\"]";
			return ret;
		}
		else if (count == 3)
		{
			ret[0] = overwriteFormat.replaceAll(threeTermsRegex, "$1$2$3$4$5");
			String[] s = overwriteFormat.replaceAll(threeTermsRegex, "$1 $3 $5").replaceAll("%", "").toLowerCase().split("\\s");
			ret[1] = "[\"" + s[0] + "\" , \"" + s[1] + "\", \"" + s[2] + "\"]";
			return ret;
		}
		ret[0] = "yyyy-MM-dd";
		ret[1] = "[\"y\" , \"m\", \"d\"]";
		return ret;
	}

	/**
	 *  [0] overrideTimeOutput
	 *  [1] overrideTimeFieldOrder
	 *  
	 * @param frmt
	 * @return
	 */
	private String[] convertFormatToJQMTime(String frmt)
	{
		int time12 = frmt.indexOf('h');
		if (time12 == -1) time12 = frmt.indexOf('K');
		int amPmMarker = frmt.indexOf('a');

		String hourChar = "%k";
		if (time12 != -1) hourChar = "%l";
		String overrideFormat = frmt.replaceAll("[HkhK]+", hourChar);
		overrideFormat = overrideFormat.replaceAll("[m]+", "%M");
		//jqmFormat = jqmFormat.replaceAll("[s]+", "%s");
		overrideFormat = overrideFormat.replaceAll("[a]+", "%p"); // am pm marker

		if (amPmMarker != -1)
		{
			String threeTermsRegex = ".*?(%k|%l|%M)(.*?)(%k|%l|%M)(.*?)(%p).*";
			String[] ret = new String[2];
			ret[0] = overrideFormat.replaceAll(threeTermsRegex, "$1$2$3$4$5");
			String[] s = overrideFormat.replaceAll(threeTermsRegex, "$1 $3 $5").replaceAll("%k", "h").replaceAll("%l", "h").replaceAll("%M", "i").replaceAll(
				"%p", "a").split("\\s");
			ret[1] = "[\"" + s[0] + "\" , \"" + s[1] + "\", \"" + s[2] + "\"]";
			return ret;
		}
		else
		{
			String threeTermsRegex = ".*?(%k|%l|%M)(.*?)(%k|%l|%M)(.*?).*";
			String[] ret = new String[2];
			ret[0] = overrideFormat.replaceAll(threeTermsRegex, "$1$2$3$4");
			String[] s = overrideFormat.replaceAll(threeTermsRegex, "$1 $3").replaceAll("%k", "h").replaceAll("%l", "h").replaceAll("%M", "i").split("\\s");
			ret[1] = "[\"" + s[0] + "\" , \"" + s[1] + "\"]";
			return ret;
		}

		//	return null;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		String stringValue = getValue();
		if (stringValue != null && !stringValue.trim().equals("")) //$NON-NLS-1$
		{
			DateTimeFormat dtf = DateTimeFormat.getFormat(getFormat());
			return dtf.parse(stringValue);
		}
		return null;
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		if (data instanceof Date)
		{
			DateTimeFormat dtf = DateTimeFormat.getFormat(getFormat());
			String parsed = dtf.format((Date)data);
			setValue(parsed);
		}
		else
		{
			setValue(data != null ? data.toString() : null);
		}
	}

	private EditProvider editProvider;
	private HandlerRegistration editBlurHandler;
	private HandlerRegistration editChangeHandler;

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IEditListenerSubject#addEditListener(com.servoy.mobile.client.dataprocessing.IEditListener)
	 */
	@Override
	public void addEditListener(IEditListener editListener)
	{
		if (editProvider == null)
		{
			editProvider = new EditProvider(this);
			editProvider.addEditListener(editListener);
			editBlurHandler = addBlurHandler(editProvider);
			editChangeHandler = addChangeHandler(editProvider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptableProvider#getScriptObject()
	 */
	@Override
	public IRuntimeField getRuntimeComponent()
	{
		return scriptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#setTitleText(java.lang.String)
	 */
	@Override
	public void setTitleText(String titleText)
	{
		setText(titleText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#getTitleText()
	 */
	@Override
	public String getTitleText()
	{
		return getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportTitleText#setTitleTextVisible(boolean)
	 */
	@Override
	public void setTitleTextVisible(boolean visible)
	{
		// not supported
	}

	public void setPlaceholderText(String placeholder)
	{
		if (isOrWasAttached())
		{
			setPlaceholder(getId(), placeholder);
		}
	}

	@Override
	protected void onLoad()
	{
		super.onLoad();
		setPlaceholder(getId(), scriptable.getApplication().getI18nProvider().getI18NMessageIfPrefixed(getRuntimeComponent().getPlaceholderText()));
	}

	private native void setPlaceholder(String inputId, String placeholder) /*-{
		if (placeholder != null) {
			$wnd.$("#" + inputId).attr("placeholder", placeholder);
		} else {
			$wnd.$("#" + inputId).removeAttr("placeholder");
		}
	}-*/;

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

		if (editProvider != null)
		{
			editProvider.clean();
			editProvider = null;
		}
		if (editBlurHandler != null)
		{
			editBlurHandler.removeHandler();
			editBlurHandler = null;
		}
		if (editChangeHandler != null)
		{
			editChangeHandler.removeHandler();
			editChangeHandler = null;
		}
	}
}
