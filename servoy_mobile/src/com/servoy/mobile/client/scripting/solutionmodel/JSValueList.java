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

package com.servoy.mobile.client.scripting.solutionmodel;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.util.TagParser;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMValuelist;
import com.servoy.mobile.client.util.Utils;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSValueList /* extends JSBase */implements IMobileSMValuelist, Exportable // TODO ac when ValueListDescription becomes a persist, please extends JSBase
{

	private final ValueList vl;

	public JSValueList(ValueList vl)
	{
		this.vl = vl;
	}

	@Getter
	@Override
	public String getName()
	{
		return vl.getName();
	}

	public String getUUID()
	{
		return vl.getUUID();
	}

	@Getter
	@Override
	public String getCustomValues()
	{
		JsArrayString displayValues = vl.getRawDiplayValues();
		JsArrayMixed realValues = vl.getRealValues();
		String values = "";
		for (int i = 0; i < displayValues.length(); i++)
		{
			values += displayValues.get(i);
			if (realValues != null && i < realValues.length())
			{
				values += "|" + realValues.getString(i);
			}
			if (i < displayValues.length() - 1)
			{
				values += "\n";
			}
		}
		return values;
	}

	@Setter
	@Override
	public void setCustomValues(String arg)
	{
		JsArrayString displayValues = JavaScriptObject.createArray().cast();
		JsArrayMixed realValues = JavaScriptObject.createArray().cast();
		if (arg != null && arg.length() > 0)
		{
			String[] rows = TagParser.split(arg, '\n');
			for (int i = 0; i < rows.length; i++)
			{
				if (!Utils.equalObjects(rows[i], "\n"))
				{
					if (rows[i].contains("|"))
					{
						String[] values = TagParser.split(rows[i], '|');
						if (values.length == 3)
						{
							displayValues.set(displayValues.length(), values[0]);
							realValues.set(realValues.length(), values[2]);
						}
					}
					else
					{
						displayValues.set(displayValues.length(), rows[i]);
						realValues.set(realValues.length(), rows[i]);
					}
				}
			}
		}
		vl.setValues(displayValues, realValues);
	}

}
