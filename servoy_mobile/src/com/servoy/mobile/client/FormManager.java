package com.servoy.mobile.client;

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.solutionmodel.JSForm;
import com.servoy.mobile.client.ui.FormPage;
import com.sksamuel.jqm4gwt.JQMContext;
import com.sksamuel.jqm4gwt.JQMPage;

/**
 * The main form manager, should be subclassed
 * @author jblok
 */
public class FormManager
{
	protected MobileClient application;
	protected LinkedHashMap<String, FormPage> pageMap = new LinkedHashMap<String, FormPage>();
	protected History history = new History();

	protected FormManager(MobileClient mc)
	{
		this.application = mc;
		export();
	}

	protected FormPage getFirstForm()
	{
		JSForm jsForm = application.getSolutionModel().get(0);
		return getForm(jsForm.getName());
	}

	protected FormPage getLoginForm()
	{
		// TODO impl
		return null;
	}

	public void showForm(String formName, Object data)
	{

		FormPage form = getForm(formName);
		if (form != null)
		{
			JQMContext.changePage(form);
		}

	}

	protected FormPage getForm(String name)
	{
		FormPage formPage = pageMap.get(name);
		if (formPage == null)
		{
			JSForm form = application.getSolutionModel().getForm(name);
			formPage = new FormPage(application, form);
			pageMap.put(name, formPage);
		}
		return formPage;
	}

	protected void showForm(String id, FormPage page)
	{
		if (!pageMap.containsKey(id))
		{
			pageMap.put(id, page);
			if (pageMap.size() > 16)
			{
				Iterator<Entry<String, FormPage>> it = pageMap.entrySet().iterator();
				Entry<String, FormPage> entry = it.next();
				it.remove(); //remove first/oldest entry
				FormPage oldPage = entry.getValue();
				removePage(oldPage);
			}
		}
		JQMContext.changePage(page);
	}

	private void removePage(JQMPage oldPage)
	{
		oldPage.removeFromParent();//keep dom small
	}

	public History getHistory()
	{
		return history;
	}

	public class History
	{
		public final native void back()/*-{
			return $wnd.history.back();
		}-*/;

		public void clear()
		{
			removeAllForms();
		}
	}

	void removeAllForms()
	{
		Iterator<FormPage> it = pageMap.values().iterator();
		while (it.hasNext())
		{
			JQMPage oldPage = it.next();
			removePage(oldPage);
		}
		pageMap = new LinkedHashMap<String, FormPage>();
	}

	public void showFirstForm()
	{
		JQMContext.changePage(getFirstForm());
	}

	public void showLoginForm()
	{
		JQMContext.changePage(getFirstForm());
	}

	public FormScope getFormScope(String name)
	{
		FormPage form = getForm(name);
		if (form != null)
		{
			return form.getFormScope();
		}
		return null;
	}

	public native void export() /*-{
		var formManager = this;
		$wnd._ServoyUtils_.getFormScope = function(name) {
			return formManager.@com.servoy.mobile.client.FormManager::getFormScope(Ljava/lang/String;)(name);
		}
	}-*/;
}
