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
package net.sf.firemox.deckbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sf.firemox.action.ActionFactory;
import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.Damage;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.modifier.model.ObjectFactory;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.MPhase;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.zone.Play;
import net.sf.firemox.zone.ZoneManager;

import org.apache.commons.io.IOUtils;

/**
 * Set of tools to manipulate the MDB format : load headers, finding cards,...
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public final class MdbLoader {

	/**
	 * The first available card's name offset.
	 */
	private static int firstCardsNamesOffset;

	/**
	 * The first available card's bytes offset.
	 */
	private static long firstCardsBytesOffset;

	/**
	 * The current full name of selected TBS
	 */
	private static String tbsFullName = null;

	/**
	 * The current TBS disclaimer
	 */
	private static String disclaimer = null;

	/**
	 * The current TBS more information text
	 */
	private static String moreInfo = null;

	/**
	 * The current TBS comment
	 */
	private static String author = null;

	/**
	 * The current TBS version
	 */
	private static String version = null;

	/**
	 * Indicates where to find the art from a URL
	 */
	private static String artURL = null;

	/**
	 * Indicates the picture name of the back picture.
	 */
	public static String backPicture = null;

	/**
	 * Indicates the picture name of the damage picture.
	 */
	public static String damagePicture = null;

	/**
	 * This is the defined colorless mana file names without base name. The base
	 * web base name is <code>colorlessURL</code> and the local base name is
	 * <code>tbs/TBS_NAME/images/mana/colorless/</code>
	 * 
	 * @see #colorlessSmlManas
	 */
	public static String[] colorlessSmlManas;

	/**
	 * This the HTML representation of defined small colorless manas.
	 * 
	 * @see #colorlessSmlManas
	 */
	public static String[] colorlessSmlManasHtml;

	/**
	 * This is the filename without base name corresponding to the big colorless
	 * picture. The base web base name is <code>colorlessURL</code> and the local
	 * base name is <code>tbs/TBS_NAME/images/mana/colorless/big/</code>
	 */
	public static String colorlessBigURL;

	/**
	 * This is the defined small colored mana file names without base name. The
	 * base web base name is <code>coloredManaSmlURL</code> and the local base
	 * name is <code>tbs/TBS_NAME/images/mana/colored/small/</code>
	 * 
	 * @see #coloredManaSmlURL
	 */
	public static String[] coloredSmlManas;

	/**
	 * This the HTML representation of defined small colored manas.
	 * 
	 * @see #colorlessSmlManas
	 */
	public static String[] coloredSmlManasHtml;

	/**
	 * This is the defined small colored mana file names without base name. The
	 * base web base name is <code>coloredManaBigURL</code> and the local base
	 * name is <code>tbs/TBS_NAME/images/mana/colored/big/</code>
	 * 
	 * @see #coloredManaBigURL
	 */
	public static String[] coloredBigManas;

	/**
	 * This is the web base name where colorless mana pictures can be found.
	 */
	public static String colorlessURL;

	/**
	 * This is the web base name where small colored mana pictures can be found.
	 */
	public static String coloredManaSmlURL;

	/**
	 * This is the web base name where big colored mana pictures can be found.
	 */
	public static String coloredManaBigURL;

	/**
	 * The picture used for unknown mana cost value.
	 */
	public static String unknownSmlManaHtml;

	/**
	 * The picture used for unknown mana cost value.
	 */
	public static String unknownSmlMana;

	/**
	 * This is the defined hybrid mana names associated with the file names
	 * without base name. The base web base name is <code>coloredManasURL</code>
	 * and the local base name is <code>tbs/TBS_NAME/images/mana/hybrid/</code>
	 * 
	 * @see #hybridManasURL
	 */
	public static Map<String, String> hybridManas;

	/**
	 * This is the web base name where hybrid mana pictures can be found.
	 * 
	 * @see #hybridManas
	 */
	public static String hybridManasURL;

	/**
	 * This the HTML representation of defined hybrid manas.
	 * 
	 * @see #hybridManas
	 */
	public static Map<String, String> hybridManasHtml;

	/**
	 * The offset position of end of header.
	 */
	private static long endOfHeaderOffset;

	/**
	 * The last loaded MDB File. Is <code>null</code> while no MDB has been
	 * loaded..
	 */
	private static String lastMdbFile;

	/**
	 * Flag indicating if the XML file have been checked or not for this instance.
	 */
	private static boolean instanceIsChecked = false;

	/**
	 * The last opened stream of the current MDB.
	 */
	private static FileInputStream lastMdbStream;

	/**
	 * Create a new instance of this class.
	 */
	private MdbLoader() {
		super();
	}

	/**
	 * Load author, tbs name,... Load the rules of this MDB and set them to the MP
	 * environment, set the current offset to the beginning of card section. <br>
	 * 
	 * @param dbFile
	 *          the MDB file containing rules.
	 * @param firstPlayer
	 *          the index of first player.
	 * @return the stream as is, the current offset corresponds to the beginning
	 *         of cards section.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static FileInputStream loadMDB(String dbFile, int firstPlayer)
			throws IOException {
		FileInputStream dbStream = loadHeader(dbFile);

		// load the registers and place where aborted spells would be placed
		StackManager.getInstance().init(dbStream, firstPlayer);

		// load phases, turn structure, system rules and static-modifiers
		EventManager.init(dbStream, dbFile);

		// initialize task pane layout
		if (MagicUIComponents.isUILoaded()) {
			MagicUIComponents.databasePanel.init(dbStream);
		}

		// read zone layouts of Play
		if (MagicUIComponents.isUILoaded()) {
			Play.initSectorConfigurations(dbStream);
		}

		// Skip card's code.
		resetMdb();

		/*
		 * Return the stream skipping card's code. The current offset corresponds to
		 * the beginning of cards references.
		 */
		return dbStream;
	}

	/**
	 * Reset the given MDB stream to the first offset of card references.
	 * 
	 * @return the stream.
	 */
	public static FileInputStream resetMdb() {
		try {
			if (lastMdbStream == null) {
				openMdb(MToolKit.mdbFile, false);
			}
			lastMdbStream.getChannel().position(firstCardsNamesOffset);
		} catch (IOException io) {
			throw new RuntimeException(io);
		}
		return lastMdbStream;
	}

	/**
	 * Check the given MDB file and update it if needed. Then open it and return
	 * the created stream.
	 * 
	 * @param mdbFile
	 *          the MDB file containing rules.
	 * @param forceRecheck
	 *          if true, all files are checked even if it has already been done.
	 * @return return the opened stream as is when file is opened.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static FileInputStream openMdb(String mdbFile, boolean forceRecheck)
			throws IOException {
		// Reset the opened stream
		if (lastMdbStream != null && !forceRecheck) {
			try {
				lastMdbStream.getChannel().position(0);
				return lastMdbStream;
			} catch (IOException io) {
				// Ignore this error
			}
		}

		// Clase the old stream
		if (!forceRecheck) {
			IOUtils.closeQuietly(lastMdbStream);
		}

		// Check the MDB last modified date against the XML files
		final File file = MToolKit.getFile(mdbFile);
		if (!instanceIsChecked || forceRecheck) {
			XmlConfiguration.main("-g", MToolKit.tbsName);
			if (!XmlConfiguration.hasError())
				instanceIsChecked = true;
		}
		if (file == null) {
			lastMdbStream = new FileInputStream(MToolKit.getFile(mdbFile));
		} else {
			lastMdbStream = new FileInputStream(file);
		}
		return lastMdbStream;
	}

	/**
	 * Load author, tbs name,... Load the rules of this MDB and set them to the MP
	 * environment, set the current offset to the begin of card section and return
	 * it's position Load settings associated to this MDB. <br>
	 * 
	 * @param dbFile
	 *          the MDB file containing rules.
	 * @return return the opened stream as is, the current offset corresponds to
	 *         the last byte read of the disclaimer/license section.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static FileInputStream loadHeader(String dbFile) throws IOException {
		if (dbFile.equals(lastMdbFile)) {
			lastMdbStream.getChannel().position(endOfHeaderOffset);
			return lastMdbStream;
		}
		closeMdb();
		final FileInputStream dbStream = openMdb(dbFile, false);
		tbsFullName = MToolKit.readString(dbStream);
		version = MToolKit.readString(dbStream);
		author = MToolKit.readString(dbStream);
		moreInfo = MToolKit.readString(dbStream);

		// the database references
		DatabaseFactory.init(dbStream);

		artURL = MToolKit.readString(dbStream);
		backPicture = MToolKit.readString(dbStream);
		damagePicture = MToolKit.readString(dbStream);
		disclaimer = MToolKit.readText(dbStream).trim();

		// colored mana section
		coloredManaSmlURL = MToolKit.readString(dbStream);
		coloredManaBigURL = MToolKit.readString(dbStream);
		coloredBigManas = new String[IdCommonToken.COLOR_NAMES.length];
		coloredSmlManas = new String[IdCommonToken.COLOR_NAMES.length];
		coloredSmlManasHtml = new String[coloredBigManas.length];
		for (int i = IdCommonToken.COLOR_NAMES.length; i-- > 1;) {
			int index = dbStream.read();
			coloredSmlManas[index] = MToolKit.readString(dbStream);
			coloredBigManas[index] = MToolKit.readString(dbStream);
			coloredSmlManasHtml[index] = "<img src='file:///"
					+ MToolKit.getTbsHtmlPicture("mana/colored/small/"
							+ coloredSmlManas[index]) + "'>&nbsp;";
		}

		// colorless mana section
		colorlessURL = MToolKit.readString(dbStream);
		colorlessBigURL = MToolKit.readString(dbStream);
		unknownSmlMana = MToolKit.readString(dbStream);
		unknownSmlManaHtml = "<img src='file:///"
				+ MToolKit.getTbsHtmlPicture("mana/colorless/small/" + unknownSmlMana)
				+ "'>&nbsp;";
		colorlessSmlManas = new String[dbStream.read()];
		colorlessSmlManasHtml = new String[colorlessSmlManas.length];
		for (int i = colorlessSmlManas.length; i-- > 0;) {
			int index = dbStream.read();
			colorlessSmlManas[index] = MToolKit.readString(dbStream);
			colorlessSmlManasHtml[index] = "<img src='file:///"
					+ MToolKit.getTbsHtmlPicture("mana/colorless/small/"
							+ colorlessSmlManas[index]) + "'>&nbsp;";
		}

		// hybrid mana section
		hybridManasURL = MToolKit.readString(dbStream);
		int nbHybridManas = dbStream.read();
		hybridManas = new HashMap<String, String>();
		hybridManasHtml = new HashMap<String, String>();
		for (int i = 0; i < nbHybridManas; i++) {
			String hybridManaName = MToolKit.readString(dbStream);
			hybridManas.put(hybridManaName, MToolKit.readString(dbStream));
			hybridManasHtml.put(
					hybridManaName,
					"<img scr='file:///"
							+ MToolKit.getTbsHtmlPicture("mana/hybrid/"
									+ hybridManas.get(hybridManaName)) + "'>&nbsp;");
		}

		// Read the card bytes position
		firstCardsBytesOffset = MToolKit.readInt24(dbStream);

		// the deck constraints
		DeckConstraints.init(dbStream);

		// read additional zone
		ZoneManager.initTbs(dbStream);

		// the tests references
		TestFactory.init(dbStream);

		// the action constraints and picture
		ActionFactory.init(dbStream);

		// the objects
		ObjectFactory.init(dbStream);

		// the abilities references
		AbilityFactory.init(dbStream);

		// read damage type name export
		Damage.init(dbStream);

		// load state pictures of card,tooltip filters, exported types
		CardFactory.init(dbStream);

		// Read the card names position
		endOfHeaderOffset = dbStream.getChannel().position();
		dbStream.getChannel().position(firstCardsBytesOffset);
		firstCardsNamesOffset = MToolKit.readInt24(dbStream);
		dbStream.getChannel().position(endOfHeaderOffset);

		lastMdbFile = dbFile;
		return dbStream;
	}

	/**
	 * Save the settings corresponding to the current TBS
	 */
	public static void saveTBSSettings() {
		if (MToolKit.mdbFile != null) {
			saveTBSSettings(MToolKit.mdbFile.replace(".mdb", ".pref"));
		}
	}

	/**
	 * Save the settings corresponding to the current TBS
	 * 
	 * @param settingFile
	 *          the setting file where settings would be saved
	 */
	private static void saveTBSSettings(String settingFile) {
		// save phases options
		if (EventManager.turnStructure == null) {
			// not MDB file loaded
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(MToolKit.getFile(settingFile));
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				MPhase.phases[0][i].saveSettings(out);
			}
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				MPhase.phases[1][i].saveSettings(out);
			}
			IOUtils.closeQuietly(out);
		} catch (java.io.IOException e) {
			JOptionPane.showMessageDialog(
					MagicUIComponents.magicForm,
					LanguageManager.getString("loadtbssettingspb") + " : "
							+ e.getMessage(), LanguageManager.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 * Loading the settings corresponding to the current TBS
	 */
	public static void loadTBSSettings() {
		loadTBSSettings(MToolKit.mdbFile.substring(0,
				MToolKit.mdbFile.lastIndexOf('.'))
				+ ".pref");
	}

	/**
	 * Loading the settings corresponding to the specified TBS
	 * 
	 * @param settingFile
	 *          the setting file where settings have been saved
	 */
	private static void loadTBSSettings(String settingFile) {
		// load phases options
		if (MToolKit.tbsName == null) {
			// no MDB file loaded
			return;
		}
		try {
			InputStream in = MToolKit.getResourceAsStream(settingFile);
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				MPhase.phases[0][i].loadSettings(in);
			}
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				MPhase.phases[1][i].loadSettings(in);
			}
			IOUtils.closeQuietly(in);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					MagicUIComponents.magicForm,
					LanguageManager.getString("loadtbssettingspb") + " : "
							+ e.getMessage(), LanguageManager.getString("error"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 * Close the current TBS.
	 */
	public static void closeMdb() {
		IOUtils.closeQuietly(lastMdbStream);
		lastMdbStream = null;
		lastMdbFile = null;
	}

	/**
	 * Return the first available card's bytes offset.
	 * 
	 * @return the first available card's bytes offset.
	 */
	public static long getFirstCardsBytesOffset() {
		return firstCardsBytesOffset;
	}

	/**
	 * Return full name of selected TBS.
	 * 
	 * @return full name of selected TBS.
	 */
	public static String getTbsFullName() {
		return tbsFullName;
	}

	/**
	 * Return the current TBS disclaimer.
	 * 
	 * @return the disclaimer.
	 */
	public static String getDisclaimer() {
		return disclaimer;
	}

	/**
	 * Return the current TBS more information text.
	 * 
	 * @return the current TBS more information text.
	 */
	public static String getMoreInfo() {
		return moreInfo;
	}

	/**
	 * Return the current TBS comment.
	 * 
	 * @return the current TBS comment.
	 */
	public static String getAuthor() {
		return author;
	}

	/**
	 * Return The current TBS version.
	 * 
	 * @return The current TBS version.
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Indicates where to find the art from a URL.
	 * 
	 * @return the URL art.
	 */
	public static String getArtURL() {
		return artURL;
	}

	/**
	 * Return the last opened stream of the current MDB.
	 * 
	 * @return the last opened stream of the current MDB.
	 */
	public static FileInputStream getLastMdbStream() {
		return lastMdbStream;
	}

	/**
	 * Set the current TBS name. Calling this method cause the mana symbols to be
	 * downloaded if it's not yet done.
	 * 
	 * @param tbsName
	 *          the TBS to define as current.
	 */
	public static void setToolKitMdb(String tbsName) {
		if (MToolKit.tbsName == null || !MToolKit.tbsName.equals(tbsName)) {
			MToolKit.tbsName = tbsName;
			MToolKit.mdbFile = IdConst.TBS_DIR + "/" + tbsName + "/" + tbsName
					+ ".mdb";
			LanguageManagerMDB.setMdb(tbsName);
			MToolKit.translator = null;
			try {
				loadHeader(MToolKit.mdbFile);
			} catch (IOException e) {
				Log.warn("the MDB file '" + MToolKit.mdbFile
						+ "' associated to the TBS '" + tbsName
						+ "' is not built correctly");
			}
		}
	}

}