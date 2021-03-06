/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.solutionmodel.mobile.IMobileSMRadios;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMField;
import com.servoy.mobile.client.util.Utils;

/**
 * Solution model radios field component.
 * 
 * @author rgansevles
 */
@Export
@ExportPackage("")
public class JSRadios extends JSField implements IMobileSMField, IMobileSMRadios, Exportable
{
	JSRadios(Field f, JSSolutionModel model, JSBase parent)
	{
		super(f, model, parent);
	}

	@Getter
	@Override
	public boolean getHorizontal()
	{
		// getMobileProperty may return a Double
		return Utils.equalObjects(IMobileProperties.RADIO_STYLE_HORIZONTAL, getMobileProperty(IMobileProperties.RADIO_STYLE));
	}

	@Setter
	@Override
	public void setHorizontal(boolean horizontal)
	{
		putMobileProperty(IMobileProperties.RADIO_STYLE, horizontal ? IMobileProperties.RADIO_STYLE_HORIZONTAL : null);
	}
}
