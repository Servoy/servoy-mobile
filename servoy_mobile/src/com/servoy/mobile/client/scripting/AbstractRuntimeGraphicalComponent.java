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

import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.servoy.base.scripting.api.IJSEvent;
import com.servoy.base.util.ITagResolver;
import com.servoy.base.util.TagParser;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.ui.Executor;
import com.servoy.mobile.client.ui.IGraphicalComponent;
import com.servoy.mobile.client.ui.TapHandlerForPageSwitchWithBlur;
import com.servoy.mobile.client.util.Utils;
import com.sksamuel.jqm4gwt.events.TapEvent;

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
			String format = componentPersist.getFormat();
			if (format != null && output != null)
			{
				if (output instanceof Number)
				{
					txt = NumberFormat.getFormat(format).format((Number)output);
				}
				else if (output instanceof Date)
				{
					txt = DateTimeFormat.getFormat(format).format((Date)output);
				}
				else
				{
					Log.error("Format is set: " + format + ", on a not supported object: " + output + " class:" + output.getClass()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					txt = output.toString();
				}
			}
			else
			{
				txt = output != null ? output.toString() : ""; //$NON-NLS-1$
			}
		}

		if (!Utils.equalObjects(txt, component.getText())) component.setText(txt);
		// otherwise don't replace innerText of tag - because if this is a click (on this same component - exactly on the visible text)
		// that generated onBlur on another field that generated setText on this component - then onClick is no longer called on Chrome/Safari...
	}

	public void setActionCommand(final String command)
	{
		if (command != null)
		{
			addHandlerRegistration(component.addTapHandler(new TapHandlerForPageSwitchWithBlur()
			{
				@Override
				public void onTapAfterBlur(TapEvent event)
				{
					executor.fireEventCommand(IJSEvent.ACTION, command, AbstractRuntimeGraphicalComponent.this, null);
				}
			}));
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		tagResolver = null;
	}
}
