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

import com.servoy.base.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.scripting.ScriptEngine;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public class JSMethod extends JSScriptPart implements IBaseSMMethod, Exportable
{

	public JSMethod(String parentScopeName, String scopeName, String name, JSSolutionModel model, JSForm parentForm)
	{
		super(parentScopeName, scopeName, name, model, parentForm);
	}

	@Getter
	@Override
	public String getCode()
	{
		String codeAndArgs = getArgsAndCodeInternal(path[0], path[1], path[2]);

		if (codeAndArgs != null)
		{
			String wholeTextBeforeArgsInternal = getWholeTextBeforeArgsInternal(path[0], path[1], path[2]);
			if (wholeTextBeforeArgsInternal == null) wholeTextBeforeArgsInternal = "function " + getName(); // for non-SM created methods

			codeAndArgs = joinIntoFullCode(codeAndArgs, wholeTextBeforeArgsInternal);
		}
		return codeAndArgs;
	}

	@NoExport
	public static String joinIntoFullCode(String codeWithoutArgs, String wholeTextBeforeArgsInternal)
	{
		return wholeTextBeforeArgsInternal + codeWithoutArgs;
	}

	/**
	 * @return 3 items array, first being arguments + code, second is name, third is the whole string before arguments.
	 */
	@NoExport
	public static String[] splitFullCode(String c)
	{
		int idx1 = c.indexOf("function "); //$NON-NLS-1$
		int idx2 = c.indexOf("("); //$NON-NLS-1$

		if (idx1 >= 0 && idx2 >= 0) return new String[] { c.substring(idx2), c.substring(idx1 + 9, idx2).trim(), c.substring(0, idx2) };
		return null;
	}

	@Setter
	@Override
	public void setCode(String content)
	{
		if (content == null) return;
		cloneFormIfNeeded();
		String[] sr = splitFullCode(content);
		if (sr != null && sr.length == 3)
		{
			setCodeInternal(path[0], path[1], path[2], sr[0], sr[2]);
			reloadScope();
		}
	}

	@Override
	public Object[] getArguments()
	{
		return null; // this is only implemented by JSMethodWithArguments
	}

	@Override
	@NoExport
	public String getReferenceString()
	{
		return super.getReferenceString() + "()"; //$NON-NLS-1$
	}

	@NoExport
	public static JSMethod getMethodFromString(String methodCall, JSForm form, JSSolutionModel solutionModel)
	{
		if (methodCall != null && methodCall.contains("("))
		{
			methodCall = methodCall.substring(0, methodCall.indexOf("(")).trim();
			String[] callParts = methodCall.split("\\.");
			if (callParts.length == 1)
			{
				return new JSMethod(ScriptEngine.FORMS, form.getName(), callParts[0], solutionModel, form);
			}
			else
			{
				return new JSMethod(ScriptEngine.SCOPES, callParts[callParts.length - 2], callParts[callParts.length - 1], solutionModel, null);
			}
		}
		return null;
	}

	@Override
	@NoExport
	public boolean exists()
	{
		return existsInternal(path[0], path[1], path[2]);
	}

	private final native boolean existsInternal(String parentScope, String scope, String fName) /*-{
		var scp = $wnd._ServoyInit_[parentScope][scope];
		if (scp) {
			if (scp._sv_fncs[fName] || scp[fName]) {
				return true;
			}
			if (scp._sv_init) {
				var tmpScope = new Object();
				scp._sv_init(tmpScope, null, true);
				if (tmpScope[fName]) {
					return true;
				}
			}
		}
		return false;
	}-*/;

	private final native String getArgsAndCodeInternal(String parentScope, String scope, String fName) /*-{
		var scp = $wnd._ServoyInit_[parentScope][scope];
		if (scp) {
			var code = scp._sv_fncs[fName];
			if (code)
				return code;
			if (scp._sv_init) {
				var tmpScope = new Object();
				scp._sv_init(tmpScope, null, true);
				if (tmpScope[fName]) {
					return tmpScope[fName].realFunction.toString();
				}
			}
		}
		return;
	}-*/;

	private final native String getWholeTextBeforeArgsInternal(String parentScope, String scope, String fName) /*-{
		return typeof $wnd._ServoyInit_[parentScope][scope].preTxt == 'undefined' ? null
				: $wnd._ServoyInit_[parentScope][scope].preTxt[fName];
	}-*/;

	private final native void setCodeInternal(String parentScope, String scope, String fName, String argsAndCode, String wholeTextBeforeArgs) /*-{
		$wnd._ServoyInit_[parentScope][scope]._sv_fncs[fName] = argsAndCode;
		if (typeof $wnd._ServoyInit_[parentScope][scope].preTxt == 'undefined')
			$wnd._ServoyInit_[parentScope][scope].preTxt = {};
		$wnd._ServoyInit_[parentScope][scope].preTxt[fName] = wholeTextBeforeArgs;
	}-*/;

	private final native void removeInternal(String parentScope, String scope, String fName) /*-{
		delete $wnd._ServoyInit_[parentScope][scope]._sv_fncs[fName];
		if (typeof $wnd._ServoyInit_[parentScope][scope].preTxt != 'undefined')
			delete $wnd._ServoyInit_[parentScope][scope].preTxt[fName];
	}-*/;

	@NoExport
	public void create(String argsAndCode, String wholeTextBeforeArgs)
	{
		setCodeInternal(path[0], path[1], path[2], argsAndCode, wholeTextBeforeArgs);
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
