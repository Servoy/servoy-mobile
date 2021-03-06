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

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.solutionmodel.mobile.IMobileSMLabel;
import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSLabel extends JSGraphicalComponent implements IMobileSMLabel, Exportable
{
	JSLabel(GraphicalComponent gc, JSSolutionModel model, JSBase parent)
	{
		super(gc, model, parent);
	}

	@Getter
	public int getLabelSize()
	{
		return getMobileProperty(IMobileProperties.HEADER_SIZE).intValue();
	}

	@Setter
	public void setLabelSize(int size)
	{
		if (size > 0 && size < 7)
		{
			putMobileProperty(IMobileProperties.HEADER_SIZE, Integer.valueOf(size));
		}
	}

	@Override
	public JSTitle getTitle()
	{
		return new JSTitle((GraphicalComponent)JSField.getTitleForComponent(this).getBase(), getSolutionModel(), getParent());
	}
}
