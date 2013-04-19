/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.database.data;

import java.util.Arrays;

import net.sf.firemox.database.Proxy;
import net.sf.firemox.database.propertyconfig.PropertyConfig;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a collection of data which is a name associated to a table of
 * <code>String</code> objects. The <code>isTranslated()</code> method
 * always returns <code>false</code>.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class CollectionData extends TranslatableData {
	/**
	 * The translated values of this data. Equals to <code>null</code> until is
	 * translated.
	 */
	protected String[] values;

	/**
	 * Default public constructor.
	 * 
	 * @param propertyConfig
	 *          the property configuration of this data to use
	 * @param values
	 *          the values of this data
	 */
	public CollectionData(PropertyConfig propertyConfig, String... values) {
		super(propertyConfig);
		this.values = values;
	}

	@Override
	public String getTranslatedValue(Proxy proxy) {
		StringBuilder res = new StringBuilder(values.length * 20);
		for (String value : values) {
			if (res.length() == 0) {
				res.append(", ");
			}
			res.append(value);
		}
		return res.toString();
	}

	@Override
	public String getValue() {
		return StringUtils.chomp(Arrays.toString(values).substring(1), "]");
	}
}
