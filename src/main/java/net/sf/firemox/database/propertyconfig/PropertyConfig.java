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
package net.sf.firemox.database.propertyconfig;

import net.sf.firemox.database.Proxy;
import net.sf.firemox.database.data.StringData;
import net.sf.firemox.database.data.TranslatableData;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * Abstract class that holds information about a property name and its
 * translation in the selected language in the <code>LanguageManager</code>
 * class.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public abstract class PropertyConfig {

	/**
	 * The translated name of this property.
	 */
	private String translatedName;

	/**
	 * The name of this property.
	 */
	private String name;

	/**
	 * Default protected constructor.
	 * 
	 * @param name
	 *          the identifier or name of this property
	 */
	protected PropertyConfig(String name) {
		this.name = name;
	}

	/**
	 * Indicates if the value of this property is translated or not.
	 * 
	 * @return <code>true</code> if the value of this property is translated,
	 *         <code>false</code> either
	 */
	abstract boolean isTranslated();

	/**
	 * Returns the translated property name.
	 * 
	 * @return the translated property name
	 */
	public String getTranslatedName() {
		if (translatedName == null) {
			translatedName = LanguageManagerMDB.getString(name);
		}
		return translatedName;
	}

	/**
	 * Returns the name of this property.
	 * 
	 * @return the name of this property
	 */
	public String getName() {
		return name;
	}

	@Override
	public final int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "[name=" + name + ",local=" + translatedName + "]";
	}

	/**
	 * Parse the given stream to build the associated TranslatableData object to
	 * the specified card name.
	 * 
	 * @param cardName
	 *          the card name.
	 * @param node
	 *          the node containing the data used to build the
	 *          {@link TranslatableData} object.
	 * @return the {@link TranslatableData} object built from the given node.
	 */
	public TranslatableData parseProperty(String cardName, Node node) {
		return new StringData(this, node.getAttribute("value"));
	}

	/**
	 * Parse the given stream to build the associated TranslatableData object to
	 * the specified card name.
	 * 
	 * @param cardName
	 *          the card name.
	 * @param stream
	 *          the stream containing the data used to build the
	 *          {@link TranslatableData} object.
	 * @param proxy
	 *          is the proxy this data come from. Is used to translate from
	 *          private-proxy to public-tbs value.
	 * @return the {@link TranslatableData} object built from the given stream.
	 */
	public TranslatableData parseProperty(String cardName, String stream,
			Proxy proxy) {
		return new StringData(this, proxy
				.getGlobalValueFromLocal(getName(), stream));
	}
}
