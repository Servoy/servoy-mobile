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

package com.servoy.mobile.client.scripting;

import java.util.Date;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.servoy.base.scripting.api.IJSFoundSet;
import com.servoy.base.scripting.api.IJSRecord;
import com.servoy.base.scripting.api.IJSUtils;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.Record;

/**
 * @author jcompagner
 *
 */
@Export
public class JSUtils implements Exportable, IJSUtils
{
	private final MobileClient application;

	public JSUtils(MobileClient application)
	{
		this.application = application;
		export(ExporterUtil.wrap(this));
	}

	public String dateFormat(Date date, String format)
	{
		if (format != null && date != null)
		{
			return DateTimeFormat.getFormat(format).format(date);
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSUtils#hasRecords(com.servoy.j2db.scripting.api.IJSFoundSet)
	 */
	@Override
	public boolean hasRecords(IJSFoundSet foundset)
	{
		if (foundset != null)
		{
			return foundset.getSize() > 0;
		}
		return false;
	}

	public boolean hasRecords(FoundSet foundset)
	{
		if (foundset != null)
		{
			return foundset.getSize() > 0;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSUtils#hasRecords(com.servoy.j2db.scripting.api.IJSRecord, java.lang.String)
	 */
	@Override
	public boolean hasRecords(IJSRecord record, String relationString)
	{
		return JSDatabaseManager.hasRelatedRecords(record, relationString);
	}

	public boolean hasRecords(Record record, String relationString)
	{
		return JSDatabaseManager.hasRelatedRecords(record, relationString);
	}

	private native void export(Object object)
	/*-{
		$wnd.utils = object;
	}-*/;

	public String numberFormat(Number number, Number digits)
	{
		if (number != null)
		{
			if (digits == null)
			{
				return number.toString();
			}
			return numberFormat(number.doubleValue(), digits.doubleValue());
		}
		return ""; //$NON-NLS-1$
	}

	public String numberFormat(double number, double digits)
	{
		NumberFormat nf = NumberFormat.getDecimalFormat();
		nf.overrideFractionDigits(Double.valueOf(digits).intValue());
		return nf.format(number);
	}

	public String numberFormat(Number number, String format)
	{
		if (number != null)
		{
			if (format == null)
			{
				return number.toString();
			}
			return numberFormat(number.doubleValue(), format);
		}
		return ""; //$NON-NLS-1$
	}

	public String numberFormat(double number, String format)
	{
		if (format == null)
		{
			return Double.toString(number);
		}
		return NumberFormat.getFormat(format).format(number);
	}
}
