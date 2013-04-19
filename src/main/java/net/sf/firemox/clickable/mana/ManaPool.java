/*
 * ManaPool.java
 * Created on 27 octobre 2002, 17:24
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
 */
package net.sf.firemox.clickable.mana;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.tools.Configuration;

/**
 * Representes the mana pool of a player : 5 colored manas and one colorless
 * mana. A graphic for each different mana is displayed with a jButton
 * representing the amount of manas for this colored(less) mana. The normal
 * order is : BLACK,BLUE,GREEN,RED,WHITE,COLORLESS, but if we consider the
 * opponent case, we have a reversed order and a rotation of PI to do.
 * 
 * @see net.sf.firemox.token.IdCommonToken#COLORLESS_MANA
 * @see net.sf.firemox.token.IdCommonToken#BLACK_MANA
 * @see net.sf.firemox.token.IdCommonToken#BLUE_MANA
 * @see net.sf.firemox.token.IdCommonToken#GREEN_MANA
 * @see net.sf.firemox.token.IdCommonToken#RED_MANA
 * @see net.sf.firemox.token.IdCommonToken#WHITE_MANA
 * @see net.sf.firemox.clickable.mana.Mana
 * @since 0.3 support reversed mana pictures for opponent
 * @since 0.81 mana buttons are reversed AS NEEDED
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class ManaPool extends JPanel {

	/**
	 * Creates a new instance of MManas
	 * 
	 * @param reverseImage
	 *          if true all mana pictures are flipped horizontally and vertically.
	 */
	public ManaPool(boolean reverseImage) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.reverseImage = reverseImage;

		setVisible(false);
		setBackground(Color.BLACK);

		// create and fill an array of Mana object
		manaButtons = new Mana[IdCardColors.CARD_COLOR_NAMES.length];
		for (int idColor = manaButtons.length; idColor-- > 0;) {
			final Mana mana = new Mana(idColor, reverseImage);
			manaButtons[idColor] = mana;

			if (reverseImage && Configuration.getBoolean("reverseSide", false)) {
				add(mana);
			} else {
				add(mana, 0);
			}
		}
	}

	/**
	 * Return the ammount of mana in the mana pool
	 * 
	 * @return the ammount of mana in the mana pool
	 */
	public int allManas() {
		return allManas(null);
	}

	/**
	 * Return the ammount of mana in the mana pool
	 * 
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @return the ammount of mana in the mana pool
	 */
	public int allManas(Ability abilityRequest) {
		int result = 0;
		for (int a = 6; a-- > 0;) {
			result += getMana(a, abilityRequest);
		}
		return result;
	}

	/**
	 * Return the ammount of mana in the mana pool of one color
	 * 
	 * @param idColor
	 *          is the mana's color
	 * @return the ammount of mana in the mana pool of this color
	 */
	public int getMana(int idColor) {
		return getMana(idColor, null);
	}

	/**
	 * Return the ammount of mana in the mana pool of one color
	 * 
	 * @param idColor
	 *          is the mana's color
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @return the ammount of mana in the mana pool of this color
	 */
	public int getMana(int idColor, Ability abilityRequest) {
		return manaButtons[idColor].getMana(abilityRequest);
	}

	/**
	 * Empty the mana pool
	 * 
	 * @return the ammount of mana removed from the mana pool
	 */
	public int setToZero() {
		int c = 0;
		for (int a = 6; a-- > 0;) {
			c += setToZero(a);
		}
		return c;
	}

	/**
	 * empty the mana pool of one color
	 * 
	 * @param idColor
	 *          empty the mana pool of this color
	 * @return the previous amount of mana
	 */
	public int setToZero(int idColor) {
		return manaButtons[idColor].setToZero();
	}

	/**
	 * Add a number of mana of one color
	 * 
	 * @param idColor
	 *          is the color of the mana added
	 * @param idNumber
	 *          is the number of mana added
	 * @param restriction
	 *          the test defining mana usage
	 * @return the new amount of mana
	 */
	public int addMana(int idColor, int idNumber, Test restriction) {
		return manaButtons[idColor].addMana(idNumber, restriction);
	}

	/**
	 * Remove a number of mana of one color
	 * 
	 * @param idColor
	 *          is the color of the mana to add
	 * @param idNumber
	 *          is the number of mana to remove
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @return the new amount of mana
	 */
	public int removeMana(int idColor, int idNumber, Ability abilityRequest) {
		return manaButtons[idColor].removeMana(idNumber, abilityRequest);
	}

	/**
	 * This method is invoked when opponent has clicked on mana.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// waiting for mana information
		StackManager.PLAYERS[data[0]].mana.manaButtons[data[1]].clickOn();
	}

	/**
	 * Remove any color of the border
	 */
	public void disHighLight() {
		for (Mana mana : manaButtons) {
			mana.disHighLight();
		}
	}

	/**
	 * Update the opponent side depending on the "enable reverse" options. By
	 * default, nothing is done.
	 */
	public void updateReversed() {
		removeAll();
		if (reverseImage && Configuration.getBoolean("reverseSide", false)) {
			for (int i = manaButtons.length; i-- > 0;) {
				add(manaButtons[i]);
			}
		} else {
			for (Mana mana : manaButtons) {
				add(mana);
			}
		}
	}

	/**
	 * represents all colored mana, and colorless mana The order of MMana objects
	 * are stored in this array in this order :
	 * COLORLESS,BLACK,BLUE,GREEN,RED,WHITE
	 */
	public Mana[] manaButtons;

	/**
	 * Indicates if graphics are reversed (PI rotation)
	 */
	private boolean reverseImage;
}