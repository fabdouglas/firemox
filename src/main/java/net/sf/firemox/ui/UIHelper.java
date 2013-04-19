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
package net.sf.firemox.ui;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.92
 */
public final class UIHelper {

	/**
	 * Create a new instance of this class.
	 */
	private UIHelper() {
		super();
	}

	/**
	 * Returns the associated icon.
	 * 
	 * @param icon
	 *          the icon path relative to the images directory.
	 * @return the icon used for the UI
	 */
	public static ImageIcon getIcon(String icon) {
		String iconFile = MToolKit.getIconPath(icon);
		if (iconFile != null)
			return new ImageIcon(iconFile);
		return null;
	}

	/**
	 * Returns the associated TBS icon.
	 * 
	 * @param icon
	 *          the TBS icon path relative to the images dir
	 * @return the icon used for the UI
	 */
	public static ImageIcon getTbsIcon(String icon) {
		String iconFile = MToolKit.getTbsPicture(icon);
		if (iconFile != null)
			return new ImageIcon(iconFile);
		return null;
	}

	/**
	 * Build and return a JMenu instance with an i18n text, and <param>menuName</param>
	 * as action command.
	 * 
	 * @param menuName
	 *          the menu key name.
	 * @return the new JMenu instance.
	 */
	public static JMenu buildMenu(String menuName) {
		JMenu menu = new JMenu(LanguageManager.getString(menuName));
		menu.setActionCommand(menuName);
		return menu;
	}

	/**
	 * Build and return a JButton instance with a image icon with
	 * <param>actionName</param> as action command.
	 * 
	 * @param actionName
	 *          the action name.
	 * @return the new JMenu instance.
	 */
	public static JButton buildButton(String actionName) {
		JButton menu = new JButton(getIcon(actionName + ".gif"));
		menu.setActionCommand(actionName);
		return menu;
	}

	/**
	 * Build and return a JButton instance with a image icon with
	 * <param>actionName</param> as action command.
	 * 
	 * @param actionName
	 *          the action name.
	 * @param actionListener
	 *          the listener to register to the new component.
	 * @return the new JMenu instance.
	 */
	public static JButton buildButton(String actionName,
			ActionListener actionListener) {
		JButton menu = buildButton(actionName);
		menu.addActionListener(actionListener);
		return menu;
	}

	/**
	 * Build and return a JMenu instance with an i18n text, and <param>menuName</param>
	 * as action command.
	 * 
	 * @param menuName
	 *          the menu key name.
	 * @param mnemonic
	 *          the mnemonic char for this menu.
	 * @return the new JMenu instance.
	 */
	public static JMenu buildMenu(String menuName, char mnemonic) {
		JMenu menu = buildMenu(menuName);
		String icon = MToolKit.getIconPath(menuName + ".gif");
		if (icon == null)
			icon = MToolKit.getIconPath(menuName + ".png");
		if (icon != null)
			menu.setIcon(new ImageIcon(icon));
		return menu;
	}

	/**
	 * Build and return a JMenuItem instance with an i18n text, and
	 * <param>menuName</param> as action command. The tooltip is also set with
	 * the i18n + '.tooltip' key.
	 * 
	 * @param menuName
	 *          the menu key name.
	 * @param mnemonic
	 *          the mnemonic char for this menu.
	 * @param icon
	 *          the icon name to set to this menu.
	 * @param actionListener
	 *          the listener to register to the new component.
	 * @return the new JMenuItem instance.
	 */
	public static JMenuItem buildMenu(String menuName, char mnemonic,
			String icon, ActionListener actionListener) {
		JMenuItem result = buildMenu(menuName, mnemonic, actionListener);
		result.setMnemonic(mnemonic);
		if (icon != null)
			result.setIcon(getIcon(MToolKit.getIconPath(icon)));
		return result;
	}

	/**
	 * Build and return a JMenuItem instance with an i18n text, and
	 * <param>menuName</param> as action command. The tooltip is also set with
	 * the i18n + '.tooltip' key.
	 * 
	 * @param menuName
	 *          the menu key name.
	 * @param mnemonic
	 *          the mnemonic char for this menu.
	 * @param actionListener
	 *          the listener to register to the new component.
	 * @return the new JMenuItem instance.
	 */
	public static JMenuItem buildMenu(String menuName, char mnemonic,
			ActionListener actionListener) {
		JMenuItem result = buildMenu(menuName, actionListener);
		result.setMnemonic(mnemonic);
		return result;
	}

	/**
	 * Build and return a JMenuItem instance with an i18n text, and
	 * <param>menuName</param> as action command. The tooltip is also set with
	 * the i18n + '.tooltip' key. The used icon is first '.gif', then '.png'
	 * extension using <param>menuName</param> as base name.
	 * 
	 * @param menuName
	 *          the menu key name.
	 * @param actionListener
	 *          the listener to register to the new component.
	 * @return the new JMenuItem instance.
	 */
	public static JMenuItem buildMenu(String menuName,
			ActionListener actionListener) {
		JMenuItem result = new JMenuItem(LanguageManager.getString(menuName));
		result.addActionListener(actionListener);
		result.setActionCommand(menuName);
		String tooltip = LanguageManager.getNullString(menuName + ".tooltip");
		if (tooltip != null)
			result.setToolTipText("<html>" + tooltip);
		String icon = MToolKit.getIconPath(menuName + ".gif");
		if (icon == null)
			icon = MToolKit.getIconPath(menuName + ".png");
		if (icon != null)
			result.setIcon(new ImageIcon(icon));
		return result;
	}

}
