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
package net.sf.firemox.tools;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import net.sf.firemox.token.IdConst;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public final class Configuration {

	/**
	 * The maximum amount of recent properties.
	 */
	private static final int MAX_RECENT_SIZE = 10;

	/**
	 * Create a new instance of this class.
	 */
	private Configuration() {
		super();
	}

	/**
	 * @param property
	 * @return the string value if it exists, empty string otherwise.
	 */
	public static String getString(String property) {
		return getConfiguration().getString(property, "");
	}

	/**
	 * @param property
	 * @return the string value if it exists, empty string otherwise.
	 */
	public static String[] getArray(String property) {
		int maxIndex = Configuration.getConfiguration().getMaxIndex(property);
		String[] res = new String[maxIndex + 1];
		for (int i = 0; i < maxIndex + 1; i++) {
			String item = Configuration.getConfiguration().getString(
					property + "(" + i + ")");
			res[i] = item;
		}
		return res;
	}

	/**
	 * Add a property to the list.
	 * 
	 * @param property
	 * @param pItem
	 */
	public static void addRecentProperty(String property, String pItem) {
		String item = pItem.replace(",", "\\,");
		String[] previous = getArray(property);
		getConfiguration().clearProperty(property);
		getConfiguration().setProperty(property + "(0)", item);
		int index = ArrayUtils.indexOf(previous, item);
		if (index >= 0) {
			for (int i = 0; i < index; i++) {
				getConfiguration().setProperty(property + "(" + i + 1 + ")",
						previous[i]);
			}
			for (int i = index + 1; i < previous.length; i++) {
				getConfiguration().setProperty(property + "(" + i + ")", previous[i]);
			}
		} else {
			final int maxSize;
			if (previous.length > MAX_RECENT_SIZE)
				maxSize = MAX_RECENT_SIZE;
			else
				maxSize = previous.length;
			for (int i = 0; i < maxSize; i++) {
				getConfiguration().setProperty(property + "(" + i + 1 + ")",
						previous[i]);
			}
		}
	}

	/**
	 * @param property
	 * @param defaultValue
	 * @return the string value if it exists, defaultValue otherwise.
	 */
	public static String getString(String property, String defaultValue) {
		return getConfiguration().getString(property, defaultValue);
	}

	/**
	 * @param property
	 * @param defaultValue
	 * @return the boolean value if it exists, defaultValue otherwise.
	 */
	public static Boolean getBoolean(String property, Boolean defaultValue) {
		return getConfiguration().getBoolean(property, defaultValue);
	}

	/**
	 * @param property
	 * @return the boolean value if it exists, null otherwise.
	 */
	public static Boolean getBoolean(String property) {
		try {
			return getConfiguration().getBoolean(property);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * @param property
	 * @param defaultValue
	 * @return the integer value if it exists, defaultValue otherwise.
	 */
	public static Integer getInt(String property, Integer defaultValue) {
		return getConfiguration().getInt(property, defaultValue);
	}

	/**
	 * @param property
	 * @return the integer value if it exists, 0 otherwise.
	 */
	public static Integer getInt(String property) {
		return getConfiguration().getInt(property, 0);
	}

	/**
	 * @param property
	 * @param value
	 */
	public static void setProperty(String property, Object value) {
		getConfiguration().setProperty(property, value);
	}

	/**
	 * @param property
	 * @param defaultValue
	 * @return the float value if it exists, defaultValue otherwise.
	 */
	public static float getFloat(String property, Float defaultValue) {
		return getConfiguration().getFloat(property, defaultValue);
	}

	/**
	 * The configuration file.
	 */
	private static XMLConfiguration configuration;

	/**
	 * Return the configuration node.
	 * 
	 * @return the configuration node.
	 */
	public static XMLConfiguration getConfiguration() {
		if (configuration == null)
			try {
				loadTemplateFile(IdConst.FILE_SETTINGS);
				configuration = new XMLConfiguration(IdConst.FILE_SETTINGS);
			} catch (ConfigurationException e) {
				throw new RuntimeException("Could not load the configuration :"
						+ IdConst.FILE_SETTINGS, e);
			}
		return configuration;
	}

	/**
	 * Load the given <param>userFile</param> file. If is not found, try to load
	 * the default file.
	 * 
	 * @param userFile
	 *          the configuration file.
	 * @return the user configuration.
	 */
	public static File loadTemplateFile(String userFile) {
		if (MToolKit.getFile(userFile) == null) {
			try {
				// Is there a TBS loaded?
				if (MToolKit.tbsName == null) {
					// Set the default TBS to load the corresponding settings
					MToolKit.tbsName = IdConst.TBS_DEFAULT;
				}

				// The setting file has not been found, try the previous saved settings
				final File configurationFile = MToolKit
						.getTbsFile(IdConst.FILE_SETTINGS_SAVED);
				if (configurationFile == null) {

					// Load the default tbs file
					final File configurationDefaultFile = MToolKit
							.getTbsFile(IdConst.FILE_SETTINGS_DEFAULT);
					if (configurationDefaultFile == null) {
						throw new RuntimeException(
								"No configuration file found, if you are running "
										+ "from this from Eclipse, be sure the working "
										+ "directory is set to " + "${workspace_loc:"
										+ IdConst.PROJECT_NAME + "/src/main/resources}");
					}
					FileUtils.copyFile(configurationDefaultFile, MToolKit.getFile(
							userFile, false));
				} else {
					FileUtils.copyFile(configurationFile, MToolKit.getFile(
							IdConst.FILE_SETTINGS, false));
				}
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		return MToolKit.getFile(userFile);
	}

	/**
	 * Load the given <param>userFile</param> file. If is not found, try to load
	 * the default file.
	 * 
	 * @param userFile
	 *          the configuration file.
	 * @return the user configuration.
	 */
	public static File loadTemplateTbsFile(String userFile) {
		File tbsFile = MToolKit.getTbsFile(userFile);
		if (tbsFile == null) {
			try {
				// Load the default tbs file
				File configurationFile = MToolKit
						.getTbsFile("settings/profiles/default-" + userFile);
				if (configurationFile == null) {
					throw new RuntimeException(
							"No configuration file found, if you are running "
									+ "from this from Eclipse, be sure the working "
									+ "directory is set to " + "${workspace_loc:"
									+ IdConst.PROJECT_NAME + "/src/main/resources}");
				}
				tbsFile = MToolKit.getTbsFile(userFile, false);
				FileUtils.copyFile(configurationFile, tbsFile);
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		return tbsFile;
	}
}
