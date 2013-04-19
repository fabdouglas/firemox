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
 * Represents a translatable data. A data contains a key and a value. The key is
 * always translated, but the value may not.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public abstract class TranslatableData {

	/**
	 * The property configuration of this data.
	 */
	protected PropertyConfig propertyConfig;

	/**
	 * Default public constructor.
	 * 
	 * @param propertyConfig
	 *          the property configuration of this data to use
	 */
	public TranslatableData(PropertyConfig propertyConfig) {
		this.propertyConfig = propertyConfig;
	}

	/**
	 * Returns the translated property name. The translation is done only during
	 * the first call.
	 * 
	 * @return the translated property name
	 */
	public final String getTranslatedPropertyName() {
		return propertyConfig.getTranslatedName();
	}

	/**
	 * Returns the translated value. The translation is done only during the first
	 * call.
	 * 
	 * @param proxy
	 *          is the proxy this data come from. Is used to translate from
	 *          private-proxy to public-tbs value.
	 * @return the translated value
	 */
	public abstract String getTranslatedValue(Proxy proxy);

	/**
	 * The key.
	 * 
	 * @return the key of this data
	 */
	public final String getPropertyName() {
		return propertyConfig.getName();
	}

	/**
	 * The value.
	 * 
	 * @return the value associated to the key of this data
	 */
	public abstract String getValue();

	@Override
	public String toString() {
		return getPropertyName() + "='" + getTranslatedValue(null) + "'";
	}
}
