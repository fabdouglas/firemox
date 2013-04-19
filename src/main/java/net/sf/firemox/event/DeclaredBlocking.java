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
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class DeclaredBlocking extends TriggeredEvent {

	/**
	 * Create an instance of DeclaredBlocking by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test to apply on blocking creature [...]</li>
	 * <li>test to apply on attacking creature [...]</li>
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
	public DeclaredBlocking(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		testAttacking = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of DeclaredBlocking specifying all attributes of
	 * this class. All parameters are copied, not cloned. So this new object
	 * shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 */
	private DeclaredBlocking(int idPlace, Test test, MCard card,
			Test testAttackant) {
		super(idPlace, test, card);
		this.testAttacking = testAttackant;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new DeclaredBlocking(idZone, test, card, testAttacking);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param blockingCreature
	 *          the blocking creature
	 * @param blockedCreature
	 *          the blocked attacking creature
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard blockingCreature,
			MCard blockedCreature) {
		return test(ability, blockingCreature)
				&& testAttacking.test(ability, blockedCreature);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param blockingCreature
	 *          the blocking creature
	 * @param blockedCreature
	 *          the blocked attacking creature
	 * @see #isMatching(Ability, MCard, MCard)
	 */
	public static void dispatchEvent(MCard blockingCreature, MCard blockedCreature) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (ability.isMatching()
					&& ((DeclaredBlocking) ability.eventComing()).isMatching(ability,
							blockingCreature, blockedCreature)) {
				ability.triggerIt(new MContextCardCardIntInt(blockingCreature,
						blockedCreature));
			}
		}
	}

	/**
	 * Dispatch this event to replacement abilites only. If one or several
	 * abilities have been activated this way, this function will return false.
	 * The return value must be checked. In case of <code>false</code> value,
	 * the caller should not call any stack resolution since activated abilities
	 * are being played.
	 * 
	 * @param blockingCreature
	 *          the blocking creature
	 * @param blockedCreature
	 *          the blocked attacking creature
	 * @return true if and only if no replacement abilities have been activated
	 * @see #isMatching(Ability, MCard, MCard)
	 */
	public static boolean tryAction(MCard blockingCreature, MCard blockedCreature) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			for (ReplacementAbility ability : map.get(priority)) {
				boolean res;
				if (ability.isMatching()) {
					res = ((DeclaredBlocking) ability.eventComing()).isMatching(ability,
							blockingCreature, blockedCreature);
					if (res) {
						if (result == null) {
							result = new ArrayList<AbstractCard>();
						}
						result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
								blockingCreature, blockedCreature)));
					}
				}
			}
			if (!manageReplacement(blockingCreature, result, "declared attacking")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Test applied on the blocked attacking creature
	 */
	private Test testAttacking;

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.DECLARED_BLOCKING;

}