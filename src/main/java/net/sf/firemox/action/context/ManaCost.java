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
package net.sf.firemox.action.context;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.mana.ManaPool;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.tools.PairIntObject;

/**
 * The mana cost context : initial mana cost, mana paid, required mana
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class ManaCost implements ActionContext {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param manaCost
	 *          initial mana cost.
	 */
	public ManaCost(int[] manaCost) {
		this.manaCost = new int[IdCardColors.CARD_COLOR_NAMES.length];
		this.requiredMana = new int[this.manaCost.length];
		this.manaPaid = new int[this.manaCost.length];
		addManaCost(manaCost);
	}

	/**
	 * Is the non required mana.
	 * 
	 * @return true if no mana is required.
	 */
	public boolean isNullRequired() {
		for (int mana : requiredMana) {
			if (mana != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The initial mana cost.
	 */
	public final int[] manaCost;

	/**
	 * The required mana.
	 */
	public final int[] requiredMana;

	/**
	 * The mana paid.
	 */
	public final int[] manaPaid;

	/**
	 * The restriction used to paid the mana.
	 */
	public List<PairIntObject<Test>>[] restrictions = null;

	/**
	 * Add a restriction
	 * 
	 * @param color
	 *          the restricted color
	 * @param test
	 *          restriction test to save
	 * @param amount
	 *          amount of mana remove
	 */
	@SuppressWarnings("unchecked")
	public void addRestriction(int color, Test test, int amount) {
		if (restrictions == null) {
			restrictions = new List[manaCost.length];
			restrictions[color] = new ArrayList<PairIntObject<Test>>();
		} else if (restrictions[color] == null) {
			restrictions[color] = new ArrayList<PairIntObject<Test>>();
		} else {
			// memory optimization: is already exist this restriction
			for (PairIntObject<Test> pair : restrictions[color]) {
				if (pair.value == test) {
					// previous restriction found
					pair.key += amount;
					return;
				}
			}
		}
		restrictions[color].add(new PairIntObject<Test>(amount, test));
	}

	/**
	 * Pay a required mana. Given amount of mana is remove from the given mana
	 * pool.
	 * 
	 * @param paidColor
	 *          the required mana
	 * @param withColor
	 *          the used mana
	 * @param amount
	 *          the amount of mana to remove
	 * @param manas
	 *          the mana pool
	 */
	public void payMana(int paidColor, int withColor, int amount, ManaPool manas) {
		if (requiredMana[paidColor] < amount) {
			throw new InternalError("Cannot pay the required mana : paidColor="
					+ paidColor + ", withColor=" + withColor + ", amount=" + amount);
		}
		requiredMana[paidColor] -= amount;
		manaPaid[withColor] += amount;
		manas.manaButtons[withColor].removeMana(amount,
				StackManager.currentAbility, this);
	}

	/**
	 * Restore mana paid to the given mana pool
	 * 
	 * @param manas
	 *          is the mana pool receiving the restored manas
	 */
	public void restoreMana(ManaPool manas) {
		for (int color = 6; color-- > 0;) {
			int paid = manaPaid[color];
			if (restrictions != null && restrictions[color] != null) {
				for (int i = restrictions[color].size(); i-- > 0;) {
					final PairIntObject<Test> restriction = restrictions[color].get(i);
					paid -= restriction.key;
					manas.addMana(color, restriction.key, restriction.value);
				}
			}
			if (paid > 0) {
				manas.addMana(color, paid, null);
			}
		}
	}

	/**
	 * Add a mana cost.
	 * 
	 * @param manaCost
	 *          the mana cost to add.
	 */
	private void addManaCost(int[] manaCost) {
		for (int i = this.manaCost.length; i-- > 0;) {
			this.manaCost[i] += manaCost[i];
		}
		StackManager.actionManager.updateRequiredMana(this.manaCost);
		System.arraycopy(manaCost, 0, requiredMana, 0, requiredMana.length);
	}

	/**
	 * Reduce the mana coast, and also the required withe the given color.
	 * 
	 * @param color
	 *          the color index.
	 * @param amount
	 *          reduce amount.
	 * @return the mana cost that was not possible to remove from the current mana
	 *         cost without making it negative for the given color.
	 */
	public int reduceManaCost(int color, int amount) {
		if (manaCost[color] == 0) {
			// Useless to continue, no available mana.
			return 0;
		}
		if (manaCost[color] >= amount) {
			manaCost[color] -= amount;
			requiredMana[color] -= amount;
			return 0;
		}
		// Some mana can not be removed, and the pool become 0.
		final int removed = manaCost[color];
		manaCost[color] = 0;
		requiredMana[color] = 0;
		return amount - removed;
	}
}
