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
package net.sf.firemox.ui.i18n;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
public class Language {

	/**
	 * The Java Locale language name. Like fr, en,...
	 */
	private final String locale;

	/**
	 * The language key name.
	 */
	private final String key;

	/**
	 * The language display name. Like Francais, English,...
	 */
	private final String name;

	/**
	 * Author name of the translation.
	 */
	private final String author;

	/**
	 * Additional information about this translation.
	 */
	private final String moreInfo;

	/**
	 * The tested version for this translation.
	 */
	private final String version;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param key
	 *          The language key name.
	 * @param locale
	 *          The Java Locale language name. Like fr, en,...
	 * @param name
	 *          The language display name. Like Francais, English,...
	 * @param author
	 *          Author name of the translation.
	 * @param moreInfo
	 *          Additional information about this translation.
	 * @param version
	 *          The tested version for this translation.
	 */
	public Language(String key, String locale, String name, String author,
			String moreInfo, String version) {
		this.key = key;
		this.locale = locale;
		this.name = name;
		this.author = author;
		this.moreInfo = moreInfo;
		this.version = version;
	}

	/**
	 * Returns author name of the translation.
	 * 
	 * @return Author name of the translation.
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * Returns additional information about this translation.
	 * 
	 * @return Additional information about this translation.
	 */
	public String getMoreInfo() {
		return this.moreInfo;
	}

	/**
	 * Returns the Java Locale language name. Like fr, en,...
	 * 
	 * @return the Java Locale language name. Like fr, en,...
	 */
	public String getLocale() {
		return this.locale;
	}

	/**
	 * Returns the language key name.
	 * 
	 * @return the language key name.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Returns the language display name. Like Francais, English,...
	 * 
	 * @return the language display name. Like Francais, English,...
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the tested version for this translation.
	 * 
	 * @return the tested version for this translation.
	 */
	public String getVersion() {
		return this.version;
	}

	@Override
	public String toString() {
		return getKey();
	}
}
