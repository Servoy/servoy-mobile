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
import org.timepedia.exporter.client.Exportable;

import com.servoy.j2db.persistence.constants.IFieldConstants;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;

/**
 * @author jcompagner
 */
@Export
public class JSForm implements IBaseSMForm, Exportable
{

	private final Form form;
	private final JSSolutionModel model;

	/**
	 * @param form
	 */
	public JSForm(Form form, JSSolutionModel model)
	{
		this.form = form;
		this.model = model;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newVariable(java.lang.String, int)
	 */
	@Override
	public JSVariable newVariable(String name, int type)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newVariable(java.lang.String, int, java.lang.String)
	 */
	@Override
	public JSVariable newVariable(String name, int type, String defaultValue)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getVariable(java.lang.String)
	 */
	@Override
	public JSVariable getVariable(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getVariables()
	 */
	@Override
	public JSVariable[] getVariables()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	@Override
	public JSMethod newMethod(String code)
	{
		if (code == null) return null;
		String[] codeAndName = JSMethod.splitCodeFromName(code);
		if (codeAndName != null && codeAndName.length == 2)
		{
			JSMethod fm = new JSMethod(JSScriptPart.FORMS, getName(), codeAndName[1], model);

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
		JSMethod fm = new JSMethod(JSScriptPart.FORMS, getName(), name, model);
		return fm.exists() ? fm : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getMethods()
	 */
	@Override
	public JSMethod[] getMethods()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newField(java.lang.Object, int, int, int, int, int)
	 */
	@Override
	public JSField newField(Object dataprovider, int type, int x, int y, int width, int height)
	{
		// TODO ac add support for JSVariable for all these methods that get a dataprovider
		Field f = form.createNewField(type);
		if (dataprovider instanceof String) f.setDataProviderID((String)dataprovider);
		f.setSize(width, height);
		f.setLocation(x, y);
		return new JSField(f, model);
	}

	@Override
	public JSField newTextField(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac add support for JSVariable for all these methods that get a dataprovider
		return newField(dataprovider, IFieldConstants.TEXT_FIELD, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newTextArea(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newTextArea(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newComboBox(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newComboBox(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newListBox(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newListBox(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newRadios(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newRadios(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newCheck(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newCheck(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newCalendar(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newCalendar(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newImageMedia(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newImageMedia(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newPassword(java.lang.Object, int, int, int, int)
	 */
	@Override
	public JSField newPassword(Object dataprovider, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
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
		return new JSButton(gc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#newLabel(java.lang.String, int, int, int, int)
	 */
	@Override
	public JSLabel newLabel(String txt, int x, int y, int width, int height)
	{
		// TODO ac Auto-generated method stub
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getServerName()
	 */
	@Override
	public String getServerName()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getTableName()
	 */
	@Override
	public String getTableName()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#getDataSource()
	 */
	@Override
	public String getDataSource()
	{
		// TODO ac Auto-generated method stub
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeVariable(java.lang.String)
	 */
	@Override
	public boolean removeVariable(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm#removeMethod(java.lang.String)
	 */
	@Override
	public boolean removeMethod(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

}
