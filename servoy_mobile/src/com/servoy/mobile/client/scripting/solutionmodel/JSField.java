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
import org.timepedia.exporter.client.NoExport;
import org.timepedia.exporter.client.Setter;

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IFieldConstants;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.base.solutionmodel.IBaseSMMethod;
import com.servoy.base.solutionmodel.IBaseSMValueList;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMField;
import com.servoy.mobile.client.util.Utils;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSField extends JSComponent implements IMobileSMField, Exportable
{
	JSField(Field f, JSSolutionModel model, JSBase parent)
	{
		super(f, model, parent);
	}

	@NoExport
	public static JSField createField(Field field, JSSolutionModel model, JSBase parent)
	{
		if (field == null)
		{
			return null;
		}

		switch (field.getDisplayType())
		{
			case IFieldConstants.TEXT_FIELD :
				return new JSText(field, model, parent);

			case IFieldConstants.TEXT_AREA :
				return new JSTextArea(field, model, parent);

			case IFieldConstants.COMBOBOX :
				return new JSCombobox(field, model, parent);

			case IFieldConstants.RADIOS :
				return new JSRadios(field, model, parent);

			case IFieldConstants.CHECKS :
				return new JSChecks(field, model, parent);

			case IFieldConstants.CALENDAR :
				return new JSCalendar(field, model, parent);

			case IFieldConstants.PASSWORD :
				return new JSPassword(field, model, parent);
		}

		return new JSField(field, model, parent);
	}


	@Override
	@Getter
	public String getDataProviderID()
	{
		return ((Field)getBase()).getDataProviderID();
	}

	@Override
	@Getter
	public String getPlaceholderText()
	{
		return ((Field)getBase()).getPlaceholderText();
	}

	@Getter
	@Override
	public int getDisplayType()
	{
		return ((Field)getBase()).getDisplayType();
	}

	@Getter
	@Override
	public JSValueList getValuelist()
	{
		return new JSValueList(getSolutionModel().getApplication().getFlattenedSolution().getValueListByUUID(((Field)getBase()).getValuelistID()));
	}

	@Setter
	@Override
	public void setDataProviderID(String arg)
	{
		cloneIfNeeded();
		((Field)getBase()).setDataProviderID(arg);
	}

	@Setter
	@Override
	public void setPlaceholderText(String placeholder)
	{
		cloneIfNeeded();
		((Field)getBase()).setPlaceholderText(placeholder);
	}

	@Setter
	@Override
	public void setDisplayType(int arg)
	{
		cloneIfNeeded();
		((Field)getBase()).setDisplayType(arg);
	}

	@Setter
	@Override
	public void setValuelist(IBaseSMValueList valuelist)
	{
		setValuelist((JSValueList)valuelist);
	}

	public void setValuelist(JSValueList valuelist)
	{
		cloneIfNeeded();
		((Field)getBase()).setValueListID(valuelist.getUUID());
	}

	@Setter
	@Override
	public void setOnAction(IBaseSMMethod method)
	{
		setOnAction((JSMethod)method);
	}

	public void setOnAction(JSMethod method)
	{
		cloneIfNeeded();
		((Field)getBase()).setActionMethodCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnAction()
	{
		return JSMethod.getMethodFromString(((Field)getBase()).getActionMethodCall(), (JSForm)getParent(), getSolutionModel());
	}

	@Setter
	@Override
	public void setOnDataChange(IBaseSMMethod method)
	{
		setOnDataChange((JSMethod)method);
	}

	public void setOnDataChange(JSMethod method)
	{
		cloneIfNeeded();
		((Field)getBase()).setDataChangeMethodCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnDataChange()
	{
		return JSMethod.getMethodFromString(((Field)getBase()).getDataChangeMethodCall(), (JSForm)getParent(), getSolutionModel());
	}

	@Override
	public JSTitle getTitle()
	{
		return new JSTitle((GraphicalComponent)getTitleForComponent(this).getBase(), getSolutionModel(), getParent());
	}

	@NoExport
	public static JSLabel getTitleForComponent(JSComponent comp)
	{
		JSForm parentForm = (JSForm)comp.getParent();
		String cGroup = comp.getGroupID();
		if (cGroup != null)
		{
			JSComponent[] labels = parentForm.getComponentsInternal(true, Integer.valueOf(IRepositoryConstants.GRAPHICALCOMPONENTS));
			for (JSComponent label : labels)
			{
				if (label instanceof JSLabel)
				{
					JSLabel l = (JSLabel)label;
					if (cGroup.equals(l.getGroupID()))
					{
						// I guess the following if might as well not be; the location thing is for legacy solutions (before the COMPONENT_TITLE existed)
						if (Boolean.TRUE.equals(l.getCustomProperty(IMobileProperties.COMPONENT_TITLE)) || l.getY() < comp.getY() ||
							(l.getY() == comp.getY() && l.getX() < comp.getX()))
						{
							return l;
						}
					}
				}
			}
		}

		if (cGroup == null)
		{
			comp.setGroupID(cGroup = Utils.createStringUUID());
		}
		JSLabel titleLabel = parentForm.newLabel(null, comp.getX() - 1, comp.getY() - 1, comp.getWidth(), comp.getHeight());
		titleLabel.setGroupID(cGroup);
		titleLabel.putCustomProperty(IMobileProperties.COMPONENT_TITLE, Boolean.TRUE);
		return titleLabel;
	}

}
