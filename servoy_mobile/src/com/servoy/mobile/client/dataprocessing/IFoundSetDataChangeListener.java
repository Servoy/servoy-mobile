/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.mobile.client.dataprocessing;


/**
 * This listener is interested in any data(provider) changes in any of the records of a foundset.
 * 
 * @author acostescu
 */
public interface IFoundSetDataChangeListener
{

	/**
	 * Called when the data of a record has changed.
	 * 
	 * @param record the record that was changed.
	 * @param dataProviderID the dataprovider that was changed
	 * @param value the new value of that dataprovider.
	 */
	void recordDataProviderChanged(Record record, String dataProviderID, Object value); // TODO Make a "RecordDataChangedEvent" to bundle these args?

}
