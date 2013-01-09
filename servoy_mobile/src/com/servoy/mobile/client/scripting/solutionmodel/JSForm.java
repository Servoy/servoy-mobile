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

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.servoy.j2db.persistence.constants.IFieldConstants;
import com.servoy.j2db.persistence.constants.IRepositoryConstants;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMPortal;
import com.servoy.j2db.util.DataSourceUtilsBase;
import com.servoy.mobile.client.dataprocessing.RelatedFoundSet;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Portal;
import com.servoy.mobile.client.persistence.TabPanel;
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
		super(form, form.getName(), model);
		this.form = form;
	}

	@Getter
	public String getName()
	{
		return form.getName();
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
		JsArrayString names = ScriptEngine.getVariableNamesInternal(ScriptEngine.FORMS, getName());
		if (names != null)
		{
			JSVariable[] variables = new JSVariable[names.length()];
			for (int i = names.length() - 1; i >= 0; i--)
			{
				variables[i] = getVariable(names.get(i));
			}
			return variables;
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
		JsArrayString names = ScriptEngine.getFunctionNamesInternal(ScriptEngine.FORMS, getName());
		if (names != null)
		{
			JSMethod[] methods = new JSMethod[names.length()];
			for (int i = names.length() - 1; i >= 0; i--)
			{
				methods[i] = getMethod(names.get(i));
			}
			return methods;
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
		return new JSField(f, form.getName(), getSolutionModel());
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
		if (action != null) gc.setOnActionMethodCall(action.getReferenceString());
		return new JSButton(gc, form.getName(), getSolutionModel());
	}

	@Override
	public JSLabel newLabel(String txt, int x, int y, int width, int height)
	{
		GraphicalComponent gc = form.createNewGraphicalComponent(GraphicalComponent.VIEW_TYPE_LABEL);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		return new JSLabel(gc, form.getName(), getSolutionModel());
	}

	@Override
	public IBaseSMPortal newPortal(String name, Object relation, int x, int y, int width, int height)
	{
		Portal portal = form.createNewPortal(name);
		portal.setLocation(x, y);
		portal.setSize(width, height);
		String relationName = null;
		if (relation instanceof RelatedFoundSet)
		{
			relationName = ((RelatedFoundSet)relation).getRelationName();
		}
		else if (relation instanceof String)
		{
			relationName = (String)relation;
		}
//			else if (relation instanceof JSRelation) // when we have relations in mobile solution model
//			{
//				relationName = ((JSRelation)relation).getName();
//			}
		portal.setRelationName(relationName);
		return new JSPortal(portal, form.getName(), getSolutionModel());
	}

	@Override
	public JSPortal getPortal(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				Portal portal = component.isPortal();
				if (portal != null && name.equals(portal.getName()))
				{
					return new JSPortal(portal, form.getName(), getSolutionModel());
				}
			}
		}
		return null;
	}

	@Override
	public boolean removePortal(String name)
	{
		return removeComponent(name, IRepositoryConstants.PORTALS);
	}

	@Override
	public JSPortal[] getPortals()
	{
		List<JSPortal> portals = new ArrayList<JSPortal>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Portal portal = component.isPortal();
			if (portal != null)
			{
				portals.add(new JSPortal(portal, form.getName(), getSolutionModel()));
			}
		}
		return portals.toArray(new JSPortal[0]);
	}

	@Override
	public JSTabPanel newTabPanel(String name, int x, int y, int width, int height)
	{
		TabPanel tabPanel = form.createNewTabPanel();
		tabPanel.setSize(width, height);
		tabPanel.setLocation(x, y);
		return new JSTabPanel(tabPanel, form.getName(), getSolutionModel());
	}

	@Override
	public JSTabPanel getTabPanel(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				TabPanel tabPanel = component.isTabPanel();
				if (tabPanel != null && name.equals(tabPanel.getName()))
				{
					return new JSTabPanel(tabPanel, getName(), getSolutionModel());
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeTabPanel(String name)
	{
		return removeComponent(name, IRepositoryConstants.TABPANELS);
	}

	@Override
	public JSTabPanel[] getTabPanels()
	{
		List<JSTabPanel> tabPanels = new ArrayList<JSTabPanel>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			TabPanel tabPanel = component.isTabPanel();
			if (tabPanel != null)
			{
				JSTabPanel jsTabPanel = new JSTabPanel(tabPanel, getName(), getSolutionModel());
				tabPanels.add(jsTabPanel);
			}
		}
		return tabPanels.toArray(new JSTabPanel[0]);
	}

	@Override
	public JSField getField(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				Field field = component.isField();
				if (field != null && name.equals(field.getName()))
				{
					return new JSField(field, getName(), getSolutionModel());
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeField(String name)
	{
		return removeComponent(name, IRepositoryConstants.FIELDS);
	}

	@Override
	public JSField[] getFields()
	{
		List<JSField> fields = new ArrayList<JSField>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Field field = component.isField();
			if (field != null)
			{
				JSField jsField = new JSField(field, getName(), getSolutionModel());
				fields.add(jsField);
			}
		}
		return fields.toArray(new JSField[0]);
	}

	@Override
	public JSButton getButton(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				GraphicalComponent graphicalComponent = component.isGraphicalComponent();
				if (graphicalComponent != null && graphicalComponent.isButton() && name.equals(graphicalComponent.getName()))
				{
					return new JSButton(graphicalComponent, getName(), getSolutionModel());
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeButton(String name)
	{
		return removeComponent(name, IRepositoryConstants.GRAPHICALCOMPONENTS);
	}

	@Override
	public JSButton[] getButtons()
	{
		List<JSButton> buttons = new ArrayList<JSButton>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			GraphicalComponent graphicalComponent = component.isGraphicalComponent();
			if (graphicalComponent != null && graphicalComponent.isButton())
			{
				JSButton jsButton = new JSButton(graphicalComponent, getName(), getSolutionModel());
				buttons.add(jsButton);
			}
		}
		return buttons.toArray(new JSButton[0]);
	}

	@Override
	public JSComponent getComponent(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				if (name.equals(component.getName()))
				{
					return getJSComponent(component);
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeComponent(String name)
	{
		return removeComponent(name, -1);
	}

	@Override
	public JSComponent[] getComponents()
	{
		JsArray<Component> formComponents = form.getComponents();
		List<JSComponent> components = new ArrayList<JSComponent>(formComponents.length());
		for (int i = 0; i < formComponents.length(); i++)
		{
			JSComponent jsComponent = getJSComponent(formComponents.get(i));
			if (jsComponent != null)
			{
				components.add(jsComponent);
			}
		}
		return components.toArray(new JSComponent[components.size()]);
	}

	@Override
	public JSLabel getLabel(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				GraphicalComponent graphicalComponent = component.isGraphicalComponent();
				if (graphicalComponent != null && !graphicalComponent.isButton() && name.equals(graphicalComponent.getName()))
				{
					return new JSLabel(graphicalComponent, getName(), getSolutionModel());
				}
			}
		}
		return null;
	}

	@Override
	public boolean removeLabel(String name)
	{
		return removeComponent(name, IRepositoryConstants.GRAPHICALCOMPONENTS);
	}

	public boolean removeComponent(String name, int componentType)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				Component persistComponent = null;
				if (componentType == IRepositoryConstants.GRAPHICALCOMPONENTS)
				{
					persistComponent = component.isGraphicalComponent();
				}
				else if (componentType == IRepositoryConstants.FIELDS)
				{
					persistComponent = component.isField();
				}
				else if (componentType == IRepositoryConstants.PORTALS)
				{
					persistComponent = component.isPortal();
				}
				else if (componentType == IRepositoryConstants.TABPANELS)
				{
					persistComponent = component.isTabPanel();
				}
				else
				{
					persistComponent = component;
				}
				if (persistComponent != null && name.equals(persistComponent.getName()))
				{
					form.removeComponent(i);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public JSLabel[] getLabels()
	{
		List<JSLabel> labels = new ArrayList<JSLabel>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			GraphicalComponent graphicalComponent = component.isGraphicalComponent();
			if (graphicalComponent != null && !graphicalComponent.isButton())
			{
				JSLabel jsLabel = new JSLabel(graphicalComponent, getName(), getSolutionModel());
				labels.add(jsLabel);
			}
		}
		return labels.toArray(new JSLabel[0]);
	}

	@Getter
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

	@Getter
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

	@Getter
	@Override
	public String getDataSource()
	{
		return form.getDataSource();
	}

	@Setter
	@Override
	public void setServerName(String arg)
	{
		setDataSource(DataSourceUtilsBase.createDBTableDataSource(arg, getTableName()));
	}

	@Setter
	@Override
	public void setTableName(String arg)
	{
		setDataSource(DataSourceUtilsBase.createDBTableDataSource(getServerName(), arg));
	}

	@Setter
	@Override
	public void setDataSource(String arg)
	{
		form.setDataSource(arg);
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

	@Getter
	@Override
	public int getView()
	{
		return form.getView();
	}

	@Setter
	@Override
	public void setView(int viewType)
	{
		form.setView(viewType);
	}

	@Getter
	@Override
	public JSMethod getOnShow()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnShowCall(), getName(), getSolutionModel());
	}

	@Override
	public void setOnShow(IBaseSMMethod method)
	{
		setOnShow((JSMethod)method);

	}

	@Setter
	public void setOnShow(JSMethod method)
	{
		((Form)getBase()).setOnShowCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnLoad()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnLoadCall(), getName(), getSolutionModel());
	}

	@Override
	public void setOnLoad(IBaseSMMethod method)
	{
		setOnLoad((JSMethod)method);
	}

	@Setter
	public void setOnLoad(JSMethod method)
	{
		((Form)getBase()).setOnLoadCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnHide()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnHideCall(), getName(), getSolutionModel());
	}

	@Override
	public void setOnHide(IBaseSMMethod method)
	{
		setOnHide((JSMethod)method);
	}

	@Setter
	public void setOnHide(JSMethod method)
	{
		((Form)getBase()).setOnHideCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnRecordSelection()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnRecordSelectionCall(), getName(), getSolutionModel());
	}

	@Override
	public void setOnRecordSelection(IBaseSMMethod method)
	{
		setOnRecordSelection((JSMethod)method);
	}

	@Setter
	public void setOnRecordSelection(JSMethod method)
	{
		((Form)getBase()).setOnRecordSelectionCall(method != null ? method.getReferenceString() : null);
	}
}
