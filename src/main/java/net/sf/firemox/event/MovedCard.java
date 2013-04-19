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
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.AbstractAbility;
import net.sf.firemox.clickable.ability.DetachmentAbility;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.80 support replacement
 */
public class MovedCard extends TriggeredEvent {

	/**
	 * Create an instance of MEventListener by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test to apply on source [...]</li>
	 * <li>test to apply on destination [...]</li>
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
	public MovedCard(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		testDestination = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of CanICast specifying all attributes of this class.
	 * All parameters are copied, not cloned. So this new object shares the card
	 * and the specified codes
	 * 
	 * @param idPlace
	 *          the place constraint to activate this event
	 * @param testSource
	 *          the test of this event, this one is applied on source
	 * @param testDestination
	 *          test to apply on destination
	 * @param card
	 *          is the card owning this card
	 */
	public MovedCard(int idPlace, Test testSource, Test testDestination,
			MCard card) {
		super(idPlace, testSource, card);
		this.testDestination = testDestination;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new MovedCard(idZone, test, testDestination, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param movingCard
	 *          the moving card
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard movingCard) {
		return test(ability, movingCard);
	}

	@Override
	public boolean reCheck(ContextEventListener previousContext, Ability ability) {
		return super.reCheck(previousContext, ability)
				&& testDestination.test(ability, ((MContextTarget) previousContext)
						.getCard());
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param movingCard
	 *          the moving card
	 * @param idPlaceDest
	 *          destination zone of this move
	 * @param newController
	 *          the new controller.
	 * @param silentMode
	 *          Is the silent mode is enabled for this move.
	 * @return true if this action cannot be played, or has been replaced by
	 *         another one(s)
	 */
	public static boolean tryAction(MCard movingCard, int idPlaceDest,
			Player newController, boolean silentMode) {
		if (silentMode) {
			return true;
		}
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			final int previousPlace = movingCard.getIdZone();
			final Player previousController = movingCard.controller;
			movingCard.controller = newController;
			for (ReplacementAbility ability : map.get(priority)) {
				// save the previous place before simulating the movement
				if (((MovedCard) ability.eventComing()).isMatching(ability, movingCard)
						&& ability.isMatching()) {
					/*
					 * simulate the movement, and then restore the real card zone, this
					 * cheat is needed since the 'MovedCard' event is a two pass event
					 */
					movingCard.setIdZone(idPlaceDest);
					if (((MovedCard) ability.eventComing()).testDestination.test(ability,
							movingCard)) {
						// the second pass succeed, this replacement is valid
						if (result == null) {
							result = new ArrayList<AbstractCard>();
						}
						result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
								movingCard, null, idPlaceDest, newController.idPlayer)));
					}
					movingCard.setIdZone(previousPlace);
				}
			}
			if (result != null) {
				// unregister useless abilities from the eventManager
				movingCard.unregisterAbilities();
				manageReplacement(movingCard, result, "moved card");
				return false;
			}
			movingCard.controller = previousController;
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
	 * @param movingCard
	 *          the moving card
	 * @param idPlaceDest
	 *          destination zone of this move
	 * @param newController
	 *          the new controller.
	 * @param silentMode
	 *          Is the silent mode is enabled for this move.
	 */
	public static void dispatchEvent(MCard movingCard, int idPlaceDest,
			Player newController, boolean silentMode) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if ((!silentMode || (ability instanceof AbstractAbility && !(ability instanceof DetachmentAbility)))
					&& ((MovedCard) ability.eventComing())
							.isMatching(ability, movingCard) && ability.isMatching()) {
				ability.triggerIt(new MContextCardCardIntInt(movingCard, null,
						idPlaceDest, newController.idPlayer, movingCard.getTimestamp() + 1,
						-1));
			}
		}
	}

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof MovedCard && ((MovedCard) other).test == test
				&& ((MovedCard) other).testDestination == testDestination;
	}

	@Override
	public int hashCode() {
		return test.hashCode();
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.MOVING_CARD;

	/**
	 * test of card destination
	 */
	private final Test testDestination;

}