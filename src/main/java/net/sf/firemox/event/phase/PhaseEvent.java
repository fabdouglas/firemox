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
 * MEventDuringPhase.java
 * Created on 27 janv. 2004
 */
package net.sf.firemox.event.phase;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.Event;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdConst;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
abstract class PhaseEvent extends TriggeredEvent {

	/**
	 * Create an instance of MEventPhase by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>idPhase(phase identifier) : Expression [2]</li>
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
	protected PhaseEvent(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		phaseFilter = PhaseFilter.valueOf(inputFile);
		idPhase = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Creates a new instance of MEventPhase specifying all attributes of this
	 * class. All parameters are copied, not cloned. So this new object shares the
	 * card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 * @param idPhase
	 *          represents the phase we are looking for
	 * @param phaseFilter
	 *          the filter.
	 */
	protected PhaseEvent(int idZone, Test test, MCard card, Expression idPhase,
			PhaseFilter phaseFilter) {
		super(idZone, test, card);
		this.idPhase = idPhase;
		this.phaseFilter = phaseFilter;
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return true if the current event match with this event
	 */
	protected boolean isMatching(Ability ability) {
		final int value = idPhase.getValue(ability, null, null);
		return (value == IdConst.NO_CARE || phaseFilter.getPhaseFilter() == value)
				&& test(ability, StackManager.currentPlayer());
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability. <br>
	 * Note that the specified currentIdPhase corresponds to the phase identifier
	 * and not to te phase index. So if a same phase is present two times in the
	 * turn structure, and we are looking for it's end, this event would be
	 * activated twice.
	 * 
	 * @param event
	 *          the phase idEvent
	 */
	protected static final void dispatchEvent(Event event) {
		for (Ability ability : TRIGGRED_ABILITIES.get(event)) {
			if (((PhaseEvent) ability.eventComing()).isMatching(ability)
					&& ability.isMatching()) {
				/**
				 * this ability matches with the current event, so we add it to the
				 * triggered buffer zone
				 */
				ability.triggerIt(new MContextCardCardIntInt(StackManager
						.currentPlayer(), ((PhaseEvent) ability.eventComing()).phaseFilter
						.getPhaseFilter()));
			}
		}
	}

	/**
	 * Return the idEvent of this event
	 * 
	 * @return the idEvent of this event
	 */
	@Override
	public abstract Event getIdEvent();

	/**
	 * Return a copy of this with the specified owner
	 * 
	 * @param card
	 *          is the card of the ability of this event
	 * @return copy of this event
	 */
	@Override
	public abstract MEventListener clone(MCard card);

	/**
	 * Represents the phase we are looking for
	 */
	protected Expression idPhase;

	/**
	 * The phase filter to use.
	 */
	protected PhaseFilter phaseFilter;

}