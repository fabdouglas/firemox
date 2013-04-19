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
 * MEventDetached.java
 * Created on 30 oct. 2004
 */
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * When a card is attached to another. The first card of context (context.card)
 * is the attaced component, the second card of context (context.card2) is the
 * card to which it was attached.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class AttachedEvent extends TriggeredEvent {

	/**
	 * Create an instance of AttachedEvent by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test on attached component [...]</li>
	 * <li>test on card that we'll attach to [...]</li>
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
	AttachedEvent(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		testBy = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of AttachedEvent specifying all attributes of this
	 * class. All parameters are copied, not cloned. So this new object shares the
	 * card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param testAttached
	 *          test on attached card
	 * @param testBy
	 *          test on card that we were detached to
	 * @param card
	 *          is the card owning this card
	 */
	private AttachedEvent(int idZone, Test testAttached, Test testBy, MCard card) {
		super(idZone, testAttached, card);
		this.testBy = testBy;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new AttachedEvent(idZone, test, testBy, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param attached
	 *          the attached card
	 * @param by
	 *          the card that we'll attach to
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard attached, MCard by) {
		return test(ability, attached) && testBy.test(ability, by);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param attached
	 *          the attached card
	 * @param by
	 *          the card that we'll attach to
	 * @see #isMatching(Ability, MCard, MCard)
	 */
	public static void dispatchEvent(MCard attached, MCard by) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (ability.isMatching()
					&& ((AttachedEvent) ability.eventComing()).isMatching(ability,
							attached, by)) {
				ability.triggerIt(new MContextCardCardIntInt(attached, by));
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
	public static final Event EVENT = Event.ATTACHED_TO;

	/**
	 * The test to apply on the card that we'll detached to
	 */
	private Test testBy;

}