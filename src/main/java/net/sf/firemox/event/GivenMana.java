/*
 * Created on 2005/08/25
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
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.MContextCardCardIntIntTest;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdConst;

/**
 * When the mana pool is modified.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class GivenMana extends TriggeredEvent {

	/**
	 * Create an instance of GivenMana by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>test to apply on source [Test]</li>
	 * <li>test to apply on destination player [Test]</li>
	 * <li>looked for color [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	GivenMana(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		playerTest = TestFactory.readNextTest(inputFile);
		color = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Creates a new instance of GivenMana specifying all attributes of this
	 * class. All parameters are copied, not cloned. So this new object shares the
	 * card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param sourceTest
	 *          the test applied on source giving this mana.
	 * @param card
	 *          is the card owning this card
	 * @param playerTest
	 *          test applied on the player receiving mana
	 * @param color
	 *          the looked for color
	 */
	private GivenMana(int idZone, Test sourceTest, MCard card, Test playerTest,
			Expression color) {
		super(idZone, sourceTest, card);
		this.playerTest = playerTest;
		this.color = color;
	}

	/**
	 * Return a copy of this with the specified owner
	 * 
	 * @param card
	 *          is the card of the ability of this event
	 * @return copy of this event
	 */
	@Override
	public MEventListener clone(MCard card) {
		return new GivenMana(idZone, test, card, playerTest, color);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param source
	 *          the source of the spell/ability
	 * @param target
	 *          the targeted player/card
	 * @param color
	 *          the given mana color
	 * @param player
	 *          the player receiving mana
	 * @return true if the current event match with this event
	 */
	private boolean isMatching(Ability ability, MCard source, int color,
			Player player) {
		final int lookedForColor = this.color.getValue(ability, source, null);
		return (lookedForColor == IdConst.NO_CARE || lookedForColor == color)
				&& test.test(ability, source) && playerTest.test(ability, player);
	}

	/**
	 * Dispatch this event to replacement abilites only. If one or several
	 * abilities have been activated this way, this function will return false.
	 * The return value must be checked. In case of <code>false</code> value,
	 * the caller should not call any stack resolution since activated abilities
	 * are being played.
	 * 
	 * @param source
	 *          source of target action
	 * @param player
	 *          the player receiving mana
	 * @param color
	 *          the given mana color
	 * @param amount
	 *          the amount of given mana
	 * @param restriction
	 *          the restriction attached to this mana add
	 * @return true if and only if no replacement abilities have been activated
	 */
	public static final boolean tryAction(MCard source, Player player, int color,
			int amount, Test restriction) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			for (ReplacementAbility ability : map.get(priority)) {
				boolean res;
				if (ability.isMatching()) {
					res = ((GivenMana) ability.eventComing()).isMatching(ability, source,
							color, player);
					if (res) {
						if (result == null) {
							result = new ArrayList<AbstractCard>(5);
						}
						result.add(ability
								.getTriggeredClone(new MContextCardCardIntIntTest(player,
										source, color, amount, restriction)));
					}
				}
			}
			if (!manageReplacement(source, result, "declared attacking")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param source
	 *          source of target action
	 * @param player
	 *          the player receiving mana
	 * @param color
	 *          the given mana color
	 * @param amount
	 *          the amount of given mana
	 * @param restriction
	 *          the restriction attached to this mana add
	 */
	public static final void dispatchEvent(MCard source, Player player,
			int color, int amount, Test restriction) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (((GivenMana) ability.eventComing()).isMatching(ability, source,
					color, player)) {
				ability.triggerIt(new MContextCardCardIntIntTest(player, source, color,
						amount, restriction));
			}
		}
	}

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.GIVEN_MANA;

	/**
	 * Test applied on the player receiving mana
	 */
	private Test playerTest;

	/**
	 * The looked for color.
	 */
	private Expression color;
}