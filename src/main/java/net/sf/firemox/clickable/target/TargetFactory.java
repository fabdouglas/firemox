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
package net.sf.firemox.clickable.target;

import java.awt.Color;

import javax.swing.JPopupMenu;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86.
 */
public final class TargetFactory {

	/**
	 * Create a new instance of this class.
	 */
	private TargetFactory() {
		super();
	}

	/**
	 * The text displayed in tooltip to indicate this is a valid target.
	 */
	public static String tooltipValidTarget;

	/**
	 * The text displayed in tooltip to indicate this is not a valid target.
	 */
	public static String tooltipInvalidTarget;

	/**
	 * The text displayed in tooltip to indicate this contains some data hidding
	 * the original ones.
	 */
	public static String tooltipDirtyDataBase;

	/**
	 * The color used to corlor the target component
	 */
	public static final Color TARGET_COLOR = Color.blue;

	/**
	 * The color used to color the tokenized component
	 */
	static final Color TOKENIZE_COLOR = Color.green;

	/**
	 * The associated menu to abilities choice
	 */
	public static JPopupMenu abilitiesMenu;

	/**
	 * The last Card where popup trigger has been recorded. Used by popup manager.
	 */
	public static MCard triggerTargetable;

	/**
	 * Initialize the tooltip headers and topics to display fastest the tooltip of
	 * card.
	 */
	public static void initSettings() {

		// target validation
		tooltipValidTarget = "<br><br><font color=\"blue\"><i>"
				+ LanguageManager.getString("validtarget") + "</i></font><br>";
		// target validation
		tooltipInvalidTarget = "<br><br><font color=\"red\"><i>"
				+ LanguageManager.getString("notvalidtarget") + "</i></font><br>";

		// dirty database
		tooltipDirtyDataBase = "<br><br><font color=\"green\"><i>"
				+ LanguageManager.getString("dirtydatabase") + "</i></font><br>";

		abilitiesMenu = new JPopupMenu(LanguageManager.getString("abilities"));
		abilitiesMenu.setFont(MToolKit.defaultFont);
		abilitiesMenu.setToolTipText(LanguageManager.getString("chooseability"));

	}
}
