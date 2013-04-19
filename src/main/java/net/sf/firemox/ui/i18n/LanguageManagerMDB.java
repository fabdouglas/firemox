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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.token.IdConst;

/**
 * LanguageManager.java Created on 23 janv. 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54.16
 * @since 0.54.17 in case of the main language didn't contain the specified
 * @since 0.54.18 language extension is no more $lang, but .lang to suit to key,
 *        we search in the secondary one linux users, and so sourceforge web
 *        hosting
 */
public final class LanguageManagerMDB {

	/**
	 * Create a new instance of this class.
	 */
	private LanguageManagerMDB() {
		super();
	}

	/**
	 * The resource bundle used for all messages
	 */
	private static ResourceBundle bundle;

	/**
	 * The resource bundle used if the main one did not contain a message in the
	 * user language
	 */
	private static ResourceBundle secondaryBundle;

	/**
	 * @param key
	 * @return the message associated to the key
	 * @since 0.54.17 in case of the main language didn't contain the specified
	 *        key, we search in the secondary one
	 * @since 0.86 in case of the key has not been found in no defined languages,
	 *        the "-[0-9]*-" string of key is replaced by "%n" and the caught
	 *        value is passed as parameter. If the key not found, the original key
	 *        is returned.
	 */
	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			try {
				return secondaryBundle.getString(key);
			} catch (MissingResourceException e2) {
				if (key.matches(".*-[0-9]*-.*")) {
					String res = key.replaceFirst("-[0-9]*-", "-%n-");
					int index = res.indexOf("%n");
					final String value = key
							.substring(index, key.indexOf('-', index + 1));
					return LanguageManagerMDB.getString(res, value);
				} else if (key.matches(".--1-.*")) {
					String res = key.replaceFirst("--1-", "-%n-");
					return LanguageManagerMDB
							.getString(res, MdbLoader.unknownSmlManaHtml);
				} else if (key.matches(".--1-.*")) {
					String res = key.replaceFirst("-x-", "-%n-");
					return LanguageManagerMDB
							.getString(res, MdbLoader.unknownSmlManaHtml);
				}
				return key;
			}
		}
	}

	/**
	 * @param key
	 * @param parameters
	 * @return the message associated to the key
	 * @since 0.85.38 parameters
	 */
	public static String getString(String key, Object... parameters) {
		if (parameters == null || parameters.length == 0) {
			return getString(key);
		}

		// parameters are used
		return MessageFormat.format(getString(key), parameters);
	}

	/**
	 * @param newMdb
	 */
	public static void setMdb(String newMdb) {
		String root = IdConst.TBS_DIR + "/" + newMdb + "/";
		bundle = LanguageManager.getPrimaryBundle(root);
		secondaryBundle = LanguageManager.getSecondaryBundle(root);
	}
}