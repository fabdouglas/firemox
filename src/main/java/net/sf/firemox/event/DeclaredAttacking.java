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

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class DeclaredAttacking extends TriggeredEvent {

	/**
	 * Create an instance of DeclaredAttacking by reading a file Offset's file
	 * must pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
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
	protected DeclaredAttacking(InputStream inputFile, MCard card)
			throws IOException {
		super(inputFile, card);
	}

	/**
	 * Creates a new instance of DeclaredAttacking specifying all attributes of
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
	private DeclaredAttacking(int idPlace, Test test, MCard card) {
		super(idPlace, test, card);
	}

	@Override
	public MEventListener clone(MCard card) {
		return new DeclaredAttacking(idZone, test, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param declaredCreature
	 *          the declared creature
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard declaredCreature) {
		return test(ability, declaredCreature);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param declaredCreature
	 *          declared creature
	 * @see #isMatching(Ability, MCard)
	 */
	public static void dispatchEvent(MCard declaredCreature) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (((DeclaredAttacking) ability.eventComing()).isMatching(ability,
					declaredCreature)) {
				ability.triggerIt(new MContextCardCardIntInt(declaredCreature));
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
	 * @param declaredCreature
	 *          declared creature
	 * @see #isMatching(Ability, MCard)
	 * @return true if and only if no replacement abilities have been activated
	 */
	public static boolean tryAction(MCard declaredCreature) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			for (ReplacementAbility ability : map.get(priority)) {
				boolean res;
				if (ability.isMatching()) {
					res = ((DeclaredAttacking) ability.eventComing()).isMatching(ability,
							declaredCreature);
					if (res) {
						if (result == null) {
							result = new ArrayList<AbstractCard>();
						}
						result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
								declaredCreature)));
					}
				}
			}
			if (!manageReplacement(declaredCreature, result, "declared attacking")) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.DECLARED_ATTACKING;
}
