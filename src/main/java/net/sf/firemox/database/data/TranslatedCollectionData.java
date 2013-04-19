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
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a collection of translated data which is a name associated to a
 * table of <code>String</code> objects. The <code>isTranslated()</code>
 * method always returns <code>true</code>.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class TranslatedCollectionData extends CollectionData {
	/**
	 * The translated values of this data. Equals to <code>null</code> until is
	 * translated.
	 */
	private String translatedValue;

	/**
	 * Default public constructor.
	 * 
	 * @param propertyConfig
	 *          the property configuration of this data to use
	 * @param values
	 *          the values of this data
	 */
	public TranslatedCollectionData(PropertyConfig propertyConfig,
			String... values) {
		super(propertyConfig, values);
	}

	@Override
	public String getTranslatedValue(Proxy proxy) {
		if (translatedValue == null) {
			StringBuilder res = new StringBuilder(values.length * 20);
			for (String value : values) {
				if (res.length() != 0) {
					res.append(", ");
				}
				if (proxy == null) {
					res.append(LanguageManagerMDB.getString(StringUtils
							.chop(getPropertyName())
							+ "-" + value.trim()));
				} else {
					res.append(LanguageManagerMDB.getString(StringUtils
							.chop(getPropertyName())
							+ "-"
							+ proxy.getGlobalValueFromLocal(StringUtils
									.chop(getPropertyName()), value.trim())));
				}
			}
			translatedValue = res.toString();
		}
		return translatedValue;
	}

}
