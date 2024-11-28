package com.servoy.mobile.client.persistence;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.constants.IRepositoryConstants;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.util.Utils;

/**
 * Runtime abstraction of the solution. Wraps around the actual solution for added functionality.
 * @author acostescu
 */
public class FlattenedSolution
{
	private final Solution solution;
	private HashMap<String, Form> formCacheByName = null;
	private ArrayList<Form> copyForms; // SolutionModel does it's changes on copies
	private ArrayList<String> removedForms; // via SolutionModel (actually they are just hidden)

	public FlattenedSolution(Solution solution)
	{
		this.solution = solution;
	}

	public Collection<Form> getForms()
	{
		if (formCacheByName == null) refreshFormCache();
		return formCacheByName.values();
	}

	public Form getForm(String name)
	{
		if (formCacheByName == null) refreshFormCache();
		return formCacheByName.get(name);
	}

	public Form getFormByUUID(String uuid)
	{
		if (formCacheByName == null) refreshFormCache();
		for (Form f : formCacheByName.values())
		{
			if (f.getUUID().equals(uuid)) return f;
		}
		return null;
	}

	public Form getFirstForm()
	{
		Form f = solution.getForm(0);
		// it shouldn't have been removed with SM; but it might have been modified
		return getForm(f.getName());
	}

	public Form newForm(String name, String dataSource, int width, int height)
	{
		Form f = solution.instantiateForm(name, Utils.createStringUUID(), IRepositoryConstants.FORMS);
		f.markAsCopy();

		// prepare js access to it and method/variable usage
		prepareFormScopeLoading(f.getName());
		setFormScopeInitialization(f.getName(), null);

//		f.setSize("380, 100"); //$NON-NLS-1$
		f.getOrCreateMobileProperties().setPropertyValue(IMobileProperties.MOBILE_FORM, Boolean.TRUE);
		f.setDataSource(dataSource);
		f.setSize(width + "," + height);

		// remember as non-original form
		storeAsCopyForm(f);
		return f;
	}

	public ValueList newValueList(String name)
	{
		return solution.instantiateValueList(name, Utils.createStringUUID());
	}

	public void removeValueList(ValueList valuelist)
	{
		solution.removeValueList(valuelist);
	}

	public Form cloneFormDeep(Form formToClone)
	{
		Form f = formToClone.cloneDeep();

		// prepare function/method usage
//		prepareFormScopeLoading(f.getName(), true); // this is not needed as an original for already exists so it's prepared
		setFormScopeInitialization(f.getName(), Utils.cloneDeep(getScriptEntityForForm(f.getName())));

		storeAsCopyForm(f);
		return f;
	}

	private void storeAsCopyForm(Form f)
	{
		if (copyForms == null) copyForms = new ArrayList<Form>();
		copyForms.add(f);
//		formCacheByName.put(f.getName(), f);
		refreshFormCache();
	}

	public void removeFormOrCopy(Form f)
	{
		if (copyForms != null && copyForms.remove(f))
		{
			// a modified copy of an original form
			if (solution.getForm(f.getName()) != null) storeRemovedForm(f);
			removeFormScope(f.getName());
//			formCacheByName.remove(f.getName());
			refreshFormCache();
		}
		else
		{
			Form frm = getForm(f.getName());
			if (frm != null)
			{
				storeRemovedForm(f);
//				formCacheByName.remove(f.getName());
				refreshFormCache();
			}
		}
	}

	private void storeRemovedForm(Form f)
	{
		if (removedForms == null) removedForms = new ArrayList<String>();
		removedForms.add(f.getName());
	}

	public void revertForm(String formName)
	{
		Form f = getForm(formName);
		if (f != null && copyForms != null && copyForms.remove(f))
		{
			// a modified copy of an original form
			refreshFormCache();
			removeFormScope(formName);
			if (getForm(formName) == null)
			{
				throw new RuntimeException("Can't revert form '" + formName + "' to original, because there is no original."); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		else if (removedForms != null && removedForms.remove(formName))
		{
			// it's an original form
			refreshFormCache();
		}
	}

	private void refreshFormCache()
	{
		if (formCacheByName == null) formCacheByName = new HashMap<String, Form>();
		else formCacheByName.clear();

		for (int i = solution.formCount() - 1; i >= 0; i--)
		{
			Form f = solution.getForm(i);
			formCacheByName.put(f.getName(), f);
		}

		// cloned forms have priority
		if (copyForms != null)
		{
			for (Form f : copyForms)
			{
				formCacheByName.put(f.getName(), f);
			}
		}

		if (removedForms != null)
		{
			for (String fn : removedForms)
				formCacheByName.remove(fn);
		}
	}

	public final native void removeFormScope(String formName) /*-{
		try {
			delete $wnd.forms[formName]; // this does not evaluate (doesn't load the form if it's not there); original forms cannot be removed like this
		} finally {
		}
		if ($wnd._ServoyInit_.smForms) {
			if ($wnd._ServoyInit_.forms[formName]) {
				if ($wnd._ServoyInit_.smForms[formName]) {
					// so it's an original form with backup; restore scripts methods and vars
					$wnd._ServoyInit_.forms[formName] = $wnd._ServoyInit_.smForms[formName];
				} else {
					// form was created by SM; remove clutter
					delete $wnd._ServoyInit_.forms[formName];
				}
			}
			delete $wnd._ServoyInit_.smForms[formName];
		}
	}-*/;

	private final native void prepareFormScopeLoading(String formName) /*-{
		Object.defineProperty($wnd.forms, formName, {
			get : function() {
				return $wnd._ServoyUtils_.getFormScope(formName);
			},
			configurable : true
		});
	}-*/;

	private final native void setFormScopeInitialization(String formName, JavaScriptObject formScriptEntity) /*-{
		if (!$wnd._ServoyInit_.smForms)
			$wnd._ServoyInit_.smForms = {};
		$wnd._ServoyInit_.smForms[formName] = formScriptEntity; // can be null for SM created forms; otherwise it's a backup
		if (formScriptEntity == null) {
			$wnd._ServoyInit_.forms[formName] = {
				_sv_fncs : {},
				_sv_vrbs : {}
			};
		}
	}-*/;

	private final native JavaScriptObject getScriptEntityForForm(String formName) /*-{
		return $wnd._ServoyInit_.forms[formName];
	}-*/;

	public int relationCount()
	{
		return solution.relationCount();
	}

	/**
	 * This one shouldn't be called from here normally for the ui, you have to use {@link RelationDescription} instances
	 */
	public Relation getRelation(int i)
	{
		return solution.getRelation(i);
	}

	public String getSolutionName()
	{
		return solution.getSolutionName();
	}

	public String getServiceSolutionName()
	{
		return solution.getServiceSolutionName();
	}


	public String getOnSolutionOpen()
	{
		return solution.getOnSolutionOpen();
	}

	public String getLoginForm()
	{
		return solution.getLoginForm();
	}

	public String getServerUrl()
	{
		return solution.getServerUrl();
	}

	public int getTimeout()
	{
		return solution.getTimeout();
	}

	public boolean getSkipConnect()
	{
		return solution.getSkipConnect();
	}

	public void setSkipConnect(boolean skip)
	{
		solution.setSkipConnect(skip);
	}

	public boolean getMustAuthenticate()
	{
		return solution.getMustAuthenticate();
	}

	public String getStyleSheet()
	{
		return solution.getStyleSheet();
	}

	public String getI18nValue(String key)
	{
		return solution.getI18nValue(key);
	}

	public void setI18nValue(String key, String value)
	{
		solution.setI18nValue(key, value);
	}

	/**
	 * This one shouldn't be called from here normally for the ui, you have to use {@link RelationDescription} instances
	 */
	public Relation getRelation(String name)
	{
		return solution.getRelation(name);
	}

	public ValueList getValueListByUUID(String valuelistID)
	{
		return solution.getValueListByUUID(valuelistID);
	}

	public ValueList getValueList(String name)
	{
		return solution.getValueList(name);
	}

	public int valuelistCount()
	{
		return solution.valuelistCount();
	}

	public ValueList getValueList(int i)
	{
		return solution.getValueList(i);
	}

}
