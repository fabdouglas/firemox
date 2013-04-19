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

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54.16
 * @since 0.54.17 in case of the main language didn't contain the specified
 * @since 0.54.18 language extension is no more $lang, but .lang to suit to key,
 *        we search in the secondary one linux users, and so sourceforge web
 *        hosting
 */
public final class LanguageManager {

	/**
	 * Create a new instance of this class.
	 */
	private LanguageManager() {
		super();
	}

	/**
	 * The root directory where resource bundles are located.
	 */
	public static final String PATH_ROOT = "";

	/**
	 * The default language used is case of error or undefined message if the user
	 * language
	 */
	public static final String DEFAULT_LANGUAGE = "en";

	/**
	 * The local language.
	 */
	public static final String LOCALE_LANGUAGE = Locale.getDefault()
			.getLanguage();

	/**
	 * The language suffix string
	 */
	public static final String LANGUAGE_SUFFIX = "-lang";

	/**
	 * The language extension
	 */
	public static final String LANGUAGE_EXTENSION = ".properties";

	/**
	 * The current language name
	 */
	private static Language language;

	/**
	 * Represents the available languages and their specifications
	 */
	public static Set<Language> languages;

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
	 * Return the current language name.
	 * 
	 * @return the current language name.
	 */
	public static Language getLanguage() {
		return language;
	}

	/**
	 * Load the preferred language, and set the language field
	 * 
	 * @param languageName
	 *          the language name to use.
	 */
	public static void initLanguageManager(String languageName) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(LANGUAGE_SUFFIX + LANGUAGE_EXTENSION);
			}
		};
		if (languages != null)
			// I18N settings have already been set
			return;
		languages = new HashSet<Language>();
		final String[] languages = new File(MToolKit.getRootDir()).list(filter);
		for (String language : languages) {
			language = language.split("\\" + LANGUAGE_SUFFIX + "\\"
					+ LANGUAGE_EXTENSION)[0];
			ResourceBundle res = ResourceBundle.getBundle(language + LANGUAGE_SUFFIX);
			LanguageManager.languages.add(new Language(language, language, res
					.getString("LanguageName"), res.getString("Author"), res
					.getString("MoreInfo"), res.getString("Version")));
		}
		LanguageManager.languages.add(new Language("auto", LOCALE_LANGUAGE,
				"Default", "", "", ""));
		setLanguage(languageName);
		bundle = getPrimaryBundle(PATH_ROOT);
		secondaryBundle = getSecondaryBundle(PATH_ROOT);
	}

	/**
	 * Return a bundle corresponding to the specified language in the root. If
	 * there is bundle suiting to the language, it returns the English one.
	 * 
	 * @param root
	 *          directory where the looked for bundle is located.
	 * @return a bundle corresponding to the specified language.
	 */
	public static ResourceBundle getPrimaryBundle(String root) {
		try {
			return ResourceBundle.getBundle(root + language.getLocale()
					+ LANGUAGE_SUFFIX);
		} catch (MissingResourceException e) {
			// as default, we return the English language
			return ResourceBundle.getBundle(DEFAULT_LANGUAGE + LANGUAGE_SUFFIX);
		}
	}

	/**
	 * Return resource bundle used if the main one did not contain a message in
	 * the user language
	 * 
	 * @param root
	 *          directory where the looked for bundle is located.
	 * @return resource bundle used if the main one did not contain a message in
	 *         the user language
	 */
	public static ResourceBundle getSecondaryBundle(String root) {
		if (DEFAULT_LANGUAGE == language.getLocale()) {
			return bundle;
		}
		return ResourceBundle.getBundle(root + DEFAULT_LANGUAGE + LANGUAGE_SUFFIX);
	}

	/**
	 * return the localized message.
	 * 
	 * @param key
	 *          the message key.
	 * @return the message associated to the key
	 * @since 0.54.17 in case of the main language didn't contain the specified
	 *        key, we search in the secondary one
	 */
	public static String getString(String key) {
		String value = getNullString(key);
		if (value != null)
			return value;

		if (secondaryBundle == null)
			return '!' + key + '!';

		try {
			return secondaryBundle.getString(key);
		} catch (MissingResourceException ex) {
			return '!' + key + '!';
		}
	}

	/**
	 * Return the localized message.
	 * 
	 * @param key
	 *          the message key.
	 * @return the message associated to the key.<code>null</code> if the key
	 *         has not been found.
	 */
	public static String getNullString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	/**
	 * @param key
	 *          the message key.
	 * @param parameters
	 *          the parameters to add to resourceBudle.
	 * @return the message associated to the key with replaced parameters.
	 */
	public static String getString(String key, Object... parameters) {
		// parameter is used
		return MessageFormat.format(getString(key), parameters);
	}

	/**
	 * Set the language to use.
	 * 
	 * @param languageName
	 *          the new language to use
	 */
	public static void setLanguage(String languageName) {
		for (Language language : languages) {
			if (language.getKey().equalsIgnoreCase(languageName)) {
				LanguageManager.language = language;
				break;
			}
		}
	}

}