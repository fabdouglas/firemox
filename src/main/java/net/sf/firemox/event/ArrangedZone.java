/*
 * ArrangedZone.java 
 * Created on 29 janv. 2004
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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdZones;

/**
 * When a zone is arranged.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class ArrangedZone extends TriggeredEvent {

	/**
	 * Create an instance of MEventListener by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>matched zone [1]</li>
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
	ArrangedZone(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		arrangedZone = inputFile.read();
	}

	/**
	 * Creates a new instance of CanICast specifying all attributes of this class.
	 * All parameters are copied, not cloned. So this new object shares the card
	 * and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the additional test of this event
	 * @param card
	 *          is the card owning this card
	 * @param arrangedZone
	 *          the arranged zone
	 */
	public ArrangedZone(int idZone, Test test, MCard card, int arrangedZone) {
		super(idZone, test, card);
		this.arrangedZone = arrangedZone;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new ArrangedZone(idZone, test, card, arrangedZone);
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
	 * @param arrangedZone
	 *          the arranged zone
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, MCard source, int arrangedZone) {
		return test.test(ability, source)
				&& (this.arrangedZone == IdZones.ANYWHERE || this.arrangedZone == arrangedZone);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param arrangedZone
	 *          the arranged zone
	 * @see #isMatching(Ability, MCard, int)
	 */
	public static void dispatchEvent(int arrangedZone) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (((ArrangedZone) ability.eventComing()).isMatching(ability, ability
					.getCard(), arrangedZone)) {
				ability.triggerIt(null);
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
	public static final Event EVENT = Event.ARRANGED_ZONE;

	/**
	 * The arrangedZone
	 */
	private int arrangedZone;

}