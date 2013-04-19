/*
 * CanICast.java 
 * Created on 27 janv. 2004
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
import java.util.List;

import net.sf.firemox.action.GiveMana;
import net.sf.firemox.action.MAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdTokens;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.93 recheck the card position during ability matching to avoid
 *        playable spell from stack.
 */
public class CanICast extends MEventListener {

	/**
	 * Create an instance of CanICast by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>idCard [Expression]</li>
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
	public CanICast(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		// TODO extend the idCard to a real Expression for activated abilities
		idCard = ExpressionFactory.readNextExpression(inputFile).getValue(null,
				null, null);
	}

	/**
	 * Creates a new instance of CanICast specifying all attributes of this class.
	 * All parameters are copied, not cloned. So this new object shares the card
	 * and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 * @param idCard
	 *          is the id of card we want to play
	 */
	protected CanICast(int idZone, Test test, MCard card, int idCard) {
		super(idZone, test, card);
		this.idCard = idCard;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new CanICast(idZone, test, card, idCard);
	}

	@Override
	public final boolean isActivated() {
		return true;
	}

	@Override
	public final boolean isTriggered() {
		return false;
	}

	@Override
	public final void registerToManager(Ability ability) {
		// add this event listener
		if (!MEventListener.CAN_I_CAST_ABILITIES[ability.getController().idPlayer]
				.contains(ability)) {
			CAN_I_CAST_ABILITIES[ability.getController().idPlayer].add(ability);
		}
	}

	@Override
	public final void removeFromManager(Ability ability) {
		CAN_I_CAST_ABILITIES[0].remove(ability);
		CAN_I_CAST_ABILITIES[1].remove(ability);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param idActivePlayer
	 *          id of active player
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, int idActivePlayer) {
		return isWellPlaced() && canIcastCondition(idActivePlayer, idCard)
				&& test(ability, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param idActivePlayer
	 *          id of active player
	 * @return true if the current event match with this event
	 */
	public boolean isMatchingManaAbility(Ability ability, int idActivePlayer) {
		return idCard == IdTokens.MANA_ABILITY && test(ability, card);
	}

	@Override
	public boolean isWellPlaced() {
		return card.playableZone(card.getIdZone(), this.idZone);
	}

	@Override
	public boolean isWellPlaced(int idZone) {
		return card.playableZone(idZone, this.idZone);
	}

	/**
	 * Dispatch this event to all active event listener able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners
	 * 
	 * @param idActivePlayer
	 *          id of active player
	 * @param res
	 *          list in which playable abilities would be added
	 * @param advRes
	 *          list in which advanced playable abilities would be added
	 * @see #isMatching(Ability, int)
	 */
	public static void dispatchEvent(int idActivePlayer, List<Ability> res,
			List<Ability> advRes) {
		for (Ability ability : CAN_I_CAST_ABILITIES[idActivePlayer]) {
			if (((CanICast) ability.eventComing())
					.isMatching(ability, idActivePlayer)
					&& ability.checkTargetActions() && ability.checkObjectActions()) {
				if (ability.isMatching()) {
					res.add(ability);
				} else {
					advRes.add(ability);
				}
			}
		}
	}

	/**
	 * Dispatch this event to all active event listener able to understand this
	 * event.
	 * 
	 * @param idActivePlayer
	 *          id of active player
	 * @param res
	 *          list in which playable abilities would be added
	 * @see #isMatching(Ability, int)
	 */
	public static void dispatchManaAbilityEvent(int idActivePlayer,
			List<Ability> res) {
		for (Ability ability : CAN_I_CAST_ABILITIES[idActivePlayer]) {
			if (((CanICast) ability.eventComing()).isMatchingManaAbility(ability,
					idActivePlayer)
					&& ability.isMatching()) {
				res.add(ability);
			}
		}
	}

	/**
	 * Iterate on given actions looking for a 'give mana' action. If one or
	 * several are found, the playable <code>idCard</code> of this event will be
	 * replaced by <code>IdTokens.MANA_ABILITY</code>
	 * 
	 * @param actionList
	 *          list of actions
	 * @return true if at least one 'give mana' action has been found in the
	 *         specified list.
	 */
	public boolean updateManaAbilityTag(MAction... actionList) {
		for (MAction action : actionList) {
			if (action instanceof GiveMana) {
				idCard = IdTokens.MANA_ABILITY;
				return true;
			}
		}
		return false;
	}

	/**
	 * Return set of playable idCard
	 * 
	 * @return set of playable idCard
	 */
	public int getPlayableIdCard() {
		return idCard;
	}

	@Override
	public final Event getIdEvent() {
		return Event.CAN_CAST_CARD;
	}

	/**
	 * set of playable idCard
	 */
	private int idCard;

}