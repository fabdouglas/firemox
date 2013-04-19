/*
 * Created on 4 mars 2004
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
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;

/**
 * Event generated when a card receive enougth damages to make it dead
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public class LethalDamaged extends TriggeredEvent {

	/**
	 * Create an instance of MEventLethalDamage by reading a file Offset's file
	 * must pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
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
	LethalDamaged(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
	}

	/**
	 * Creates a new instance of MEventBecomeUnTapped specifying all attributes of
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
	private LethalDamaged(int idPlace, Test test, MCard card) {
		super(idPlace, test, card);
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
		return new LethalDamaged(idZone, test, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param damaged
	 *          the damaged card
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard damaged) {
		return test(ability, damaged);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param damaged
	 *          the damaged card
	 * @see #isMatching(Ability, MCard)
	 */
	public static void dispatchEvent(MCard damaged) {
		int context1 = StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
				.getAbilityContext()).getValue()
				: 0;
		int context2 = StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
				.getAbilityContext()).getValue2()
				: 0;
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (((LethalDamaged) ability.eventComing()).isMatching(ability, damaged)) {
				ability.triggerIt(new MContextCardCardIntInt(damaged, null, context1,
						context2));
			}
		}
	}

	/**
	 * /** Dispatch this event to replacement abilites only. If one or several
	 * abilities have been activated this way, this function will return false.
	 * The return value must be checked. In case of <code>false</code> value,
	 * the caller should not call any stack resolution since activated abilities
	 * are being played.
	 * 
	 * @param damaged
	 *          the damaged card
	 * @return true if and only if no replacement ability has been activated
	 */
	public static boolean tryAction(MCard damaged) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			final int context1 = StackManager.getInstance().getAbilityContext() != null
					&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
					.getAbilityContext()).getValue()
					: 0;
			final int context2 = StackManager.getInstance().getAbilityContext() != null
					&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
					.getAbilityContext()).getValue2()
					: 0;
			for (ReplacementAbility ability : map.get(priority)) {
				if (((LethalDamaged) ability.eventComing())
						.isMatching(ability, damaged)) {
					if (result == null) {
						result = new ArrayList<AbstractCard>();
					}
					result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
							damaged, null, context1, context2)));
				}
			}
			if (!manageReplacement(damaged, result, "lethal damage")) {
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
	public static final Event EVENT = Event.LETHAL_DAMAGE;
}