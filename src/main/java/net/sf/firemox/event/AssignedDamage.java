/*
 * MEventDamaged.java 
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
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.80 support replacement
 */
public class AssignedDamage extends TriggeredEvent {

	/**
	 * Create an instance of MEventDamaged by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>damage type[Expression]</li>
	 * <li>test source[Test]</li>
	 * <li>test destination[Test]</li>
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
	public AssignedDamage(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		damageType = ExpressionFactory.readNextExpression(inputFile);
		testSource = TestFactory.readNextTest(inputFile);
		testDestination = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of MEventDamaged specifying all attributes of this
	 * class. All parameters are copied, not cloned. So this new object shares the
	 * card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param testSource
	 *          the test to apply on the source of this event
	 * @param testDestination
	 *          the test to apply on destination of this event
	 * @param card
	 *          is the card owning this card
	 */
	private AssignedDamage(int idPlace, Test test, Test testSource,
			Test testDestination, MCard card, Expression damageType) {
		super(idPlace, test, card);
		this.damageType = damageType;
		this.testSource = testSource;
		this.testDestination = testDestination;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new AssignedDamage(idZone, test, testSource, testDestination, card,
				damageType);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param source
	 *          the source of damage
	 * @param destination
	 *          the damaged player/card
	 * @param damageNumber
	 *          the amount of damage to do
	 * @param damageType
	 *          the type of this damage (prevent/dealt, lose life or gain life).
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return true if the current event match with this event
	 */
	public boolean isMatching(MCard source, Target destination, int damageNumber,
			int damageType, Ability ability) {
		return (damageType & this.damageType.getValue(ability, destination, null)) == damageType
				&& test(ability, card)
				&& testSource.test(ability, source)
				&& testDestination.test(ability, destination);
	}

	/**
	 * Dispatch this event to replacement abilites only. If one or several
	 * abilities have been activated this way, this function will return false.
	 * The return value must be checked. In case of <code>false</code> value,
	 * the caller should not call any stack resolution since activated abilities
	 * are being played.
	 * 
	 * @param source
	 *          the source of damage
	 * @param target
	 *          the damaged player/card
	 * @param damageNumber
	 *          the amount of damage to do
	 * @param damageType
	 *          the type of this damage (prevent/dealt, lose life or gain life).
	 * @return true if and only if no replacement abilities have been activated
	 */
	public static boolean tryAction(MCard source, Target target,
			int damageNumber, int damageType) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			for (ReplacementAbility ability : map.get(priority)) {
				if (ability.isMatching()
						&& ((AssignedDamage) ability.eventComing()).isMatching(source,
								target, damageNumber, damageType, ability)) {
					if (result == null) {
						result = new ArrayList<AbstractCard>();
					}
					result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
							target, source, damageNumber, damageType)));
				}
			}
			if (!manageReplacement(source, result, "damaged component")) {
				return false;
			}
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
	 * @param source
	 *          the source of damage
	 * @param target
	 *          the damaged player/card
	 * @param damageNumber
	 *          the amount of damage to do
	 * @param damageType
	 *          the type of this damage (prevent/dealt, lose life or gain life).
	 * @see #isMatching(MCard, Target, int, int, Ability)
	 */
	public static void dispatchEvent(MCard source, Target target,
			int damageNumber, int damageType) {
		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			if (((AssignedDamage) ability.eventComing()).isMatching(source, target,
					damageNumber, damageType, ability)) {
				ability.triggerIt(new MContextCardCardIntInt(target, source,
						damageNumber, damageType));
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
	public static final Event EVENT = Event.DEALTING_DAMAGE;

	/**
	 * represent the test of condition on damage source
	 */
	private Test testSource;

	/**
	 * represent the test of condition on damage destination
	 */
	private Test testDestination;

	/**
	 * Looked for damage type
	 */
	private Expression damageType;
}