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
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.operation.Any;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.Register;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54 suport either player, either card registers modification
 * @since 0.80 the looked for operation is added
 * @since 0.83 support the general register modification (both player and card)
 * @since 0.85 useless operation are ignored and not genrated.
 */
public class ModifiedRegister extends TriggeredEvent {

	/**
	 * Create an instance of MEventModifiedRegister by reading a file Offset's
	 * file must pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test for source of modification[Test]</li>
	 * <li>test for modified component[Test]</li>
	 * <li>operation [Operation]</li>
	 * <li>register [Register]</li>
	 * <li>index [Expression]</li>
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
	ModifiedRegister(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		this.testModified = TestFactory.readNextTest(inputFile);
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
		this.register = Register.deserialize(inputFile);
		this.index = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Creates a new instance of MEventModifiedRegister specifying all attributes
	 * of this class. All parameters are copied, not cloned. So this new object
	 * shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param sourceTest
	 *          the test applied on card modifying the card.
	 * @param testModified
	 *          the test applied on the modified component.
	 * @param card
	 *          is the card owning this card
	 * @param op
	 *          the looked for operation. Any or specific operation instance.
	 * @param register
	 *          the modified register
	 * @param index
	 *          the modified register.
	 */
	public ModifiedRegister(int idZone, Test sourceTest, Test testModified,
			MCard card, Operation op, Register register, Expression index) {
		super(idZone, sourceTest, card);
		this.testModified = testModified;
		this.op = op;
		this.register = register;
		this.index = index;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new ModifiedRegister(idZone, test, testModified, card, op, register,
				index);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param modified
	 *          the component containing the modified register. May be null if the
	 *          container was not a card.
	 * @param source
	 *          the card modifying the requested register.
	 * @param op
	 *          the looked for operation triggering this event
	 * @param register
	 *          the modified register
	 * @param index
	 *          the modified register's index
	 * @return true if the current event match with this event
	 */
	protected boolean isMatching(Ability ability, Target modified, MCard source,
			Operation op, int register, int index) {
		return (this.register.ordinal() == register || this.register.ordinal() == IdTokens.TARGET)
				&& index == this.index.getValue(ability, modified, null)
				&& (this.op == op || this.op == Any.getInstance())
				&& testModified.test(ability, modified) && test(ability, source);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability
	 * 
	 * @param modified
	 *          the component containing the modified register. May be null if the
	 *          container was not a card.
	 * @param pSource
	 *          the card modifying the requested register.
	 * @param register
	 *          the modified register
	 * @param index
	 *          the modified register's index
	 * @param op
	 *          the operation
	 * @param rightValue
	 *          the right value of this modification
	 * @since 0.85 useless operation are ignored and not genrated.
	 */
	public static final void dispatchEvent(Target modified, MCard pSource,
			int register, int index, Operation op, int rightValue) {
		MCard source = pSource;
		if (source == SystemCard.instance
				&& StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt
				&& ((MContextCardCardIntInt) StackManager.triggered.getAbilityContext())
						.getCard2() != null) {
			source = ((MContextCardCardIntInt) StackManager.triggered
					.getAbilityContext()).getCard2();
		}

		// propagate the existing "context2" value
		final int context2 = StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
				.getAbilityContext()).getValue2()
				: index;

		for (Ability ability : TRIGGRED_ABILITIES.get(EVENT)) {
			try {
				if (ability.isMatching()
						&& ((ModifiedRegister) ability.eventComing()).isMatching(ability,
								modified, source, op, register, index)) {
					ability.triggerIt(new MContextCardCardIntInt(modified, source,
							rightValue, context2, modified.getTimestamp() + 1, source
									.getTimestamp() + 1));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new InternalError("Error while testing ability : " + ability);
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
	 * @param modified
	 *          the component containing the modified register. May be null if the
	 *          container was not a card.
	 * @param pSource
	 *          the card modifying the requested register.
	 * @param register
	 *          the modified register
	 * @param index
	 *          the modified register's index
	 * @param op
	 *          the operation
	 * @param rightValue
	 *          the right value of this modification
	 * @return true if and only if no replacement abilities have been activated
	 */
	public static final boolean tryAction(Target modified, MCard pSource,
			int register, int index, Operation op, int rightValue) {
		MCard source = pSource;
		if (pSource == SystemCard.instance
				&& StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt
				&& ((MContextCardCardIntInt) StackManager.triggered.getAbilityContext())
						.getCard2() != null) {
			source = ((MContextCardCardIntInt) StackManager.triggered
					.getAbilityContext()).getCard2();
		}

		final int context2 = StackManager.getInstance().getAbilityContext() != null
				&& StackManager.triggered.getAbilityContext() instanceof MContextCardCardIntInt ? ((MContextCardCardIntInt) StackManager.triggered
				.getAbilityContext()).getValue2()
				: 0;

		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			List<AbstractCard> result = null;
			for (ReplacementAbility ability : map.get(priority)) {
				if (((ModifiedRegister) ability.eventComing()).isMatching(ability,
						modified, source, op, register, index)) {
					if (result == null) {
						result = new ArrayList<AbstractCard>();
					}
					result.add(ability.getTriggeredClone(new MContextCardCardIntInt(
							modified, source, rightValue, context2)));
				}
			}
			if (!manageReplacement(source, result, "modified register")) {
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
	public static final Event EVENT = Event.MODIFIED_REGISTER;

	/**
	 * The token we are looking for
	 */
	protected Register register;

	/**
	 * The register index
	 */
	protected Expression index;

	/**
	 * represent the test of the modified component
	 */
	protected Test testModified;

	/**
	 * The looked for operation
	 */
	protected Operation op;
}