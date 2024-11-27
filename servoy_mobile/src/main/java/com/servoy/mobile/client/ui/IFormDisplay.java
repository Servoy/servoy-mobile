package com.servoy.mobile.client.ui;

import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.angular.JsPlainObj;
import com.servoy.mobile.client.dataprocessing.Record;

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

/**
 * Form display interface
 * @author gboros
 */
public interface IFormDisplay
{

	FormController getFormController();

	void refreshRecord(Record record);

	void cleanup();

	boolean isShow();

	void setVisible(boolean visible);

	Object convertServerValue(final String key, final Object value, WebRuntimeComponent component);

	Object convertClientValue(final String key, final Object value, WebRuntimeComponent component);

	/**
	 * @param beanName
	 */
	WebRuntimeComponent getComponent(String beanName);

	/**
	 * @param formData
	 */
	void sendComponentData(JsPlainObj formData);

	Object sendApiCall(WebRuntimeComponent webRuntimeComponent, String key, Object[] args, ApiSpec api);

	/**
	 * @param component
	 * @param key
	 */
	void pushChanges(WebRuntimeComponent component, String key);
}
