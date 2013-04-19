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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.expression.Counter;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MCardCompare;

/**
 * Count cards in a player's deck until a threshold.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class DeckCounter extends Test {

	/**
	 * The test to use for counters
	 */
	private final Test test;

	/**
	 * The last test running for this class.
	 */
	public static DeckCounter lastRanInstance;

	/**
	 * The last card tested with a counter.
	 */
	public static MCardCompare lastRanCard;

	/**
	 * The threshold of this counter.
	 */
	private final Expression threshold;

	/**
	 * Creates a new instance of DeckCounter <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test used to fill counter [Test]
	 * <li>threshold [Expression]
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public DeckCounter(InputStream inputFile) throws IOException {
		super(inputFile);
		test = TestFactory.readNextTest(inputFile);
		threshold = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		// we count cards, we save the upper counter test.
		final Target previousTested = Counter.superTested;
		final int threshold = this.threshold.getValue(ability, tested, null);
		Counter.superTested = tested;
		lastRanCard = null;
		int res = 0;
		for (MCardCompare card : Deck.currentDeck.getCards()) {
			try {
				if (test.test(ability, card.getCard(Deck.currentDeck.getMdbStream()))) {
					res += card.getAmount();
					if (res > threshold) {
						if (lastRanCard == null) {
							lastRanCard = card;
							lastRanInstance = this;
						}
						return false;
					}
				} else if (threshold == IdConst.ALL) {
					// We are in 'all' mode, no faillure allowed here
					if (lastRanCard == null) {
						lastRanCard = card;
						lastRanInstance = this;
					}
					return false;
				}
			} catch (IOException e) {
				Log.error(e);
			}
		}
		lastRanInstance = this;

		// restore the previous upper counter test.
		Counter.superTested = previousTested;
		return true;
	}

	/**
	 * Return the threshold of this counter.
	 * 
	 * @return the threshold of this counter.
	 */
	public Expression getThreshold() {
		return this.threshold;
	}

	@Override
	public String toString() {
		return "OCCURENCE IN DECK <= "
				+ threshold.getPreemptionValue(null, SystemCard.instance, null);
	}

}
