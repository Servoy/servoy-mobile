package com.servoy.mobile.client.ui;

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

import java.util.Comparator;

import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.util.Utils;

/**
 * Utility class for position compare
 * 
 * @author gboros
 */
public class PositionComparator
{
	public static final Comparator<Component> XY_COMPARATOR = new PositionJSComponentComparator(true);
	public static final Comparator<Component> YX_COMPARATOR = new PositionJSComponentComparator(false);

	public static class PositionJSComponentComparator implements Comparator<Component>
	{
		private final boolean xy;

		PositionJSComponentComparator(boolean xy)
		{
			this.xy = xy;
		}

		public int compare(Component o1, Component o2)
		{
			return comparePoint(xy, splitIntegers(o1.getLocation()), splitIntegers(o2.getLocation()));
		}
	}

	public static int comparePoint(boolean xy, int[] p1, int[] p2)
	{
		if (p1 != null && p2 != null)
		{
			int diff = xy ? (p1[0] - p2[0]) : (p1[1] - p2[1]);
			if (diff == 0)
			{
				diff = xy ? (p1[1] - p2[1]) : (p1[0] - p2[0]);
			}
			return diff;
		}
		if (p1 == null)
		{
			return p2 == null ? 0 : -1;
		}
		return 1;
	}

	public static int[] splitIntegers(String dimensionString)
	{
		if (dimensionString != null)
		{
			int[] xy = Utils.splitAsIntegers(dimensionString);
			if (xy != null && xy.length == 2) return xy;
		}

		return null;
	}
}
