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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.NoExport;
import org.timepedia.exporter.client.Setter;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.thirdparty.javascript.jscomp.mozilla.rhino.annotations.JSFunction;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IComponentConstants;
import com.servoy.base.persistence.constants.IFieldConstants;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.base.solutionmodel.IBaseSMMethod;
import com.servoy.base.solutionmodel.IBaseSMPortal;
import com.servoy.base.util.DataSourceUtilsBase;
import com.servoy.mobile.client.dataprocessing.RelatedFoundSet;
import com.servoy.mobile.client.persistence.AbstractBase.MobileProperties;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.Part;
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

	private Form form;

	public JSForm(Form form, JSSolutionModel model)
	{
		super(form, model, null);
		this.form = form;
	}

	@NoExport
	@Override
	protected boolean cloneIfNeeded()
	{
		if (!getBase().isClone())
		{
			form = getSolutionModel().getApplication().getFlattenedSolution().cloneFormDeep(form);
			setBase(form);
			return true;
		}
		return false;
	}

	@Getter
	public String getName()
	{
		return form.getName();
	}


	@Override
	public JSVariable newVariable(String name, int type)
	{
		cloneIfNeeded();
		JSVariable fv = new JSVariable(ScriptEngine.FORMS, getName(), name, getSolutionModel(), this);
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
		JSVariable fv = new JSVariable(ScriptEngine.FORMS, getName(), name, getSolutionModel(), this);
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
		cloneIfNeeded();
		String[] codeAndName = JSMethod.splitCodeFromName(code);
		if (codeAndName != null && codeAndName.length == 2)
		{
			JSMethod fm = new JSMethod(ScriptEngine.FORMS, getName(), codeAndName[1], getSolutionModel(), this);
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
		JSMethod fm = new JSMethod(ScriptEngine.FORMS, getName(), name, getSolutionModel(), this);
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
		cloneIfNeeded();
		Field f = Field.createNewField(form, type);
		if (dataprovider instanceof String) f.setDataProviderID((String)dataprovider);
		f.setSize(width, height);
		f.setLocation(x, y);
		return new JSField(f, getSolutionModel(), this);
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
		cloneIfNeeded();
		GraphicalComponent gc = GraphicalComponent.createNewGraphicalComponent(form, IComponentConstants.VIEW_TYPE_BUTTON);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		if (action != null) gc.setOnActionMethodCall(action.getReferenceString());
		return new JSButton(gc, getSolutionModel(), this);
	}

	@Override
	public JSLabel newLabel(String txt, int x, int y, int width, int height)
	{
		cloneIfNeeded();
		GraphicalComponent gc = GraphicalComponent.createNewGraphicalComponent(form, IComponentConstants.VIEW_TYPE_LABEL);
		gc.setText(txt);
		gc.setSize(width, height);
		gc.setLocation(x, y);
		return new JSLabel(gc, getSolutionModel(), this);
	}

	@NoExport
	@Override
	public IBaseSMPortal newPortal(String name, Object relation, int x, int y, int width, int height)
	{
		cloneIfNeeded();
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
		return new JSPortal(portal, getSolutionModel(), this);
	}

	@NoExport
	@Override
	public JSPortal getPortal(String name)
	{
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				Portal portal = Portal.castIfPossible(component);
				if (portal != null && name.equals(portal.getName()))
				{
					return new JSPortal(portal, getSolutionModel(), this);
				}
			}
		}
		return null;
	}

	@NoExport
	@Override
	public boolean removePortal(String name)
	{
		cloneIfNeeded();
		return removeComponent(name, IRepositoryConstants.PORTALS);
	}

	@NoExport
	@Override
	public JSPortal[] getPortals()
	{
		List<JSPortal> portals = new ArrayList<JSPortal>();
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			Portal portal = Portal.castIfPossible(component);
			if (portal != null)
			{
				portals.add(new JSPortal(portal, getSolutionModel(), this));
			}
		}
		return portals.toArray(new JSPortal[0]);
	}

	@NoExport
	@Override
	public JSTabPanel newTabPanel(String name, int x, int y, int width, int height)
	{
		cloneIfNeeded();
		TabPanel tabPanel = form.createNewTabPanel();
		tabPanel.setSize(width, height);
		tabPanel.setLocation(x, y);
		return new JSTabPanel(tabPanel, getSolutionModel(), this);
	}

	@NoExport
	@Override
	public JSTabPanel getTabPanel(String name)
	{
		if (name != null)
		{
			JSComponent[] tabPanels = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.TABPANELS));
			for (JSComponent tabPanel : tabPanels)
			{
				if (tabPanel != null && name.equals(tabPanel.getName()))
				{
					return (JSTabPanel)tabPanel;
				}
			}
		}
		return null;
	}

	@NoExport
	@Override
	public boolean removeTabPanel(String name)
	{
		return removeComponent(name, IRepositoryConstants.TABPANELS);
	}

	@NoExport
	@Override
	public JSTabPanel[] getTabPanels()
	{
		JSComponent[] components = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.TABPANELS));
		return Arrays.asList(components).toArray(new JSTabPanel[0]);
	}

	@Override
	public JSField getField(String name)
	{
		if (name != null)
		{
			JSComponent[] fields = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.FIELDS));
			for (JSComponent field : fields)
			{
				if (field != null && name.equals(field.getName()))
				{
					return (JSField)field;
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
		JSComponent[] components = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.FIELDS));
		return Arrays.asList(components).toArray(new JSField[0]);
	}

	@Override
	public JSButton getButton(String name)
	{
		if (name != null)
		{
			JSComponent[] buttons = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.GRAPHICALCOMPONENTS));
			for (JSComponent button : buttons)
			{
				if (button != null && name.equals(button.getName()) && button instanceof JSButton)
				{
					return (JSButton)button;
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
		JSComponent[] components = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.GRAPHICALCOMPONENTS));
		List<JSButton> buttons = new ArrayList<JSButton>();
		for (JSComponent component : components)
		{
			if (component instanceof JSButton)
			{
				buttons.add((JSButton)component);
			}
		}
		return buttons.toArray(new JSButton[0]);
	}

	@Override
	public JSComponent getComponent(String name)
	{
		if (name != null)
		{
			JSComponent[] components = getComponentsInternal(false, null);
			for (JSComponent comp : components)
			{
				if (comp != null && name.equals(comp.getName()))
				{
					return comp;
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
		return getComponentsInternal(false, null);
	}

	@Override
	@NoExport
	public JSComponent[] getComponentsInternal(boolean showInternals, Integer componentType)
	{
		JsArray<Component> formComponents = form.getComponents();

		List<JSComponent> components = new ArrayList<JSComponent>(formComponents.length());
		HashMap<String, JSLabel> titleLabels = showInternals ? null : new HashMap<String, JSLabel>();

		for (int i = 0; i < formComponents.length(); i++)
		{
			JSComponent jsComponent = JSComponent.getJSComponent(formComponents.get(i), getSolutionModel(), this, componentType);
			if (jsComponent != null)
			{
				boolean isFormListComponent = false;
				if (!showInternals)
				{
					MobileProperties mp = jsComponent.getBase().getMobileProperties();
					isFormListComponent = mp != null &&
						(Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.LIST_ITEM_BUTTON)) ||
							Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.LIST_ITEM_COUNT)) ||
							Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.LIST_ITEM_HEADER)) ||
							Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.LIST_ITEM_IMAGE)) || Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.LIST_ITEM_SUBTEXT)));
				}

				if (showInternals || (!(jsComponent instanceof JSPortal) && !isFormListComponent))
				{
					components.add(jsComponent);
				}

				if (!showInternals)
				{
					String groupID = jsComponent.getGroupID();
					if (groupID != null && jsComponent instanceof JSLabel)
					{
						// a grouped label is a title label for a component, except for the case where it's grouped with another label
						MobileProperties mp = jsComponent.getBase().getMobileProperties();
						JSLabel l = titleLabels.get(groupID);
						if (l == null || (mp != null && Boolean.TRUE.equals(mp.getPropertyValue(IMobileProperties.COMPONENT_TITLE))) ||
							(jsComponent.getY() < l.getY() || (jsComponent.getY() == l.getY() && jsComponent.getX() < l.getX())))
						{
							titleLabels.put(groupID, (JSLabel)jsComponent);
						}
					}
				}
			}
		}
		if (!showInternals) components.removeAll(titleLabels.values());
		return components.toArray(new JSComponent[components.size()]);
	}

	@Override
	public JSLabel getLabel(String name)
	{
		if (name != null)
		{
			JSComponent[] labels = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.GRAPHICALCOMPONENTS));
			for (JSComponent label : labels)
			{
				if (label != null && name.equals(label.getName()) && label instanceof JSLabel)
				{
					return (JSLabel)label;
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
		cloneIfNeeded();
		if (name != null)
		{
			JsArray<Component> formComponents = form.getComponents();
			for (int i = 0; i < formComponents.length(); i++)
			{
				Component component = formComponents.get(i);
				if (component.getTypeID() == componentType && name.equals(component.getName()))
				{
					form.removeChild(i);

					if (componentType == IRepositoryConstants.GRAPHICALCOMPONENTS || componentType == IRepositoryConstants.FIELDS)
					{
						// remove title label if it has one
						String group = component.getGroupID();
						if (group != null)
						{
							formComponents = form.getComponents(); // an item was deleted from it natively and remove by index must be in sync
							for (int j = 0; j < formComponents.length(); j++)
							{
								// search for labels with the same groupID
								component = formComponents.get(j);
								if (group.equals(component.getGroupID()))
								{
									GraphicalComponent gc = GraphicalComponent.castIfPossible(component);
									if (gc != null && !gc.isButton())
									{
										form.removeChild(j);
										break;
									}
								}
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public JSLabel[] getLabels()
	{
		JSComponent[] components = getComponentsInternal(false, Integer.valueOf(IRepositoryConstants.GRAPHICALCOMPONENTS));
		List<JSLabel> labels = new ArrayList<JSLabel>();
		for (JSComponent component : components)
		{
			if (component instanceof JSLabel)
			{
				labels.add((JSLabel)component);
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
		cloneIfNeeded();
		form.setDataSource(arg);
	}

	@Override
	public boolean removeVariable(String name)
	{
		cloneIfNeeded();
		JSVariable variable = getVariable(name);
		return variable.remove();
	}

	@Override
	public boolean removeMethod(String name)
	{
		cloneIfNeeded();
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
		cloneIfNeeded();
		form.setView(viewType);
	}

	@Getter
	@Override
	public JSMethod getOnShow()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnShowCall(), this, getSolutionModel());
	}

	@Override
	public void setOnShow(IBaseSMMethod method)
	{
		setOnShow((JSMethod)method);
	}

	@Setter
	public void setOnShow(JSMethod method)
	{
		cloneIfNeeded();
		((Form)getBase()).setOnShowCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnLoad()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnLoadCall(), this, getSolutionModel());
	}

	@Override
	public void setOnLoad(IBaseSMMethod method)
	{
		setOnLoad((JSMethod)method);
	}

	@Setter
	public void setOnLoad(JSMethod method)
	{
		cloneIfNeeded();
		((Form)getBase()).setOnLoadCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnHide()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnHideCall(), this, getSolutionModel());
	}

	@Override
	public void setOnHide(IBaseSMMethod method)
	{
		setOnHide((JSMethod)method);
	}

	@Setter
	public void setOnHide(JSMethod method)
	{
		cloneIfNeeded();
		((Form)getBase()).setOnHideCall(method != null ? method.getReferenceString() : null);
	}

	@Getter
	@Override
	public JSMethod getOnRecordSelection()
	{
		return JSMethod.getMethodFromString(((Form)getBase()).getOnRecordSelectionCall(), this, getSolutionModel());
	}

	@Override
	public void setOnRecordSelection(IBaseSMMethod method)
	{
		setOnRecordSelection((JSMethod)method);
	}

	@Setter
	public void setOnRecordSelection(JSMethod method)
	{
		cloneIfNeeded();
		((Form)getBase()).setOnRecordSelectionCall(method != null ? method.getReferenceString() : null);
	}

	@JSFunction
	@Override
	public JSPart getPart(int type)
	{
		JsArray<Component> formComponents = form.getComponents();
		for (int i = 0; i < formComponents.length(); i++)
		{
			Part part = Part.castIfPossible(formComponents.get(i));
			if (part != null && part.getType() == type)
			{
				return new JSPart(part, getSolutionModel(), this);
			}
		}
		return null;
	}

	@JSFunction
	@Override
	public JSPart newFooterPart(int height)
	{
		return getOrCreatePart(IPartConstants.FOOTER);
	}

	@JSFunction
	@Override
	public JSPart newHeaderPart(int height)
	{
		return getOrCreatePart(IPartConstants.HEADER);
	}

	@NoExport
	private JSPart getOrCreatePart(int partType)
	{
		JSPart jsPart = getPart(partType);
		if (jsPart == null)
		{
			cloneIfNeeded();
			Part part = form.createNewPart(partType);
			jsPart = new JSPart(part, getSolutionModel(), this);
		}
		return jsPart;
	}
}
