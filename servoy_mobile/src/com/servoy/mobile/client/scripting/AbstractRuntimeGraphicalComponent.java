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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.servoy.base.scripting.api.IJSEvent;
import com.servoy.base.util.ITagResolver;
import com.servoy.base.util.TagParser;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.ui.Executor;
import com.servoy.mobile.client.ui.IGraphicalComponent;

/**
 * @author gboros
 *
 */
public class AbstractRuntimeGraphicalComponent extends AbstractRuntimeBaseComponent<IGraphicalComponent, GraphicalComponent> implements
	IRuntimeGraphicalComponent
{
	protected ITagResolver tagResolver;

	public AbstractRuntimeGraphicalComponent(MobileClient application, Executor executor, IGraphicalComponent component, GraphicalComponent componentPersist)
	{
		super(application, executor, component, componentPersist);
	}

	@Override
	public boolean needEntireState()
	{
		return componentPersist.getDisplaysTags();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptable#getDataProviderID()
	 */
	@Override
	public String getDataProviderID()
	{
		return componentPersist.getDataProviderID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptable#setTagResolver(com.servoy.j2db.util.ITagResolver)
	 */
	@Override
	public void setTagResolver(ITagResolver resolver)
	{
		this.tagResolver = resolver;
	}

	public void setText(Object output)
	{
		String txt;
		if (componentPersist.getDataProviderID() == null)
		{
			txt = TagParser.processTags(application.getI18nProvider().getI18NMessageIfPrefixed(componentPersist.getText()), tagResolver,
				application.getI18nProvider());
		}
		else
		{
			txt = output != null ? output.toString() : ""; //$NON-NLS-1$
		}

		component.setText(txt);
	}

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			clickRegistration = component.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, AbstractRuntimeGraphicalComponent.this, null);
				}
			});
		}
	}

	private HandlerRegistration clickRegistration = null;

	@Override
	public void destroy()
	{
		super.destroy();
		if (clickRegistration != null)
		{
			clickRegistration.removeHandler();
		}
		tagResolver = null;
	}
}
