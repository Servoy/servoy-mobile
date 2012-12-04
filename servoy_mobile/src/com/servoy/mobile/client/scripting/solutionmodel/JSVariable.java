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

import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMVariable;

/**
 * @author acostescu
 *
 */
public class JSVariable extends JSScriptPart implements IBaseSMVariable, Exportable
{

	public JSVariable(String parentScopeName, String scopeName, String name, JSSolutionModel model)
	{
		super(parentScopeName, scopeName, name, model);
	}

	@Override
	public String getDefaultValue()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultValue(String arg)
	{
		// TODO ac Auto-generated method stub

	}

	@Override
	public void setName(String name)
	{
		// TODO ac Auto-generated method stub

	}

	@Override
	public int getVariableType()
	{
		// TODO ac Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSMVariable#setVariableType(int)
	 */
	@Override
	public void setVariableType(int arg)
	{
		// TODO ac Auto-generated method stub

	}

	@Override
	@NoExport
	public boolean exists()
	{
		return existsInternal(path[0], path[1], path[2]);
	}

	@NoExport
	public final native boolean existsInternal(String parentScope, String scope, String fName) /*-{
		var scp = $wnd._ServoyInit_[parentScope][scope];
		if (scp && scp.fncs[fName]) {
			return true;
		}
		return false;
	}-*/;

	@NoExport
	public final native String getCodeInternal(String parentScope, String scope, String fName) /*-{
		return $wnd._ServoyInit_[parentScope][scope].fncs[fName].toString();
	}-*/;

	@NoExport
	public final native void setCodeInternal(String parentScope, String scope, String fName, String code) /*-{
		$wnd._ServoyInit_[parentScope][scope].fncs[fName] = eval("(" + code
				+ ")");
	}-*/;

}
