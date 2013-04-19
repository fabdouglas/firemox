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
package net.sf.firemox.deckbuilder;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.test.DeckCounter;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class DeckConstraint implements Comparable<DeckConstraint> {

	private static final String DECK_CONSTRAINT_KEY_NAME = "deck.constraint.";

	private final Test constraint;

	private final String name;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param constraint
	 *          the constraint validation test.
	 * @param name
	 *          the constraint key name.
	 */
	public DeckConstraint(String name, Test constraint) {
		this.constraint = constraint;
		this.name = name;
	}

	/**
	 * Return the translated constraint's name.
	 * 
	 * @return the translated constraint's name.
	 */
	public String getConstraintLocalName() {
		return LanguageManagerMDB.getString(DECK_CONSTRAINT_KEY_NAME + name);
	}

	@Override
	public String toString() {
		return getConstraintLocalName();
	}

	/**
	 * Return the constraint name.
	 * 
	 * @return the constraint name.
	 * @see #getConstraintLocalName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the constraint.
	 * 
	 * @return the constraint.
	 */
	public Test getConstraint() {
		return constraint;
	}

	/**
	 * Validate the given deck.
	 * 
	 * @param deck
	 *          the given deck.
	 * @return a list of errors.
	 */
	public List<String> validate(Deck deck) {
		Deck.currentDeck = deck;
		DeckCounter.lastRanCard = null;
		final List<String> result = new ArrayList<String>();
		final boolean testResult = constraint.test(null, null);
		if (!testResult) {
			if (DeckCounter.lastRanCard == null) {
				// Global error
				result.add(LanguageManager
						.getString("wiz_network.deck.constraint.error.global"));
			} else if (DeckCounter.lastRanInstance.getThreshold().getValue(null,
					SystemCard.instance, null) == IdConst.ALL) {
				result.add(LanguageManager.getString(
						"wiz_network.deck.constraint.error.forbidden",
						DeckCounter.lastRanCard.getName()));
			} else {
				result.add(LanguageManager.getString(
						"wiz_network.deck.constraint.error.arround",
						DeckCounter.lastRanCard.getName(), String
								.valueOf(DeckCounter.lastRanInstance.getThreshold().getValue(
										null, SystemCard.instance, null))));
			}
		}
		return result;
	}

	public int compareTo(DeckConstraint o) {
		if (DeckConstraints.DECK_CONSTRAINT_NAME_NONE.equals(getName())) {
			return Integer.MIN_VALUE;
		}
		if (DeckConstraints.DECK_CONSTRAINT_NAME_NONE.equals(o.getName())) {
			return Integer.MAX_VALUE;
		}
		return getConstraintLocalName().compareTo(o.getConstraintLocalName());
	}
}
