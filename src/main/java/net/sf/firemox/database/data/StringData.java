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

import net.sf.firemox.database.Proxy;
import net.sf.firemox.database.propertyconfig.PropertyConfig;

/**
 * Represents a simple translatable data, which is a name associated to a
 * <code>String</code> value. The <code>isTranslated()</code> method always
 * returns <code>false</code>.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class StringData extends TranslatableData {
	/**
	 * The translated value of this data. Equals to <code>null</code> until is
	 * translated.
	 */
	private final String value;

	/**
	 * Default public constructor.
	 * 
	 * @param config
	 *          the property configuration of this data to use
	 * @param value
	 *          the value of this data
	 */
	public StringData(PropertyConfig config, String value) {
		super(config);
		if (value != null) {
			this.value = value.replaceAll("/&gt;", "/").replaceAll("/>", "/");
		} else {
			this.value = null;
		}
	}

	@Override
	public String getTranslatedValue(Proxy proxy) {
		return value;
	}

	@Override
	public String getValue() {
		return value;
	}
}
