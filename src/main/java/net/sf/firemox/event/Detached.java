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
 * When an attached card is detached from another. The first card of context
 * (context.card) is the detached card, the second card of context
 * (context.card2) is the card to which it was attached.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.72
 */
public class Detached extends TriggeredEvent {

	/**
	 * Create an instance of MEventDetached by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test on detached card [...]</li>
	 * <li>test on card that we were detached to [...]</li>
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
	Detached(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		testFrom = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of MEventBecomeUnTapped specifying all attributes of
	 * this class. All parameters are copied, not cloned. So this new object
	 * shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          test on detached card
	 * @param testFrom
	 *          test on card that we were detached to
	 * @param card
	 *          is the card owning this card
	 */
	private Detached(int idPlace, Test test, Test testFrom, MCard card) {
		super(idPlace, test, card);
		this.testFrom = testFrom;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new Detached(idZone, test, testFrom, card);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param detached
	 *          the detached card
	 * @param from
	 *          the card that we were detached to
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard detached, MCard from) {
		return test(ability, detached) && testFrom.test(ability, from);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param detached
	 *          the detached card
	 * @param from
	 *          the card that we were detached to
	 * @see #isMatching(Ability, MCard, MCard)
	 */
	public static void dispatchEvent(MCard detached, MCard from) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (ability.isMatching()
					&& ((Detached) ability.eventComing()).isMatching(ability, detached,
							from)) {
				ability.triggerIt(new MContextCardCardIntInt(detached, from));
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
	public static final Event EVENT = Event.DETACHED_CARD;

	/**
	 * The test to apply on the card that we were detached to
	 */
	private Test testFrom;

}