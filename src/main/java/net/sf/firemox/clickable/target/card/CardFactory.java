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
package net.sf.firemox.clickable.target.card;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.tools.StatePicture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.TooltipFilter;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.zone.MZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class CardFactory {

	/**
	 * The key identifying the "gathered" zone text/action.
	 */
	public static final String STR_GATHER = "gather";

	/**
	 * The key identifying the "expand" zone text/action.
	 */
	public static final String STR_EXPAND = "expand";

	private static final float CONFIG_SCALE = 0.25f;

	/**
	 * Rotate angle * PI
	 */
	public static final double ROTATE_SCALE = 0.015625d * 2 * Math.PI;

	private static final String CONFIG_KEY_SCALE = "scale";

	/**
	 * The color identifying the playable activated abilities.
	 */
	public static final Color ACTIVATED_COLOR = Color.yellow;

	/**
	 * Custom colors of Power/Toughness
	 */
	public static Color powerToughnessColor;

	/**
	 * Creates a new instance of the CardFactory.
	 */
	private CardFactory() {
		super();
	}

	/**
	 * Initialize the preview card picture
	 * 
	 * @return the preview card picture
	 */
	public static Picture initPreview() {
		try {
			DatabaseFactory.backImage = Picture.loadImage(
					MToolKit.getTbsPicture(MdbLoader.backPicture, false),
					new URL(MdbLoader.getArtURL() + "/" + MdbLoader.backPicture))
					.getContent();
		} catch (Throwable e) {
			// ignore this error
			Log.debug(e);
		}
		try {
			if (DatabaseFactory.backImage == null) {
				DatabaseFactory.backImage = Picture.loadImage(
						MToolKit.getTbsPicture("default.jpg"), null).getContent();
			}
			try {
				DatabaseFactory.damageImage = Picture.loadImage(
						MToolKit.getTbsPicture(MdbLoader.damagePicture, false),
						new URL(MdbLoader.getArtURL() + "/" + MdbLoader.damagePicture))
						.getContent();
			} catch (Throwable e) {
				// ignore this error
				Log.debug(e);
			}
			if (DatabaseFactory.damageImage == null) {
				DatabaseFactory.damageImage = Picture.loadImage(
						MToolKit.getTbsPicture("default.jpg"), null).getContent();
			}
			DatabaseFactory.scaledBackImage = Picture
					.getScaledImage(DatabaseFactory.backImage);
			DatabaseFactory.damageScaledImage = Picture
					.getScaledImage(DatabaseFactory.damageImage);
		} catch (Throwable e) {
			// ignore this error
			Log.debug(e);
		}
		previewCard = new Picture(IdConst.STD_WIDTH, IdConst.STD_HEIGHT);
		return previewCard;
	}

	/**
	 * Return an instance of CardModel. There is only one instance of CardModel by
	 * card with the same name.
	 * 
	 * @param cardName
	 *          the card name
	 * @return a newly created instance of CardModel if this card has not yet been
	 *         loaded, or a shared instance.
	 * @param inputStream
	 *          the input stream containing the data of this card.
	 */
	public static CardModel getCardModel(String cardName, InputStream inputStream) {
		return getCardModel(cardName, inputStream, LoadMode.init);
	}

	/**
	 * Return an instance of CardModel. There is only one instance of CardModel by
	 * card with the same name.
	 * 
	 * @param cardName
	 *          the card name
	 * @return a newly created instance of CardModel if this card has not yet been
	 *         loaded, or a shared instance.
	 * @param inputStream
	 *          the input stream containing the data of this card.
	 * @param loadMode
	 *          loading mode.
	 */
	public static CardModel getCardModel(String cardName,
			InputStream inputStream, LoadMode loadMode) {
		if (cardName.startsWith("_")) {
			String realCardName = cardName.substring(1);
			CardModel res = loadedCards.get(realCardName);
			if (res == null) {
				if (inputStream == null) {
					// MDB is not available
					res = new CardModelImpl(realCardName);
				} else if (loadMode == LoadMode.lazy) {
					res = new CardModelLazy(realCardName, inputStream);
				} else {
					res = new CardModelImpl(realCardName, inputStream);
				}
				loadedCards.put(realCardName, res);
			} else if (inputStream != null) {
				if (res instanceof CardModelLazy && loadMode != LoadMode.lazy) {
					res = new CardModelImpl(realCardName, inputStream);
					loadedCards.put(realCardName, res);
				} else {
					// consume the stream
					new CardModelImpl("", inputStream);
				}
			} else {
				res = new CardModelImpl(cardName);
			}
			return res;
		}

		CardModel res = loadedCards.get(cardName);
		if (res == null) {
			if (loadMode == LoadMode.lazy) {
				res = new CardModelLazy(cardName, inputStream);
			} else {
				if (inputStream == null) {
					res = new CardModelImpl(cardName);
				} else {
					res = new CardModelImpl(cardName, inputStream);
				}
			}
			loadedCards.put(cardName, res);
		} else if (res instanceof CardModelLazy && loadMode != LoadMode.lazy) {
			res = new CardModelImpl(cardName, inputStream);
			loadedCards.put(cardName, res);
		}
		return res;
	}

	/**
	 * All settings defined in the properties file and relating cards are managed
	 * here. Also, initialize the tooltip headers and topics to display fastest
	 * the tooltip of card.
	 */
	public static void initSettings() {
		ttPower = "<br><b>" + LanguageManager.getString("power") + ": </b>";
		ttToughness = "<br><b>" + LanguageManager.getString("toughness") + ": </b>";
		ttState = "<br><b>" + LanguageManager.getString("states") + ": </b>";
		ttDamage = "<br><b>" + LanguageManager.getString("damages") + ": </b>";
		ttProperties = "<br><b>" + LanguageManager.getString("properties")
				+ ": </b>";
		ttColors = "<br><b>" + LanguageManager.getString("colors") + ": </b>";
		ttTypes = "<br><b>" + LanguageManager.getString("types") + ": </b>";
		ttHeader = "<html><b>" + LanguageManager.getString("card.name") + ": </b>";
		ttHeaderAbility = "<html><b>"
				+ LanguageManager.getString("activatedability")
				+ ": </b><br>&nbsp;&nbsp;";

		ttAbility = "<br><br><b>" + LanguageManager.getString("activatedability")
				+ ": </b><font color='#336600'>";
		ttAbiltityEnd = "</font>";

		ttManacost = "<br><b>" + LanguageManager.getString("manacost") + " :</b>";
		ttManapaid = ")<br><b>" + LanguageManager.getString("manapaid") + " :</b>";

		ttAdvancedAability = "<br><br><img src='file:///"
				+ MToolKit.getIconPath("warn.gif") + "'><font color='#660000'><b>"
				+ LanguageManager.getString("advanceactivatedability") + ": </b>";
		ttAdvancedAabilityEnd = "</font>";

		// credits
		ttRulesAuthor = "<br><br><b>" + LanguageManager.getString("rulesauthor")
				+ ": </b>";

		ttSource = "<br><b>" + LanguageManager.getString("source") + ": </b>";

		contextMenu = new JPopupMenu(LanguageManager.getString("options"));
		contextMenu.setFont(MToolKit.defaultFont);
		countItem = new JMenuItem("", UIHelper.getIcon("count.gif"));
		countItem.setFont(MToolKit.defaultFont);
		expandItem = new JMenuItem(LanguageManager.getString(STR_EXPAND), UIHelper
				.getIcon(STR_EXPAND + IdConst.TYPE_PIC));
		expandItem.setToolTipText("<html>" + MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("expandTTtip2"));
		expandItem.setActionCommand(STR_EXPAND);
		expandItem.addActionListener(SystemCard.instance);
		expandItem.setFont(MToolKit.defaultFont);
		gatherItem = new JMenuItem(LanguageManager.getString(STR_GATHER), UIHelper
				.getIcon(STR_GATHER + IdConst.TYPE_PIC));
		gatherItem.setToolTipText("<html>" + MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("expandTTtip2"));
		gatherItem.setActionCommand(STR_GATHER);
		gatherItem.addActionListener(SystemCard.instance);
		gatherItem.setFont(MToolKit.defaultFont);
		javaDebugItem = new JMenuItem(LanguageManager.getString("javadebug"),
				UIHelper.getIcon("javadebug.gif"));
		javaDebugItem.addActionListener(SystemCard.instance);
		databaseCardInfoItem = new JMenuItem(LanguageManager
				.getString("databasecard"), UIHelper.getIcon("databasecard.gif"));
		databaseCardInfoItem.addActionListener(SystemCard.instance);
		reloadPictureItem = new JMenuItem(LanguageManager
				.getString("card.reload.picture"), UIHelper.getIcon("reload.gif"));
		reloadPictureItem.addActionListener(SystemCard.instance);
		updateColor(Configuration.getString("border-color", "auto"));
		updateScale();
	}

	/**
	 * Update the scale, sizes and random angle of all cards of current game.
	 */
	public static void updateAllCardsUI() {
		updateScale();
		for (Player player : StackManager.PLAYERS) {
			for (MZone zone : player.zoneManager.getValidTargetZones()) {
				for (Component card : zone.getComponents()) {
					((MCard) card).getMUI().updateMUI();
					((MCard) card).getDatabase().updateMUI();
					((MCard) card).getMUI().updateSizes();
				}
			}
		}
	}

	/**
	 * Update the card border's color and other UI colors requiring only a global
	 * repaint to update the UI.
	 * 
	 * @param colorStr
	 *          is the new card border's color
	 */
	public static void updateColor(String colorStr) {
		if (colorStr != null) {
			if ("white".equals(colorStr)) {
				borderColor = Color.WHITE;
			} else if ("gold".equals(colorStr)) {
				borderColor = Color.YELLOW.darker().darker();
			} else if ("black".equals(colorStr)) {
				borderColor = Color.BLACK;
			} else {
				// Auto
				borderColor = null;
			}
		}
		powerToughnessColor = new Color(Configuration.getInt("powerToughnessColor",
				Color.BLUE.getRGB()));
	}

	/**
	 * Save writable string settings of cards
	 */
	public static void saveSettings() {
		if (Color.WHITE.equals(borderColor)) {
			borderColor = Color.WHITE;
			Configuration.setProperty("border-color", "white");
		} else if (Color.YELLOW.darker().darker().equals(borderColor)) {
			Configuration.setProperty("border-color", "gold");
		} else if (Color.BLACK.equals(borderColor)) {
			Configuration.setProperty("border-color", "black");
		} else {
			// Auto
			Configuration.setProperty("border-color", "auto");
		}
	}

	/**
	 * Read from the specified stream the state picture options. The current
	 * offset of the stream must pointing on the number of state pictures.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>number of states [1]</li>
	 * <li>state picture name i + \0 [...]</li>
	 * <li>state value i + \0 [...]</li>
	 * <li>state picture x i [2]</li>
	 * <li>state picture y i [2]</li>
	 * <li>state picture width i [2]</li>
	 * <li>state picture height i [2]</li>
	 * </ul>
	 * <br>
	 * Read from the specified stream the tooltip filters. The current offset of
	 * the stream must pointing on the number of tooltip filters.
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>display powerANDtoughness yes=1,no=0 [1]</li>
	 * <li>display states yes=1,no=0 [1]</li>
	 * <li>display types yes=1,no=0 [1]</li>
	 * <li>display colors yes=1,no=0 [1]</li>
	 * <li>display properties yes=1,no=0 [1]</li>
	 * <li>display damage yes=1,no=0 [1]</li>
	 * <li>filter [...]</li>
	 * </ul>
	 * 
	 * @param input
	 *          the stream containing settings.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void init(InputStream input) throws IOException {
		loadedCards.clear();
		// load state pictures of card
		statePictures = new StatePicture[input.read()];
		for (int i = 0; i < statePictures.length; i++) {
			statePictures[i] = new StatePicture(input);
		}

		// load tooltip filters
		tooltipFilters = new TooltipFilter[input.read()];
		for (int i = 0; i < tooltipFilters.length; i++) {
			tooltipFilters[i] = new TooltipFilter(input);
		}

		// read types name export (this list must be sorted)
		int count = input.read();
		exportedIdCardNames = new String[count];
		exportedIdCardValues = new int[count];
		for (int i = 0; i < count; i++) {
			exportedIdCardNames[i] = StringUtils.capitalize(LanguageManagerMDB
					.getString(MToolKit.readString(input)));
			exportedIdCardValues[i] = MToolKit.readInt16(input);
		}

		// read properties name export (this list must be sorted)
		exportedProperties = new TreeMap<Integer, String>();
		for (int i = MToolKit.readInt16(input); i-- > 0;) {
			final Integer key = Integer.valueOf(MToolKit.readInt16(input));
			exportedProperties.put(key, StringUtils.capitalize(LanguageManagerMDB
					.getString(MToolKit.readString(input))));
		}

		// property pictures
		if (propertyPicturesHTML != null) {
			propertyPicturesHTML.clear();
			propertyPictures.clear();
		} else {
			propertyPictures = new HashMap<Integer, Image>();
			propertyPicturesHTML = new HashMap<Integer, String>();
		}
		for (int i = MToolKit.readInt16(input); i-- > 0;) {
			final int property = MToolKit.readInt16(input);
			final String pictureStr = MToolKit.readString(input);
			if (pictureStr.length() > 0) {
				final String pictureName = "properties/" + pictureStr;
				propertyPicturesHTML.put(property, "<img src='file:///"
						+ MToolKit.getTbsHtmlPicture(pictureName) + "'>&nbsp;");
				final String pictureFile = MToolKit.getTbsPicture("properties/"
						+ pictureStr);
				if (pictureFile != null) {
					propertyPictures.put(property, Picture.loadImage(pictureFile));
				} else if (MagicUIComponents.isUILoaded()) {
					Log.error("Unable to load property picture '"
							+ MToolKit.getTbsPicture(pictureName, false) + "'");
				}
			}
		}

		// Add system card
		loadedCards.put("system", new CardModelImpl("system"));
	}

	/**
	 * Update the scaling transformation for all cards
	 */
	public static void updateScale() {
		float scale = getScale();
		cardWidth = (int) (IdConst.STD_WIDTH * scale);
		cardHeight = (int) (IdConst.STD_HEIGHT * scale);

		// TODO perspective view for card --> not yet implemented
		// atImageSpace.concatenate(new AffineTransform(0.8f,0,-0.1f,1,10,0));
	}

	/**
	 * Return the IdCard value from it's key name.
	 * 
	 * @param idCardName
	 *          the IdCard non-translated name
	 * @return the IdCard value from it's key name. <code>-1</code> if the given
	 *         name does not exist.
	 */
	public static int getIdCard(String idCardName) {
		final int index = ArrayUtils.indexOf(exportedIdCardNames, idCardName);
		if (index != -1) {
			return exportedIdCardValues[index];
		}
		return -1;
	}

	/**
	 * Return the property value from it's name.
	 * 
	 * @param property
	 *          the property non-translated name
	 * @return the property value from it's name. <code>-1</code> if the given
	 *         name does not exist.
	 */
	public static int getProperty(String property) {
		for (Map.Entry<Integer, String> entry : exportedProperties.entrySet()) {
			if (entry.getValue().equals(property)) {
				return entry.getKey();
			}
		}
		return -1;
	}

	/**
	 * Return the properties names between the given values.
	 * 
	 * @param minId
	 *          the minimal property id.
	 * @param maxId
	 *          the maximal property id.
	 * @return the properties names between the given values.
	 */
	public static Collection<String> getPropertiesName(int minId, int maxId) {
		return exportedProperties.headMap(maxId + 1).tailMap(minId).values();
	}

	/**
	 * Returns the scale of images of cards.
	 * 
	 * @return the scale of images of cards.
	 */
	public static float getScale() {
		return Configuration.getFloat(CONFIG_KEY_SCALE, CONFIG_SCALE);
	}

	/**
	 * The loaded cards. Card name is the key.
	 */
	private static Map<String, CardModel> loadedCards = new HashMap<String, CardModel>(
			100);

	/**
	 * the state pictures check during each paint of cards
	 */
	static StatePicture[] statePictures;

	/**
	 * the tooltip filters of cards
	 */
	static TooltipFilter[] tooltipFilters;

	/**
	 * The associated menu to context menu of the clicked card.
	 */
	static JPopupMenu contextMenu;

	/**
	 * Scaled height of card to display
	 */
	public static int cardHeight;

	/**
	 * Scaled width of card to display
	 */
	public static int cardWidth;

	/**
	 * The border color of all card
	 */
	public static Color borderColor;

	/**
	 * The "power" topic string part of tooltip
	 */
	static String ttPower;

	/**
	 * The "toughness" topic string part of tooltip
	 */
	static String ttToughness;

	/**
	 * The "state" topic string part of tooltip
	 */
	static String ttState;

	/**
	 * The "damage" topic string part of tooltip
	 */
	static String ttDamage;

	/**
	 * The "properties" topic string part of tooltip.
	 */
	static String ttProperties;

	/**
	 * The "colors" topic string part of tooltip.
	 */
	static String ttColors;

	/**
	 * The "rules author" topics string part of tooltip.
	 */
	static String ttRulesAuthor;

	/**
	 * The "activated ability" topic string part of tooltip.
	 */
	static String ttAbility;

	static String ttAbiltityEnd;

	static String ttAdvancedAability;

	static String ttAdvancedAabilityEnd;

	/**
	 * The "types" topic string part of tooltip.
	 */
	static String ttTypes;

	/**
	 * The "header" and "card name" topics string part of tooltip.
	 */
	public static String ttHeader;

	/**
	 * The "header" and "ability name" topics string part of tooltip
	 */
	static String ttHeaderAbility;

	/**
	 * The "count card" item of context menu
	 */
	public static JMenuItem countItem;

	/**
	 * The "expand panel" item of context menu
	 */
	public static JMenuItem expandItem;

	/**
	 * The "gathered panel" item of context menu
	 */
	public static JMenuItem gatherItem;

	/**
	 * The available Id card names.
	 */
	public static String[] exportedIdCardNames;

	/**
	 * The available Id card values.
	 */
	public static int[] exportedIdCardValues;

	/**
	 * The available properties.
	 */
	public static SortedMap<Integer, String> exportedProperties;

	/**
	 * The available properties pictures as Html.
	 */
	public static Map<Integer, String> propertyPicturesHTML;

	/**
	 * The available properties pictures as Image.
	 */
	public static Map<Integer, Image> propertyPictures;

	/**
	 * The warning Icon.
	 */
	public static final Icon WARNING_PICTURE = new ImageIcon("images/warn.gif");

	/**
	 * Mana cost header
	 */
	static String ttManacost;

	/**
	 * Mana paid header
	 */
	static String ttManapaid;

	/**
	 * the back image of all Magic card
	 */
	public static Picture previewCard = null;

	/**
	 * The java debug display. Show java attributes of a card.
	 */
	static JMenuItem javaDebugItem;

	/**
	 * Activate the the "database" tab of a card.
	 */
	static JMenuItem databaseCardInfoItem;

	/**
	 * Force the picture to be reloaded.
	 */
	static JMenuItem reloadPictureItem;

	/**
	 * The "source" topic string part of tooltip
	 */
	static String ttSource;

	/**
	 * The last known card name.
	 */
	static String lastCardName;

}
