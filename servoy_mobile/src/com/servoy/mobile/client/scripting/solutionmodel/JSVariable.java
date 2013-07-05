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

import com.servoy.mobile.client.scripting.solutionmodel.i.IMobileSMVariable;

/**
 * @author acostescu
 *
 */
@Export
@ExportPackage("")
public class JSVariable extends JSScriptPart implements IMobileSMVariable, Exportable
{

	public JSVariable(String parentScopeName, String scopeName, String name, JSSolutionModel model, JSForm parentForm)
	{
		super(parentScopeName, scopeName, name, model, parentForm);
	}

	@Getter
	@Override
	public String getDefaultValue()
	{
		return getDefaultValueInternal(path[0], path[1], path[2]);
	}

	@Setter
	@Override
	public void setDefaultValue(String defValueStr)
	{
		if (defValueStr == null) return;
		cloneFormIfNeeded();
		setDefaultValueInternal(path[0], path[1], path[2], defValueStr);
	}

	@Getter
	@Override
	public int getVariableType()
	{
		return getVariableTypeInternal(path[0], path[1], path[2]);
	}

	@Setter
	@Override
	public void setVariableType(int type)
	{
		cloneFormIfNeeded();
		setVariableTypeInternal(path[0], path[1], path[2], type);
	}

	@Override
	@NoExport
	public boolean exists()
	{
		return existsInternal(path[0], path[1], path[2]);
	}

	private final native boolean existsInternal(String parentScope, String scope, String vName) /*-{
		var scp = $wnd._ServoyInit_[parentScope][scope];
		if (scp && scp.vrbs[vName]) {
			return true;
		}
		return false;
	}-*/;

	private final native String getDefaultValueInternal(String parentScope, String scope, String vName) /*-{
		return $wnd._ServoyInit_[parentScope][scope].vrbs[vName][0];
	}-*/;

	private final native void setDefaultValueInternal(String parentScope, String scope, String vName, String defValueStr) /*-{
		$wnd._ServoyInit_[parentScope][scope].vrbs[vName][0] = defValueStr;
	}-*/;

	private final native int getVariableTypeInternal(String parentScope, String scope, String vName) /*-{
		return $wnd._ServoyInit_[parentScope][scope].vrbs[vName][1];
	}-*/;

	private final native void setVariableTypeInternal(String parentScope, String scope, String vName, int type) /*-{
		$wnd._ServoyInit_[parentScope][scope].vrbs[vName][1] = type;
	}-*/;

	private final native void createVarInternal(String parentScope, String scope, String vName, int type) /*-{
		$wnd._ServoyUtils_.defineWindowVariable(vName, true);
		$wnd._ServoyInit_[parentScope][scope].vrbs[vName] = [ "null", type ];
	}-*/;

	private final native void removeInternal(String parentScope, String scope, String vName) /*-{
		delete $wnd._ServoyInit_[parentScope][scope].vrbs[vName];
	}-*/;

	@NoExport
	public void create(int type)
	{
		createVarInternal(path[0], path[1], path[2], type);
		reloadScope();
	}

	@Override
	public boolean remove()
	{
		if (exists())
		{
			removeInternal(path[0], path[1], path[2]);
			reloadScope();
			return true;
		}
		return false;
	}

}
