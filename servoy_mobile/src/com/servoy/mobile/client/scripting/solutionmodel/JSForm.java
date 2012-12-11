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

import com.servoy.j2db.persistence.constants.IFieldConstants;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.j2db.util.DataSourceUtilsBase;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.ScriptEngine;
import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMForm;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSForm extends JSBase implements IMobileSMForm, Exportable
{

	private final Form form;

	public JSForm(Form form, JSSolutionModel model)
	{
		super(form, model);
		this.form = form;
	}

	public String getName()
	{
		return form.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#getDesignTimeProperty(java.lang.String)
	 */
	@Override
	public Object getDesignTimeProperty(String key)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#putDesignTimeProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object putDesignTimeProperty(String key, Object value)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMHasDesignTimeProperty#removeDesignTimeProperty(java.lang.String)
	 */
	@Override
	public Object removeDesignTimeProperty(String key)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	@Override
	public JSVariable newVariable(String name, int type)
	{
		JSVariable fv = new JSVariable(ScriptEngine.FORMS, getName(), name, getSolutionModel());

		if (!fv.exists())
		{
			fv.create(type);
			return fv;
		}
		return null;
	}

	@Override
	public JSVariable newVariable(String name, int type, String defaultValue)
	{
		JSVariable newVar = newVariable(name, type);
		if (newVar != null) newVar.setDefaultValue(defaultValue);
		return newVar;
	}

	@Override
	public JSVariable getVariable(String name)
	{
		JSVariable fv = new JSVariable(ScriptEngine.FORMS, getName(), name, getSolutionModel());
		return fv.exists() ? fv : null;
	}

	@Override
	public JSVariable[] getVariables()
	{
		String[] names = ScriptEngine.getVariableNamesInternal(ScriptEngine.FORMS, getName());
		if (names != null)
		{
			JSVariable[] variables = new JSVariable[names.length];
			for (int i = names.length - 1; i >= 0; i--)
			{
				variables[i] = getVariable(names[i]);
			}
		}
		return new JSVariable[0];
	}

	@Override
	public JSMethod newMethod(String code)
	{
		if (code == null) return null;
		String[] codeAndName = JSMethod.splitCodeFromName(code);
		if (codeAndName != null && codeAndName.length == 2)
		{
			JSMethod fm = new JSMethod(ScriptEngine.FORMS, getName(), codeAndName[1], getSolutionModel());

			if (!fm.exists())
			{
				fm.create(codeAndName[0]);
				return fm;
			}
		}
		return null;
	}

	@Override
	public JSMethod getMethod(String name)
	{
		JSMethod fm = new JSMethod(ScriptEngine.FORMS, getName(), name, getSolutionModel());
		return fm.exists() ? fm : null;
	}

	@Override
	public JSMethod[] getMethods()
	{
		String[] names = ScriptEngine.getFunctionNamesInternal(ScriptEngine.FORMS, getName());
		if (names != null)
		{
			JSMethod[] methods = new JSMethod[names.length];
			for (int i = names.length - 1; i >= 0; i--)
			{
				methods[i] = getMethod(names[i]);
			}
		}
		return new JSMethod[0];
	}

	@Override
	public JSField newField(Object dataprovider, int type, int x, int y, int width, int height)
	{
		Field f = form.createNewField(type);
		if (dataprovider instanceof String) f.setDataProviderID((String)dataprovider);
		f.setSize(width, height);
		f.setLocation(x, y);
		return new JSField(f, getSolutionModel());
	}

	public JSField newField(JSVariable dataprovider, int type, int x, int y, int width, int height)
	{
		return newField(dataprovider.getReferenceString(), type, x, y, width, height);
	}

	@Override
	public JSField newTextField(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.TEXT_FIELD, x, y, width, height);
	}

	public JSField newTextField(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newTextField(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newTextArea(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.TEXT_AREA, x, y, width, height);
	}

	public JSField newTextArea(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newTextArea(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newComboBox(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.COMBOBOX, x, y, width, height);
	}

	public JSField newComboBox(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newComboBox(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newListBox(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.LIST_BOX, x, y, width, height);
	}

	public JSField newListBox(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newListBox(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newRadios(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.RADIOS, x, y, width, height);
	}

	public JSField newRadios(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newRadios(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newCheck(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.CHECKS, x, y, width, height);
	}

	public JSField newCheck(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newCheck(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newCalendar(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.CALENDAR, x, y, width, height);
	}

	public JSField newCalendar(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newCalendar(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newImageMedia(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.IMAGE_MEDIA, x, y, width, height);
	}

	public JSField newImageMedia(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newImageMedia(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSField newPassword(Object dataprovider, int x, int y, int width, int height)
	{
		return newField(dataprovider, IFieldConstants.PASSWORD, x, y, width, height);
	}

	public JSField newPassword(JSVariable dataprovider, int x, int y, int width, int height)
	{
		return newPassword(dataprovider.getReferenceString(), x, y, width, height);
	}

	@Override
	public JSButton newButton(String txt, int x, int y, int width, int height, Object action)
	{
		return newButton(txt, x, y, width, height, null); // because of how GWT Exporter works, the other method will be most likely called instead anyway for JSMethod actions
	}

	public JSButton newButton(String txt, int x, int y, int width, int height, JSMethod action)
	{
		GraphicalComponent gc = form.createNewGraphicalComponent(GraphicalComponent.VIEW_TYPE_BUTTON);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		if (action != null) gc.setOnActionMethodID(action.getReferenceString());
		return new JSButton(gc, getSolutionModel());
	}

	@Override
	public JSLabel newLabel(String txt, int x, int y, int width, int height)
	{
		GraphicalComponent gc = form.createNewGraphicalComponent(GraphicalComponent.VIEW_TYPE_LABEL);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		return new JSLabel(gc, getSolutionModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newTabPanel(java.lang.String, int, int, int, int)
	 */
	@Override
	public JSTabPanel newTabPanel(String name, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getTabPanel(java.lang.String)
	 */
	@Override
	public JSTabPanel getTabPanel(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeTabPanel(java.lang.String)
	 */
	@Override
	public boolean removeTabPanel(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getTabPanels()
	 */
	@Override
	public JSTabPanel[] getTabPanels()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getField(java.lang.String)
	 */
	@Override
	public JSField getField(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeField(java.lang.String)
	 */
	@Override
	public boolean removeField(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getFields()
	 */
	@Override
	public JSField[] getFields()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getButton(java.lang.String)
	 */
	@Override
	public JSButton getButton(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeButton(java.lang.String)
	 */
	@Override
	public boolean removeButton(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getButtons()
	 */
	@Override
	public JSButton[] getButtons()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getComponent(java.lang.String)
	 */
	@Override
	public JSComponent getComponent(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeComponent(java.lang.String)
	 */
	@Override
	public boolean removeComponent(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getComponents()
	 */
	@Override
	public JSComponent[] getComponents()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getLabel(java.lang.String)
	 */
	@Override
	public JSLabel getLabel(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeLabel(java.lang.String)
	 */
	@Override
	public boolean removeLabel(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getLabels()
	 */
	@Override
	public JSLabel[] getLabels()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName()
	{
		String ds = form.getDataSource();
		if (ds != null)
		{
			String[] stn = DataSourceUtilsBase.getDBServernameTablename(ds);
			if (stn != null && stn.length > 0) return stn[0];
		}
		return null;
	}

	@Override
	public String getTableName()
	{
		String ds = form.getDataSource();
		if (ds != null)
		{
			String[] stn = DataSourceUtilsBase.getDBServernameTablename(ds);
			if (stn != null && stn.length == 2) return stn[1];
		}
		return null;
	}

	@Override
	public String getDataSource()
	{
		return form.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getView()
	 */
	@Override
	public int getView()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getWidth()
	 */
	@Override
	public int getWidth()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setServerName(java.lang.String)
	 */
	@Override
	public void setServerName(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setTableName(java.lang.String)
	 */
	@Override
	public void setTableName(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setDataSource(java.lang.String)
	 */
	@Override
	public void setDataSource(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setView(int)
	 */
	@Override
	public void setView(int arg)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setWidth(int)
	 */
	@Override
	public void setWidth(int width)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnElementFocusGained()
	 */
	@Override
	public JSMethod getOnElementFocusGained()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnElementFocusLost()
	 */
	@Override
	public JSMethod getOnElementFocusLost()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnHide()
	 */
	@Override
	public JSMethod getOnHide()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnLoad()
	 */
	@Override
	public JSMethod getOnLoad()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnRecordEditStart()
	 */
	@Override
	public JSMethod getOnRecordEditStart()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnRecordEditStop()
	 */
	@Override
	public JSMethod getOnRecordEditStop()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnRecordSelection()
	 */
	@Override
	public JSMethod getOnRecordSelection()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnShow()
	 */
	@Override
	public JSMethod getOnShow()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnUnLoad()
	 */
	@Override
	public JSMethod getOnUnLoad()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getOnResize()
	 */
	@Override
	public JSMethod getOnResize()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnElementFocusGained(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnElementFocusGained(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnElementFocusLost(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnElementFocusLost(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnHide(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnHide(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnLoad(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnLoad(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnRecordEditStart(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnRecordEditStart(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnRecordEditStop(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnRecordEditStop(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnRecordSelection(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnRecordSelection(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnShow(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnShow(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnUnLoad(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnUnLoad(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setOnResize(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod)
	 */
	@Override
	public void setOnResize(IBaseSMMethod method)
	{
		// TODO ac Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getEncapsulation()
	 */
	@Override
	public int getEncapsulation()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#setEncapsulation(int)
	 */
	@Override
	public void setEncapsulation(int arg)
	{
		// TODO ac Auto-generated method stub

	}

	@Override
	public boolean removeVariable(String name)
	{
		JSVariable variable = getVariable(name);
		return variable.remove();
	}

	@Override
	public boolean removeMethod(String name)
	{
		JSMethod method = getMethod(name);
		return method.remove();
	}

}
