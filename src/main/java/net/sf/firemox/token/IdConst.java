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
 * 
 */
package net.sf.firemox.token;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public interface IdConst {

	/**
	 * Bit used to defined negative number
	 */
	int NEGATIVE_NUMBER_MASK = 0x8880;

	/**
	 * This constant is unused in this program. Generally, this value is set for
	 * unused field or code
	 */
	int UNUSED = 0xCCCC;

	/**
	 * No care constant This value indicates that a field is not a constraint.
	 */
	int NO_CARE = 0xFFFE;

	/**
	 * All constant Indicates a number or a size; Has different signification
	 * depending on its usage
	 */
	int ALL = 0xFFFF;

	/**
	 * Height of original card to display
	 */
	int STD_HEIGHT = 285;

	/**
	 * Width of original card to display
	 */
	int STD_WIDTH = 200;

	/**
	 * The card border ratio.
	 */
	int BORDER_WIDTH = 12;

	/**
	 * Number of cards needed to have a 3D view pixel dx=1 and dy = 1
	 */
	int CARD_3D_COUNT = 7;

	/**
	 * Location of Proxy XML files in the tbs directory.
	 */
	String PROXIES_LOCATION = "proxies";

	/**
	 * The picture type
	 */
	String TYPE_PIC = ".gif";

	/**
	 * The settings xml file for the current TBS --> never added to SVN
	 */
	String FILE_SETTINGS = "settings.xml";

	/**
	 * The settings xml file for a TBS --> never added to SVN
	 */
	String FILE_SETTINGS_SAVED = "settings/profiles/" + FILE_SETTINGS;

	/**
	 * The default settings xml file for a TBS
	 */
	String FILE_SETTINGS_DEFAULT = "settings/profiles/default-" + FILE_SETTINGS;

	/**
	 * The picture directory
	 */
	String IMAGES_DIR = "images/";

	/**
	 * Sound location
	 */
	String SOUNDS_DIR = "sounds/";

	/**
	 * Flag icons location
	 */
	String FLAGS_DIR = IMAGES_DIR + "flags/";

	/**
	 * The tbs directory
	 */
	String TBS_DIR = "tbs";

	/**
	 * The XSD file
	 */
	String TBS_XSD = "validator.xsd";

	/**
	 * The project name
	 */
	String PROJECT_NAME = "Firemox";

	/**
	 * The project name in lower case
	 */
	String PROJECT_NAME_LC = "firemox";

	/**
	 * The project display name
	 */
	String PROJECT_DISPLAY_NAME = "Firemox";

	/**
	 * The project ID
	 */
	String PROJECT_ID = "92519";

	/**
	 * The main project page
	 */
	String MAIN_PAGE = "http://" + PROJECT_NAME + ".sourceforge.net";

	/**
	 * XSD Namespace
	 */
	String NAME_SPACE = "http://sourceforge.net/projects/" + PROJECT_NAME_LC;

	/**
	 * code with no value
	 */
	int[] EMPTY_CODE = { 0, 0, 0, 0, 0, 0, 0 };

	/**
	 * Comment for <code>ABORT_ME</code>
	 */
	int ABORT_ME = IdConst.ALL;

	/**
	 * Comment for <code>definedValuesName</code>
	 */
	String[] VALUES_NAME = { "abortme", "all", "unused", "nocare", "colorless",
			"black", "blue", "green", "red", "white", "white-or-blue", "white-or-black",
			"blue-or-black", "blue-or-red", "black-or-red", "black-or-green",
			"red-or-green", "red-or-white", "green-or-white", "green-or-blue",
			"2-or-white", "2-or-blue", "2-or-black", "2-or-red", "2-or-green",
			"pblack", "pblue", "pgreen", "pred", "pwhite" };

	/**
	 * Comment for <code>definedValuesValue</code>
	 */
	int[] VALUES = { ABORT_ME, ALL, UNUSED, NO_CARE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };

	/**
	 * Minimal version for MP
	 */
	String MINIMAL_JRE = "1.5";

	/**
	 * current version of Firemox
	 */
	String VERSION = "0.95.52";

	/**
	 * The current build version number
	 */
	int VERSION_NUMBER = 0x002F;

	/**
	 * Version name
	 */
	String VERSION_NAME = PROJECT_NAME + " " + VERSION;

	/**
	 * The license file
	 */
	String FILE_LICENSE = "LICENSE.txt";

	/**
	 * The theme settings file
	 */
	String FILE_THEME_SETTINGS = "themes.properties";

	/**
	 * The database file
	 */
	String FILE_DATABASE_SAVED = "database.xml";

	/**
	 * The Log file
	 */
	String FILE_DEBUG = "debug.log";

	/**
	 * The default TBS
	 */
	String TBS_DEFAULT = "mtg";

	/**
	 * Restart code
	 */
	int EXIT_CODE_RESTART = 47;

}