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
import org.timepedia.exporter.client.ExporterUtil;
import org.timepedia.exporter.client.NoExport;

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Solution;

/**
 * @author jcompagner
 *
 */
@Export
public class JSSolutionModel implements IBaseSolutionModel, Exportable
{
	private final MobileClient application;
	private final Solution solution;

	public JSSolutionModel(MobileClient application)
	{
		this.application = application;
		this.solution = application.getSolution();
		export(ExporterUtil.wrap(this));
	}

	public MobileClient getApplication()
	{
		return application;
	}

	private native void export(Object object)
	/*-{
		$wnd.solutionModel = object;
	}-*/;

	public JSForm getForm(String name)
	{
		Form form = solution.getForm(name);
		if (form != null)
		{
			return new JSForm(form, this);
		}
		return null;
	}


	@Override
	public JSForm newForm(String name, String serverName, String tableName, String styleName, boolean show_in_menu, int width, int height)
	{
		// TODO ac merge servername with tablename => datasource USING an utility method
		return newForm(name, "db:/" + serverName + "/" + tableName, styleName, show_in_menu, width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#newForm(java.lang.String, java.lang.String, java.lang.String, boolean, int, int)
	 */
	@Override
	public JSForm newForm(String name, String dataSource, String styleName, boolean show_in_menu, int width, int height)
	{
		Form form = solution.getForm(name);
		if (form == null)
		{
			// TODO ac use datasource and width/height
			return new JSForm(solution.newForm(name, dataSource, width, height), this);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneForm(java.lang.String, com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm)
	 */
	@Override
	public JSForm cloneForm(String newName, IBaseSMForm form)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneComponent(java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent)
	 */
	@Override
	public JSComponent cloneComponent(String newName, IBaseSMComponent component)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneComponent(java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent, com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm)
	 */
	@Override
	public JSComponent cloneComponent(String newName, IBaseSMComponent component, IBaseSMForm newParentForm)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeForm(java.lang.String)
	 */
	@Override
	public boolean removeForm(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeRelation(java.lang.String)
	 */
	@Override
	public boolean removeRelation(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeGlobalMethod(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeGlobalMethod(String scopeName, String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeGlobalVariable(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeGlobalVariable(String scopeName, String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeValueList(java.lang.String)
	 */
	@Override
	public boolean removeValueList(String name)
	{
		// TODO ac Auto-generated method stub
		return false;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms(java.lang.String)
//	 */
//	@Override
//	public JSForm[] getForms(String datasource)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public JSForm[] getForms(String server, String tablename)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms()
//	 */
//	@Override
//	public JSForm[] getForms()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getValueList(java.lang.String)
	 */
	@Override
	public JSValueList getValueList(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getValueLists()
//	 */
//	@Override
//	public JSValueList[] getValueLists()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#newValueList(java.lang.String, int)
	 */
	@Override
	public JSValueList newValueList(String name, int type)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#newGlobalVariable(java.lang.String, java.lang.String, int)
	 */
	@Override
	public JSVariable newGlobalVariable(String scopeName, String name, int type)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalVariable(java.lang.String, java.lang.String)
	 */
	@Override
	public JSVariable getGlobalVariable(String scopeName, String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getScopeNames()
	 */
	@Override
	public String[] getScopeNames()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalVariables()
//	 */
//	@Override
//	public JSVariable[] getGlobalVariables()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalVariables(java.lang.String)
//	 */
//	@Override
//	public JSVariable[] getGlobalVariables(String scopeName)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

	@Override
	public JSMethod newGlobalMethod(String scopeName, String code)
	{
		if (code == null) return null;
		String scope = (scopeName == null ? "globals" : scopeName); //$NON-NLS-1$

		String[] codeAndName = JSMethod.splitCodeFromName(code);
		if (codeAndName != null && codeAndName.length == 2)
		{
			JSMethod fm = new JSMethod(JSScriptPart.SCOPES, scope, codeAndName[1], this);

			if (!fm.exists())
			{
				createScopeIfNecessary(scope);
				fm.create(codeAndName[0]);
				return fm;
			}
		}
		return null;
	}

	@NoExport
	public final native void createScopeIfNecessary(String scope) /*-{
		if (!$wnd._ServoyInit_.scopes[scope]) {
			Object.defineProperty($wnd.scopes, scope, {
				get : function() {
					return $wnd._ServoyUtils_.getGlobalScope(scope);
				},
				configurable : false
			});
			$wnd._ServoyInit_.scopes[scope] = {
				fncs : {},
				vrbs : {}
			};
		}
	}-*/;

	@Override
	public JSMethod getGlobalMethod(String scopeName, String name)
	{
		JSMethod gm = new JSMethod(JSScriptPart.SCOPES, scopeName, name, this);
		return gm.exists() ? gm : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#wrapMethodWithArguments(com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod,
	 * java.lang.Object[])
	 */
	@Override
	public JSMethod wrapMethodWithArguments(IBaseSMMethod method, Object... args)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalMethods()
//	 */
//	@Override
//	public JSMethod[] getGlobalMethods()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalMethods(java.lang.String)
//	 */
//	@Override
//	public JSMethod[] getGlobalMethods(String scopeName)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#newRelation(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public JSRelation newRelation(String name, String primaryDataSource, String foreignDataSource, int joinType)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getRelation(java.lang.String)
	 */
	@Override
	public JSRelation getRelation(String name)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getRelations(java.lang.String)
//	 */
//	@Override
//	public JSRelation[] getRelations(String datasource)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getRelations(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public JSRelation[] getRelations(String servername, String tablename)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

}
