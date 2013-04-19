/*
 * Created on Sep 13, 2004 
 * Original filename was MEventModifiedIdCard.java
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
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class ModifiedIdCard extends TriggeredEvent {

	/**
	 * Creates a new instance of ModifiedIdCard <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>test [Test]</li>
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
	ModifiedIdCard(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		idCard = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Creates a new instance of MEventModifiedIdCard <br>
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 * @param idCard
	 */
	public ModifiedIdCard(int idZone, Test test, MCard card, Expression idCard) {
		super(idZone, test, card);
		this.idCard = idCard;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new ModifiedIdCard(idZone, test, card, idCard);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param target
	 *          is the target owning the modified idcard
	 * @param idCard
	 *          the modified color
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(Ability ability, Target target, int idCard) {
		return (this.idCard.getValue(ability, target, null) & idCard) != 0
				&& test(ability, target);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param target
	 *          is the target owning the modified property
	 * @param idCard
	 *          the modified card type
	 * @see #isMatching(Ability, Target, int)
	 */
	public static void dispatchEvent(Target target, int idCard) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (ability.isMatching()
					&& ((ModifiedIdCard) ability.eventComing()).isMatching(ability,
							target, idCard)) {
				ability.triggerIt(new MContextCardCardIntInt(target, idCard));
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
	public static final Event EVENT = Event.MODIFIED_IDCARD;

	/**
	 * The tested card identifier
	 */
	private Expression idCard;

}