/*
 * Created on Oct 24, 2004 
 * Original filename was PopupManager.java
 * 
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
package net.sf.firemox.zone;

import static net.sf.firemox.clickable.target.card.CardFactory.STR_EXPAND;
import static net.sf.firemox.clickable.target.card.CardFactory.STR_GATHER;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class PopupManager implements ActionListener {

	private static final int EXPAND = 1;

	private static final int GATHER = 2;

	private static final String SET_WALLPAPER = "1";

	private static final String SET_BACKGROUND = "2";

	private static final String UNSET_WALLPAPER = "3";

	private static final String MOSAIC = "4";

	private static final String FIT_TO_PANEL = "5";

	/**
	 * Creates a new instance of PopupManager <br>
	 */
	private PopupManager() {
		super();
	}

	/**
	 * Initialize popup components.
	 */
	public static void init() {
		// we have to create this menu
		optionsMenu = new JPopupMenu();
		optionsMenu.setFont(MToolKit.defaultFont);
		optionsMenu.setLabel(LanguageManager.getString("paneloptions"));
		optionsMenu.add(CardFactory.countItem);

		// expand/crop a zone
		final JMenuItem expandItem = new JMenuItem(LanguageManager
				.getString("expand"), UIHelper.getIcon(STR_EXPAND + IdConst.TYPE_PIC));
		expandItem.setToolTipText("<html>" + MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("expandTTtip"));
		expandItem.setActionCommand(STR_EXPAND);
		expandItem.addActionListener(instance);
		expandItem.setFont(MToolKit.defaultFont);
		optionsMenu.add(expandItem, EXPAND);
		final JMenuItem cropItem = new JMenuItem(LanguageManager.getString("crop"),
				UIHelper.getIcon(STR_GATHER + IdConst.TYPE_PIC));
		cropItem.setToolTipText("<html>" + MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("expandTTtip"));
		cropItem.setActionCommand(STR_GATHER);
		cropItem.addActionListener(instance);
		cropItem.setFont(MToolKit.defaultFont);
		optionsMenu.add(cropItem, GATHER);
		optionsMenu.add(new JSeparator());

		// wallpaper settings
		JMenu menuitem = new JMenu(LanguageManager.getString("wallpapersettings"));
		menuitem.setMnemonic('w');
		menuitem.setIcon(UIHelper.getIcon("landscape.gif"));
		menuitem.setFont(MToolKit.defaultFont);

		JMenuItem item = new JMenuItem(LanguageManager.getString("changewallpaper")
				+ "...");
		item.setMnemonic('c');
		item.setToolTipText("<html>"
				+ LanguageManager.getString("changewallpaper.tooltip"));
		item.setActionCommand(SET_WALLPAPER);
		item.addActionListener(instance);
		item.setFont(MToolKit.defaultFont);
		menuitem.add(item);

		itemWallpaperEnabled = new JCheckBoxMenuItem(LanguageManager
				.getString("showwallpaper"));
		itemWallpaperEnabled.setMnemonic('w');
		itemWallpaperEnabled.setToolTipText("<html>"
				+ LanguageManager.getString("showwallpaper.tooltip"));
		itemWallpaperEnabled.setActionCommand(UNSET_WALLPAPER);
		itemWallpaperEnabled.addActionListener(instance);
		itemWallpaperEnabled.setFont(MToolKit.defaultFont);
		menuitem.add(itemWallpaperEnabled);

		ButtonGroup group5 = new ButtonGroup();
		menuPosition = new JMenu(LanguageManager.getString("wallpaperposition"));
		menuPosition.setMnemonic('p');
		menuPosition.setFont(MToolKit.defaultFont);
		JRadioButtonMenuItem radioitem = new JRadioButtonMenuItem(LanguageManager
				.getString("mosaicmode"), UIHelper.getIcon("mosaic.gif"));
		radioitem.setMnemonic('m');
		radioitem.setToolTipText("<html>"
				+ LanguageManager.getString("mosaicmode.tooltip"));
		radioitem.addActionListener(instance);
		radioitem.setActionCommand(MOSAIC);
		radioitem.setFont(MToolKit.defaultFont);
		menuPosition.add(radioitem);
		group5.add(radioitem);
		radioitem = new JRadioButtonMenuItem(LanguageManager.getString("fitmode"),
				UIHelper.getIcon("fit.gif"));
		radioitem.setMnemonic('f');
		radioitem.setToolTipText("<html>"
				+ LanguageManager.getString("fitmode.tooltip"));
		radioitem.addActionListener(instance);
		radioitem.setActionCommand(FIT_TO_PANEL);
		radioitem.setFont(MToolKit.defaultFont);
		menuPosition.add(radioitem);
		group5.add(radioitem);
		menuitem.add(menuPosition);
		optionsMenu.add(menuitem);

		item = new JMenuItem(LanguageManager.getString("background") + " ...",
				UIHelper.getIcon("colorspicking.gif"));
		item.setMnemonic('b');
		item.addActionListener(instance);
		item.setActionCommand(SET_BACKGROUND);
		item.setFont(MToolKit.defaultFont);
		optionsMenu.add(item);

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String lnfName = e.getActionCommand();
		if (lnfName == SET_WALLPAPER) {
			// set and change wallpaper
			final String picFile = MToolKit.getPictureFile(triggerPlace
					.getWallPaperFile(), MagicUIComponents.magicForm);
			if (picFile != null) {
				triggerPlace.setWallPaperFile(picFile);
				triggerPlace.repaint();
			}
		} else if (lnfName == CardFactory.STR_GATHER
				|| lnfName == CardFactory.STR_EXPAND) {
			// expand/crop container
			((ExpandableZone) triggerPlace).toggle();
		} else if (lnfName == SET_BACKGROUND) {
			// set and change wallpaper
			Color color = JColorChooser.showDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("background") + ":" + triggerPlace,
					triggerPlace.getBackground());
			triggerPlace.setBackground(color);
			triggerPlace.repaint();
		} else if (lnfName == MOSAIC) {
			// set and change wallpaper
			triggerPlace.doMosaic = true;
			triggerPlace.repaint();
		} else if (lnfName == FIT_TO_PANEL) {
			// set and change wallpaper
			triggerPlace.doMosaic = false;
			triggerPlace.repaint();
		} else if (lnfName == UNSET_WALLPAPER) {
			if (triggerPlace.backImage == null) {
				// no wallpaper displayed
				if (triggerPlace.getWallPaperFile() == null) {
					// user has to choose a wallpaper
					String picFile = MToolKit.getPictureFile(null,
							MagicUIComponents.magicForm);
					if (picFile == null) {
						return;
					}
					triggerPlace.setWallPaperFile(picFile);
				} else {
					// restore wallpaper from old file
					triggerPlace.setWallPaperFile(triggerPlace.getWallPaperFile());
				}
			} else {
				triggerPlace.setWallPaperFile(null);
			}
			// set and change wallpaper
			triggerPlace.repaint();
		}
	}

	/**
	 * Is called when you click on the specified Zone. If this is done the context
	 * menu button, a popup will be displayed.
	 * 
	 * @param e
	 *          is the mouse event
	 * @param zone
	 *          is the zone associated to the popup that will appear.
	 */
	public void mousePressed(MouseEvent e, MZone zone) {
		if (e.isPopupTrigger()) {
			/*
			 * right button is pressed, show the options popup menu and mark the place
			 * clicked triggerPlace as this.
			 */
			triggerPlace = zone;
			itemWallpaperEnabled.setSelected(zone.backImage != null);
			((JRadioButtonMenuItem) menuPosition.getItem(0))
					.setSelected(zone.doMosaic);
			((JRadioButtonMenuItem) menuPosition.getItem(1))
					.setSelected(!zone.doMosaic);

			// update the crop/expand zone option
			optionsMenu.getComponent(GATHER).setVisible(false);
			optionsMenu.getComponent(EXPAND).setVisible(false);
			if (zone instanceof ExpandableZone) {
				if (ZoneManager.expandedZone == zone) {
					optionsMenu.getComponent(GATHER).setVisible(true);
				} else {
					optionsMenu.getComponent(EXPAND).setVisible(true);
				}
			}

			// update the card counter
			CardFactory.countItem.setText(LanguageManager.getString("countcard", zone
					.toString(), String.valueOf(zone.getCardCount())));

			// show the option popup menu
			optionsMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	static PopupManager instance = new PopupManager();

	/**
	 * the last Mpanel where popup trigger has been recorded
	 */
	private static MZone triggerPlace;

	/**
	 * The popup menu indicating the image layout : mosaic or fit
	 */
	private static JMenu menuPosition;

	/**
	 * The popup menu indicating whether an picture is disclayed whether the
	 * background is visible.
	 */
	private static JCheckBoxMenuItem itemWallpaperEnabled;

	/**
	 * Popup menu allowing to change wallpaper, etc...
	 */
	public static JPopupMenu optionsMenu = null;

}
