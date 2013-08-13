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
import org.timepedia.exporter.client.NoExport;
import org.timepedia.exporter.client.Setter;

import com.servoy.base.persistence.PersistUtils;
import com.servoy.mobile.client.persistence.Part;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMPart;

/**
 * @author lvostinar
 *
 */
@Export
@ExportPackage("")
public class JSPart extends JSBase implements IMobileSMPart, Exportable
{
	JSPart(Part part, JSSolutionModel model, JSForm parent)
	{
		super(part, model, parent);
		setStyleClass("b");
	}

	@Override
	@NoExport
	public Part getBase()
	{
		return (Part)super.getBase();
	}

	@NoExport
	public static JSPart createPart(Part part, JSSolutionModel model, JSForm parent)
	{
		if (part == null)
		{
			return null;
		}
		if (PersistUtils.isHeaderPart(part.getPartType()))
		{
			return new JSHeader(part, model, parent);
		}
		if (PersistUtils.isFooterPart(part.getPartType()))
		{
			return new JSFooter(part, model, parent);
		}

		return new JSPart(part, model, parent);
	}

	@Getter
	@Override
	public String getStyleClass()
	{
		return getBase().getStyleClass();
	}

	@Setter
	@Override
	public void setStyleClass(String styleClass)
	{
		cloneIfNeeded();
		getBase().setStyleClass(styleClass);
	}
}
