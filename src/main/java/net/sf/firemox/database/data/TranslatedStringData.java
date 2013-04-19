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

/**
 * Represents a simple translated data, which is a name associated to a
 * <code>String</code> value. The <code>isTranslated()</code> method always
 * returns <code>true</code>.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class TranslatedStringData extends StringData {
	/**
	 * The translated value of this data. Equals to <code>null</code> until is
	 * translated.
	 */
	private String translatedValue;

	/**
	 * Default public constructor.
	 * 
	 * @param config
	 *          the property configuration of this data to use
	 * @param value
	 *          the value of this data
	 */
	public TranslatedStringData(PropertyConfig config, String value) {
		super(config, value);
	}

	@Override
	public String getTranslatedValue(Proxy proxy) {
		if (translatedValue == null) {
			if (proxy == null) {
				translatedValue = LanguageManagerMDB.getString(getPropertyName() + "-"
						+ getValue());
			} else {
				translatedValue = LanguageManagerMDB.getString(getPropertyName() + "-"
						+ proxy.getGlobalValueFromLocal(getPropertyName(), getValue()));
			}
		}
		return translatedValue;
	}
}
