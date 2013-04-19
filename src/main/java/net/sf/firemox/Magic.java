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
package net.sf.firemox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.firemox.action.PayMana;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.DeckReader;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.network.Client;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.MChat;
import net.sf.firemox.network.Server;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.MCommonVars;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.JavaVersion;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Pair;
import net.sf.firemox.tools.VersionChecker;
import net.sf.firemox.tools.WebBrowser;
import net.sf.firemox.ui.MUIManager;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.SkinLF;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.component.ProxyConfiguration;
import net.sf.firemox.ui.component.SplashScreen;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.About;
import net.sf.firemox.ui.wizard.AboutMdb;
import net.sf.firemox.ui.wizard.Bug;
import net.sf.firemox.ui.wizard.Feature;
import net.sf.firemox.ui.wizard.InputNumber;
import net.sf.firemox.ui.wizard.Settings;
import net.sf.firemox.ui.wizard.Wizard;
import net.sf.firemox.xml.XmlConfiguration;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlParser.Node;
import net.sf.firemox.xml.magic.Oracle2Xml;
import net.sf.firemox.zone.Play;
import net.sf.firemox.zone.ZoneManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jvnet.substance.SubstanceLookAndFeel;

import com.l2fprod.util.ZipResourceLoader;

/**
 * @author Fabrice Daugan
 * @version 0.54
 * @since 0.54 All listeners are no more implemented as inner class
 */
public final class Magic extends MagicUIComponents {

	/**
	 * Creates new form Magic
	 */
	private Magic() {
		super();
		initComponents();

		// list all installed Look&Feel
		UIListener uiListener = new UIListener();
		UIManager.LookAndFeelInfo[] uimTMP = UIManager.getInstalledLookAndFeels();
		final List<Pair<String, String>> lfList = new ArrayList<Pair<String, String>>();
		for (LookAndFeelInfo lookAndFeel : uimTMP) {
			Pair<String, String> info = new Pair<String, String>(lookAndFeel
					.getName(), lookAndFeel.getClassName());
			if (!lfList.contains(info)) {
				lfList.add(info);
			}
		}

		// list all SkinLF themes
		final File[] themes = MToolKit.getFile("lib").listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f != null && f.getName().endsWith(".zip");
			}
		});

		int maxIndex = Configuration.getConfiguration().getMaxIndex(
				"themes.skinlf.caches.cache");
		List<Pair<String, String>> knownThemes = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < maxIndex + 1; i++) {
			String file = Configuration.getConfiguration().getString(
					"themes.skinlf.caches.cache(" + i + ").[@file]");
			int index = ArrayUtils.indexOf(themes, MToolKit.getFile(file));
			if (index >= 0) {
				themes[index] = null;
				Pair<String, String> skin = new Pair<String, String>(Configuration
						.getConfiguration().getString(
								"themes.skinlf.caches.cache(" + i + ").[@name]"), file);
				knownThemes.add(skin);
				lfList.add(skin);
			}
		}

		for (File theme : themes) {
			if (theme != null) {
				// a new theme --> will be cached
				try {
					final List<Node> properties = new XmlParser().parse(
							new ZipResourceLoader(theme.toURI().toURL())
									.getResourceAsStream("skinlf-themepack.xml")).getNodes(
							"property");
					PROPERTIES: for (int j = 0; j < properties.size(); j++) {
						if ("skin.name".equals(properties.get(j).getAttribute("name"))) {
							// skin name found
							String relativePath = MToolKit.getRelativePath(theme
									.getCanonicalPath());
							Pair<String, String> skin = new Pair<String, String>(properties
									.get(j).getAttribute("value"), "zip:" + relativePath);
							lfList.add(skin);
							knownThemes.add(new Pair<String, String>(properties.get(j)
									.getAttribute("value"), relativePath));
							break PROPERTIES;
						}
					}
				} catch (Exception e) {
					Log.error("Error in " + theme, e);
				}
			}
		}

		Configuration.getConfiguration().clearProperty("themes.skinlf.caches");
		for (int index = 0; index < knownThemes.size(); index++) {
			Pair<String, String> skin = knownThemes.get(index);
			Configuration.getConfiguration().setProperty(
					"themes.skinlf.caches.cache(" + index + ").[@name]", skin.key);
			Configuration.getConfiguration().setProperty(
					"themes.skinlf.caches.cache(" + index + ").[@file]", skin.value);
		}

		// create look and feel menu items
		Collections.sort(lfList);
		lookAndFeels = new JRadioButtonMenuItem[lfList.size() + 1];
		ButtonGroup group5 = new ButtonGroup();
		for (int i = 0; i < lfList.size(); i++) {
			final String lfName = lfList.get(i).key;
			final String lfClassName = lfList.get(i).value;
			lookAndFeels[i] = new JRadioButtonMenuItem(lfName);
			if (lookAndFeelName.equalsIgnoreCase(lfClassName)) {
				// this the current l&f
				lookAndFeels[i].setSelected(true);
			}
			if (!SkinLF.isSkinLF(lfClassName)) {
				lookAndFeels[i]
						.setEnabled(MToolKit.isAvailableLookAndFeel(lfClassName));
			}
			group5.add(lookAndFeels[i]);
			lookAndFeels[i].setActionCommand(lfClassName);
			themeMenu.add(lookAndFeels[i], i);
			lookAndFeels[i].addActionListener(uiListener);
		}
		lfList.clear();

		initialdelayMenu.addActionListener(this);
		dismissdelayMenu.addActionListener(this);

		ConnectionManager.enableConnectingTools(false);

		// read auto mana option
		MCommonVars.autoMana = Configuration.getBoolean("automana", true);
		autoManaMenu.setSelected(MCommonVars.autoMana);

		// Force to initialize the TBS settings
		MToolKit.tbsName = null;
		setMdb(Configuration.getString("lastTBS", IdConst.TBS_DEFAULT));

		// set the autoStack mode
		MCommonVars.autoStack = Configuration.getBoolean("autostack", false);
		autoPlayMenu.setSelected(MCommonVars.autoStack);

		// read maximum displayed colored mana in context
		PayMana.thresholdColored = Configuration.getInt("threshold-colored", 6);

		ZoneManager.updateLookAndFeel();

		// pack this frame
		pack();

		// Maximize this frame
		setExtendedState(Frame.MAXIMIZED_BOTH);

		if (batchMode != -1) {

			final String deckFile = Configuration.getString(Configuration
					.getString("decks.deck(0)"));
			try {
				Deck batchDeck = DeckReader.getDeck(this, deckFile);
				switch (batchMode) {
				case BATCH_SERVER:
					ConnectionManager.server = new Server(batchDeck, null);
					ConnectionManager.server.start();
					break;
				case BATCH_CLIENT:
					ConnectionManager.client = new Client(batchDeck, null);
					ConnectionManager.client.start();
					break;
				default:
				}
			} catch (Throwable e) {
				throw new RuntimeException("Error in batch mode : ", e);
			}
		}
	}

	/**
	 * invoked when player declines to response to an event.
	 * 
	 * @see net.sf.firemox.stack.ActionManager#manualSkip()
	 */
	public void manualSkip() {
		// do the right action : cancel or skip?
		if (StackManager.idHandedPlayer == 0) {
			Player.unsetHandedPlayer();
			sendManualSkip();
			// abort this spell/ability
			StackManager.actionManager.manualSkip();
		}
	}

	/**
	 * Send the "manual skipped" action to opponent
	 */
	public static void sendManualSkip() {
		// we inform opponent that we abort this cast or skip phase/event
		net.sf.firemox.tools.Log.debug("      ...-> manual skip");
		ConnectionManager.send(CoreMessageType.SKIP);
	}

	/**
	 * Exit the Application
	 * 
	 * @param evt
	 *          is ignored
	 */
	private void exitForm(WindowEvent evt) {
		try {
			ConnectionManager.closeConnexions();
		} catch (Throwable t) {
			// ignore error
		}
		saveSettings();
		System.exit(0);
	}

	/**
	 * Save the settings
	 */
	public static void saveSettings() {
		// save part of LF_SETTINGS_FILE
		try {
			// save your preferred look and feel
			if (lookAndFeelName.toUpperCase().startsWith(
					"ZIP:" + MToolKit.getRelativePath().toUpperCase())) {
				Configuration.setProperty("preferred", "zip:"
						+ MToolKit.getFile(
								lookAndFeelName.substring("zip:".length()
										+ MToolKit.getRelativePath().length() + 1)).toString()
								.replace('\\', '/'));
			} else {
				Configuration.setProperty("preferred", lookAndFeelName);
			}

			// save back colors and wallpapers
			ZoneManager.saveSettings();

			Configuration.setProperty("language", LanguageManager.getLanguage()
					.getKey());
			Configuration.setProperty("automana", MCommonVars.autoMana);
			Configuration.setProperty("autostack", MCommonVars.autoStack);
			Configuration.setProperty("lastTBS", MToolKit.tbsName);

			// write proxy configuration
			Configuration.setProperty("logdisptime", logListing.isDispTime());
			Configuration.setProperty("loglocked", logListing.isLocked());
			Configuration.setProperty("chatdisptime", chatHistoryText.isDispTime());
			Configuration.setProperty("chatlocked", chatHistoryText.isLocked());
			Configuration.setProperty("threshold-colored", PayMana.thresholdColored);
			Configuration.setProperty("border-color",
					(CardFactory.borderColor == Color.BLACK ? "black"
							: CardFactory.borderColor == Color.WHITE ? "white" : "gold"));

			targetTimer.saveSettings();
			CardFactory.saveSettings();
			DatabaseFactory.saveCache();
			Configuration.getConfiguration().save();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(magicForm, LanguageManager
					.getString("savelfpb")
					+ " : " + e.getMessage(), LanguageManager.getString("error"),
					JOptionPane.WARNING_MESSAGE);
		}

		// save the tbs settings
		MdbLoader.saveTBSSettings();
	}

	/**
	 * Is the batch mode identifier.
	 */
	public static int batchMode = -1;

	/**
	 * The SERVER batch mode.
	 */
	public static final int BATCH_SERVER = 0;

	/**
	 * The CLIENT batch mode.
	 */
	public static final int BATCH_CLIENT = 1;

	/**
	 * Substance L&F properties file.
	 */
	private static final String FILE_SUBSTANCE_PROPERTIES = "substancelaf.properties";

	/**
	 * @param args
	 *          the command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Log.init();
		Log.debug("MP v" + IdConst.VERSION + ", jre:"
				+ System.getProperty("java.runtime.version") + ", jvm:"
				+ System.getProperty("java.vm.version") + ",os:"
				+ System.getProperty("os.name") + ", res:"
				+ Toolkit.getDefaultToolkit().getScreenSize().width + "x"
				+ Toolkit.getDefaultToolkit().getScreenSize().height + ", root:"
				+ MToolKit.getRootDir());
		System.setProperty("swing.aatext", "true");
		System.setProperty(SubstanceLookAndFeel.WATERMARK_IMAGE_PROPERTY, MToolKit
				.getIconPath(Play.ZONE_NAME + "/hardwoodfloor.png"));

		final File substancelafFile = MToolKit.getFile(FILE_SUBSTANCE_PROPERTIES);
		if (substancelafFile == null) {
			Log
					.warn("Unable to locate '"
							+ FILE_SUBSTANCE_PROPERTIES
							+ "' file, you are using the command line with wrong configuration. See http://www.firemox.org/dev/project.html documentation");
		} else {
			System.getProperties().load(new FileInputStream(substancelafFile));
		}
		MToolKit.defaultFont = new Font("Arial", 0, 11);
		try {
			if (args.length > 0) {
				final String[] args2 = new String[args.length - 1];
				System.arraycopy(args, 1, args2, 0, args.length - 1);
				if ("-rebuild".equals(args[0])) {
					XmlConfiguration.main(args2);
				} else if ("-oracle2xml".equals(args[0])) {
					Oracle2Xml.main(args2);
				} else if ("-batch".equals(args[0])) {
					if ("-server".equals(args[1])) {
						batchMode = BATCH_SERVER;
					} else if ("-client".equals(args[1])) {
						batchMode = BATCH_CLIENT;
					}
				} else {
					Log
							.error("Unknown options '"
									+ Arrays.toString(args)
									+ "'\nUsage : java -jar starter.jar <options>, where options are :\n"
									+ "\t-rebuild -game <tbs name> [-x] [-d] [-v] [-h] [-f] [-n]\n"
									+ "\t-oracle2xml -f <oracle file> -d <output directory> [-v] [-h]");
				}
				System.exit(0);
				return;
			}
			if (batchMode == -1 && !"Mac OS X".equals(System.getProperty("os.name"))) {
				splash = new SplashScreen(MToolKit.getIconPath("splash.jpg"), null,
						2000);
			}

			// language settings
			LanguageManager.initLanguageManager(Configuration.getString("language",
					"auto"));
		} catch (Throwable t) {
			Log.error("START-ERROR : \n\t" + t.getMessage());
			System.exit(1);
			return;
		}
		Log.debug("MP Language : " + LanguageManager.getLanguage().getName());
		speparateAvatar = Toolkit.getDefaultToolkit().getScreenSize().height > 768;

		// verify the java version, minimal is 1.5
		if (new JavaVersion().compareTo(new JavaVersion(IdConst.MINIMAL_JRE)) == -1) {
			Log.error(LanguageManager.getString("wrongjava") + IdConst.MINIMAL_JRE);
		}

		// load look and feel settings
		lookAndFeelName = Configuration.getString("preferred",
				MUIManager.LF_SUBSTANCE_CLASSNAME);
		// try {
		// FileInputStream in= new FileInputStream("MAGIC.TTF");
		// MToolKit.defaultFont= Font.createFont(Font.TRUETYPE_FONT, in);
		// in.close();
		// MToolKit.defaultFont= MToolKit.defaultFont.deriveFont(Font.BOLD, 11);
		// }
		// catch (FileNotFoundException e) {
		// System.out.println("editorfont.ttf not found, using default.");
		// }
		// catch (Exception ex) {
		// ex.printStackTrace();
		// }

		// Read available L&F
		final LinkedList<Pair<String, String>> lfList = new LinkedList<Pair<String, String>>();
		try {
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(
					MToolKit.getResourceAsStream(IdConst.FILE_THEME_SETTINGS)));
			String line;
			while ((line = buffReader.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#")) {
					final int index = line.indexOf(';');
					if (index != -1) {
						lfList.add(new Pair<String, String>(line.substring(0, index), line
								.substring(index + 1)));
					}
				}
			}
			IOUtils.closeQuietly(buffReader);
		} catch (Throwable e) {
			// no place for resolve this problem
			Log.debug("Error reading L&F properties : " + e.getMessage());
		}
		for (Pair<String, String> pair : lfList) {
			UIManager.installLookAndFeel(pair.key, pair.value);
		}

		// install L&F
		if (SkinLF.isSkinLF(lookAndFeelName)) {
			// is a SkinLF Look & Feel
			/*
			 * Make sure we have a nice window decoration.
			 */
			SkinLF.installSkinLF(lookAndFeelName);
		} else {
			// is Metal Look & Feel
			if (!MToolKit.isAvailableLookAndFeel(lookAndFeelName)) {
				// preferred look&feel is not available
				JOptionPane.showMessageDialog(magicForm, LanguageManager.getString(
						"preferredlfpb", lookAndFeelName), LanguageManager
						.getString("error"), JOptionPane.INFORMATION_MESSAGE);
				setDefaultUI();
			}

			// Install the preferred
			LookAndFeel newLAF = MToolKit.geLookAndFeel(lookAndFeelName);
			frameDecorated = newLAF.getSupportsWindowDecorations();

			/*
			 * Make sure we have a nice window decoration.
			 */
			JFrame.setDefaultLookAndFeelDecorated(frameDecorated);
			JDialog.setDefaultLookAndFeelDecorated(frameDecorated);
			UIManager.setLookAndFeel(MToolKit.geLookAndFeel(lookAndFeelName));
		}

		// Start main thread
		try {
			new Magic();
			SwingUtilities.invokeLater(SkinLF.REFRESH_RUNNER);
		} catch (Throwable e) {
			Log.fatal("In main thread, occurred exception : ", e);
			ConnectionManager.closeConnexions();
			return;
		}
	}

	/**
	 * Initialize panel, life, poison, stack, menu
	 */
	public void initGame() {
		StackManager.reset();
		MdbLoader.loadTBSSettings();
		ZoneManager.updateReversed();
		Player.init();
		LoaderConsole.endTask();
		setVisible(true);
		backgroundBtn.stopButton();
		StackManager.noReplayToken.take();
		try {
			EventManager.start();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	public void actionPerformed(ActionEvent e) {
		final String command = e.getActionCommand();
		final Object obj = e.getSource();
		if (obj == sendButton) {
			if (sendTxt.getText().length() != 0) {
				MChat.getInstance().sendMessage(sendTxt.getText() + "\n");
				sendTxt.setText("");
			}
		} else if (command != null && command.startsWith("border-")) {
			for (int i = cardBorderMenu.getComponentCount(); i-- > 0;) {
				((JRadioButtonMenuItem) cardBorderMenu.getComponent(i))
						.setSelected(false);
			}
			((JRadioButtonMenuItem) obj).setSelected(true);
			CardFactory.updateColor(command.substring("border-".length()));
			CardFactory.updateAllCardsUI();
			magicForm.repaint();
		} else if ("menu_help_mailing".equals(command)) {
			try {
				WebBrowser.launchBrowser("http://lists.sourceforge.net/lists/listinfo/"
						+ IdConst.PROJECT_NAME + "-user");
			} catch (Exception e1) {
				JOptionPane.showOptionDialog(this, LanguageManager.getString("error")
						+ " : " + e1.getMessage(), LanguageManager.getString("web-pb"),
						JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, UIHelper
								.getIcon("wiz_update_error.gif"), null, null);
			}
		} else if ("menu_options_settings".equals(command)) {
			// Setting panel
			final Wizard settingsPanel = new Settings();
			settingsPanel.setVisible(true);
		} else if ("menu_help_check-update".equals(command)) {
			VersionChecker.checkVersion(this);
		} else if ("menu_game_new_client".equals(command)) {
			new net.sf.firemox.ui.wizard.Client().setVisible(true);
		} else if ("menu_game_new_server".equals(command)) {
			new net.sf.firemox.ui.wizard.Server().setVisible(true);
		} else if ("menu_tools_log".equals(command)) {
			new net.sf.firemox.ui.wizard.Log().setVisible(true);
		} else if ("menu_tools_featurerequest".equals(command)) {
			new Feature().setVisible(true);
		} else if ("menu_tools_bugreport".equals(command)) {
			new Bug().setVisible(true);
		} else if ("menu_game_skip".equals(command)) {
			if (ConnectionManager.isConnected() && skipButton.isEnabled()
					&& StackManager.idHandedPlayer == 0) {
				StackManager.noReplayToken.take();
				try {
					manualSkip();
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					StackManager.noReplayToken.release();
				}
			}
		} else if ("menu_game_disconnect".equals(command)) {
			ConnectionManager.closeConnexions();
		} else if ("menu_tools_jdb".equals(command)) {
			DeckBuilder.loadFromMagic();
		} else if ("menu_game_exit".equals(command)) {
			exitForm(null);
		} else if (obj == autoManaMenu) {
			/*
			 * invoked you click directly on the "auto-mana option" of the menu
			 * "options". The opponent has to know that we are in "auto colorless mana
			 * use", since player will no longer click on the mana icon to define
			 * which colored mana active player has used as colorless mana, then the
			 * opponent have not to wait for active player choice, but apply the same
			 * Algorithm calculating which colored manas are used as colorless manas.
			 * This information is not sent immediately, but will be sent with the
			 * next action of active player.
			 */
			MCommonVars.autoMana = autoManaMenu.isSelected();
		} else if (obj == autoPlayMenu) {
			/*
			 * invoked you click directly on the "auto-play option" of the menu
			 * "options".
			 */
			MCommonVars.autoStack = autoPlayMenu.isSelected();
		} else if ("menu_tools_jcb".equals(command)) {
			// TODO cardBuilderMenu -> not yet implemented
			Log.info("cardBuilderMenu -> not yet implemented");
		} else if ("menu_game_proxy".equals(command)) {
			new ProxyConfiguration().setVisible(true);
		} else if ("menu_help_help".equals(command)) {
			/*
			 * Invoked you click directly on youLabel. Opponent will receive this
			 * information.
			 */
			try {
				WebBrowser.launchBrowser("http://prdownloads.sourceforge.net/"
						+ IdConst.PROJECT_NAME + "/7e_rulebook_EN.pdf?download");
			} catch (Exception e1) {
				JOptionPane.showOptionDialog(this, LanguageManager.getString("error")
						+ " : " + e1.getMessage(), LanguageManager.getString("web-pb"),
						JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, UIHelper
								.getIcon("wiz_update_error.gif"), null, null);
			}
		} else if ("menu_help_about".equals(command)) {
			new About(this).setVisible(true);
		} else if ("menu_help_about.tbs".equals(command)) {
			new AboutMdb(this).setVisible(true);
		} else if (obj == reverseArtCheck || obj == reverseSideCheck) {
			Configuration.setProperty("reverseArt", reverseArtCheck.isSelected());
			Configuration.setProperty("reverseSide", reverseSideCheck.isSelected());
			ZoneManager.updateReversed();
			StackManager.PLAYERS[1].updateReversed();
			repaint();
			SwingUtilities.invokeLater(SkinLF.REFRESH_RUNNER);
		} else if (obj == soundMenu) {
			Configuration.setProperty("sound", soundMenu.isSelected());
			soundMenu.setIcon(soundMenu.isSelected() ? UIHelper.getIcon("sound.gif")
					: UIHelper.getIcon("soundoff.gif"));
		} else if ("menu_lf_randomAngle".equals(command)) {
			Configuration.setProperty("randomAngle", ((AbstractButton) e.getSource())
					.isSelected());
			CardFactory.updateAllCardsUI();
		} else if ("menu_lf_powerToughnessColor".equals(command)) {
			final Color powerToughnessColor = JColorChooser.showDialog(this,
					LanguageManager.getString("menu_lf_powerToughnessColor"),
					CardFactory.powerToughnessColor);
			if (powerToughnessColor != null) {
				Configuration.setProperty("powerToughnessColor", powerToughnessColor
						.getRGB());
				CardFactory.updateColor(null);
				repaint();
			}
		} else if (obj == initialdelayMenu) {
			// TODO factor this code with the one of Magic.class
			final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			new InputNumber(LanguageManager.getString("initialdelay"),
					LanguageManager.getString("initialdelay.tooltip"), 0,
					Integer.MAX_VALUE, toolTipManager.getInitialDelay()).setVisible(true);
			if (Wizard.optionAnswer == JOptionPane.YES_OPTION) {
				toolTipManager.setEnabled(Wizard.indexAnswer != 0);
				toolTipManager.setInitialDelay(Wizard.indexAnswer);
				initialdelayMenu.setText(LanguageManager.getString("initialdelay")
						+ (toolTipManager.isEnabled() ? " : " + Wizard.indexAnswer + " ms"
								: "(disabled)"));
				Configuration.setProperty("initialdelay", Wizard.indexAnswer);
			}
		} else if (obj == dismissdelayMenu) {
			// TODO factor this code with the one of Magic.class
			final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			new InputNumber(LanguageManager.getString("dismissdelay"),
					LanguageManager.getString("dismissdelay.tooltip"), 0,
					Integer.MAX_VALUE, toolTipManager.getDismissDelay()).setVisible(true);
			if (Wizard.optionAnswer == JOptionPane.YES_OPTION) {
				toolTipManager.setDismissDelay(Wizard.indexAnswer);
				Configuration.setProperty("dismissdelay", Wizard.indexAnswer);
				dismissdelayMenu.setText(LanguageManager.getString("dismissdelay")
						+ Wizard.indexAnswer + " ms");
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		Object obj = e.getSource();
		if (obj == skipButton) {
			/**
			 * invoked by a click directly on the "skip/cancel" of the label".
			 * 
			 * @param evt
			 *          mouse event
			 * @see MEventManager#raiseEvent(int,boolean)
			 * @see MEventManager#getNextStop()
			 * @see MPhase#breakpoint
			 * @see MPhase#skipAll
			 * @see Magic#manuaSkip()
			 */
			// only if enabled and left mouse button pressed
			StackManager.noReplayToken.take();
			try {
				if (ConnectionManager.isConnected() && skipButton.isEnabled()
						&& e.getButton() == MouseEvent.BUTTON1) {
					manualSkip();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				StackManager.noReplayToken.release();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Object obj = e.getSource();
		if (obj == this) {
			exitForm(e);
		}
	}

}