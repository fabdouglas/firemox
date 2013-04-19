/*
 * MEventDuringPhase.java 
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
 * 
 */
package net.sf.firemox.event.phase;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.Event;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.test.Or;
import net.sf.firemox.test.Test;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class BeginningPhase extends PhaseEvent {

	/**
	 * Create an instance of MEventBeginningPhase by reading a file Offset's file
	 * must pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>idPhase(phase identifier) [2]</li>
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
	public BeginningPhase(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
	}

	/**
	 * Creates a new instance of MEventBeginningPhase specifying all attributes of
	 * this class. All parameters are copied, not cloned. So this new object
	 * shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the additional test
	 * @param card
	 *          is the card owning this card
	 * @param idPhase
	 *          the looked for phase identifier
	 * @param phaseFilter
	 *          the filter.
	 */
	public BeginningPhase(int idZone, Test test, MCard card, Expression idPhase,
			PhaseFilter phaseFilter) {
		super(idZone, test, card, idPhase, phaseFilter);
	}

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.BEGINNING_PHASE;

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability. <br>
	 */
	public static void dispatchEvent() {
		dispatchEvent(EVENT);
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
		return new BeginningPhase(idZone, test, card, idPhase, phaseFilter);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return LanguageManagerMDB.getString("event-bop", LanguageManagerMDB
				.getString(EventManager.turnStructure[idPhase.getValue(ability, null,
						context)].phaseName));
	}

	@Override
	public MEventListener appendOr(MEventListener other) {
		if (((PhaseEvent) other).idPhase == idPhase) {
			return new BeginningPhase(idZone, Or.append(this.test, other.test), card,
					idPhase, phaseFilter);
		}
		return null;
	}
}