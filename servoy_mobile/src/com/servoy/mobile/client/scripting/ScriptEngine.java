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

package com.servoy.mobile.client.scripting;

import java.util.HashMap;

import com.google.gwt.core.client.JsArrayString;
import com.servoy.mobile.client.MobileClient;

/**
 * Class responsible for setting up and managing the solution's javascript environment.
 * @author acostescu
 */
public class ScriptEngine
{

	public static final String SCOPES = "scopes"; //$NON-NLS-1$
	public static final String FORMS = "forms"; //$NON-NLS-1$

	private final MobileClient application;

	private final HashMap<String, GlobalScope> scopes = new HashMap<String, GlobalScope>();
	private final GlobalScopeModificationDelegate globalScopeModificationDelegate = new GlobalScopeModificationDelegate();

	public ScriptEngine(MobileClient application)
	{
		this.application = application;

		new JSApplication(application);
		new PluginsScope(application);
		new JSDatabaseManager(application.getFoundSetManager());
		new JSUtils(application);
		new JSSecurity(application);
		new JSI18N(application.getI18nProvider());
		export();
	}

	public GlobalScope getGlobalScope(String name)
	{
		GlobalScope scope = scopes.get(name);
		if (scope == null)
		{
			scope = createNewGlobalScope(name);
			initScope(SCOPES, name, scope, null);
		}
		return scope;
	}

	private GlobalScope createNewGlobalScope(String name)
	{
		GlobalScope scope = new GlobalScope(name, application);
		scopes.put(name, scope);
		scope.addModificationListener(globalScopeModificationDelegate);
		return scope;
	}

	public static native void initScope(String scopeParent, String scopeName, Scope scopeToInit, Scope oldScope)
	/*-{
		if ($wnd._ServoyInit_[scopeParent][scopeName]._sv_init) {
			$wnd._ServoyInit_[scopeParent][scopeName]._sv_init(scopeToInit,
					oldScope);
		} else {
			$wnd._ServoyInit_.initScope(scopeParent, scopeName, scopeToInit,
					oldScope);
		}
	}-*/;

	public static final native JsArrayString getFunctionNamesInternal(String parentScope, String scope)
	/*-{
		var keys = [];
		var scp = $wnd._ServoyInit_[parentScope][scope];
		for ( var k in scp._sv_fncs)
			keys.push(k);
		var tmpScope = new Object();
		scp._sv_init(tmpScope, null, true);
		for ( var k in tmpScope)
			if (k != '_destroyCallbackFunctions')
				keys.push(k);
		return keys;
	}-*/;

	public static final native JsArrayString getVariableNamesInternal(String parentScope, String scope) /*-{
		var keys = [];
		for ( var k in $wnd._ServoyInit_[parentScope][scope]._sv_vrbs)
			keys.push(k);
		return keys;
	}-*/;

	public static final native JsArrayString getScopeNamesInternal(String parentScope) /*-{
		var keys = [];
		for ( var k in $wnd._ServoyInit_[parentScope])
			keys.push(k);
		return keys;
	}-*/;

	public static final native void clearScope(Scope scope)
	/*-{
		$wnd._ServoyUtils_.clearScope(scope);
	}-*/;

	public GlobalScopeModificationDelegate getGlobalScopeModificationDelegate()
	{
		return globalScopeModificationDelegate;
	}

	private native void export()
	/*-{
		$wnd._ServoyUtils_.scriptEngine = this;
		$wnd._ServoyUtils_.getGlobalScope = function(name) {
			return $wnd._ServoyUtils_.scriptEngine.@com.servoy.mobile.client.scripting.ScriptEngine::getGlobalScope(Ljava/lang/String;)(name);
		}
		$wnd._ServoyUtils_.setScopeVariableType = function(scope, name, type) {
			return scope.@com.servoy.mobile.client.scripting.Scope::setVariableType(Ljava/lang/String;I)(name,type);
		}
		$wnd._ServoyUtils_.getScopeVariable = function(scope, name) {
			var type = scope.@com.servoy.mobile.client.scripting.Scope::getVariableType(Ljava/lang/String;)(name);
			if (type == 8 || type == 4) {
				var value = scope.@com.servoy.mobile.client.scripting.Scope::getVariableNumberValue(Ljava/lang/String;)(name);
				return isNaN(value) ? null : value;
			} else if (type == 93) {
				return scope.@com.servoy.mobile.client.scripting.Scope::getVariableDateValue(Ljava/lang/String;)(name);
			} else if (type == 16) { // boolean
				return scope.@com.servoy.mobile.client.scripting.Scope::getVariableBooleanValue(Ljava/lang/String;)(name);
			}
			return scope.@com.servoy.mobile.client.scripting.Scope::getVariableValue(Ljava/lang/String;)(name);
		}
		$wnd._ServoyUtils_.setScopeVariable = function(scope, name, value) {
			var type = scope.@com.servoy.mobile.client.scripting.Scope::getVariableType(Ljava/lang/String;)(name);
			if (typeof value == "number" || type == 8 || type == 4) {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableNumberValue(Ljava/lang/String;D)(name,value);
			} else if (type == 93) {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableDateValue(Ljava/lang/String;Lcom/google/gwt/core/client/JsDate;)(name,value);
			} else if (typeof value == "boolean") {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableBooleanValue(Ljava/lang/String;Z)(name,value);
			} else {
				scope.@com.servoy.mobile.client.scripting.Scope::setVariableValue(Ljava/lang/String;Ljava/lang/Object;)(name,value);
			}
		}
		$wnd._ServoyInit_.init();
	}-*/;

	public void reloadScopeIfInitialized(String scopeName)
	{
		GlobalScope oldGs;
		if ((oldGs = scopes.remove(scopeName)) != null)
		{
			GlobalScope newGs = createNewGlobalScope(scopeName);
			initScope(SCOPES, scopeName, newGs, oldGs);
		}
	}

}
