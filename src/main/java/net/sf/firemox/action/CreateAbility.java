/*
 * Created on Nov 9, 2004 
 * Original filename was CreateAbility.java
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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.clickable.ability.RemoveModifier;
import net.sf.firemox.clickable.ability.TriggeredAbility;
import net.sf.firemox.clickable.target.card.DelayedCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.EventFactory;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdTokens;

/**
 * Create a new {@link MCard} component and add it to the current target list.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
class CreateAbility extends UserAction implements StandardAction {

	/**
	 * Create an instance of CreateAbility by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [Action]</li>
	 * <li>nb registers to set [int]</li>
	 * <li>register index i [Expression]</li>
	 * <li>expression i [Expression]</li>
	 * <li>idToken of card to save [TestOn]</li>
	 * <li>idToken of card to save [TestOn]</li>
	 * <li>ability To Add [...]</li>
	 * <li>nb exists until this triggered event [int]</li>
	 * <li>exists until this triggered event i [Event]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	CreateAbility(InputStream inputFile) throws IOException {
		super(inputFile);

		registers = new Expression[IdTokens.ABILITY_REGISTER_SIZE];
		indexes = new Expression[registers.length];
		for (int i = inputFile.read(); i-- > 0;) {
			indexes[i] = ExpressionFactory.readNextExpression(inputFile);
			registers[i] = ExpressionFactory.readNextExpression(inputFile);
		}
		save1 = TestOn.deserialize(inputFile);
		save2 = TestOn.deserialize(inputFile);

		abilityToAdd = (TriggeredAbility) AbilityFactory.readAbility(inputFile,
				SystemCard.instance);

		// until events
		int count = inputFile.read();
		until = new ArrayList<MEventListener>();
		while (count-- > 0) {
			until.add(EventFactory.readNextEvent(inputFile, SystemCard.instance));
		}

	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.CREATE_ABILITY;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		final List<Ability> abilities = new ArrayList<Ability>();
		final MCard owner = ability.getCard();
		final TriggeredAbility clonedAbility = (TriggeredAbility) abilityToAdd
				.clone(owner);

		// Intialize registers of this ability. The first 'saved' target is used
		final int[] parsedRegisters = new int[IdTokens.ABILITY_REGISTER_SIZE];
		for (Expression exprIndex : indexes) {
			if (exprIndex != null) {
				int index = exprIndex.getValue(ability, save1.getTargetable(ability,
						owner), context);
				parsedRegisters[index] = registers[index].getValue(ability, save1
						.getTargetable(ability, owner), context);
			}
		}

		// create the delayed card, and put it into the right DBZ
		StackManager.getSpellController().zoneManager.delayedBuffer
				.add(new DelayedCard(clonedAbility, abilities, parsedRegisters, save1
						.getTargetable(ability, owner), save2.getTargetable(ability, owner)));

		// create the 'until' triggers of this ability
		for (MEventListener event : until) {
			abilities.add(new RemoveModifier((TriggeredEvent) event, clonedAbility));
		}

		// register the created 'until' triggers of this ability
		for (Ability removerAbility : abilities) {
			removerAbility.registerToManager();
		}
		clonedAbility.registerToManager();

		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Delayed ability";
	}

	/**
	 * The ability to add
	 */
	private final TriggeredAbility abilityToAdd;

	/**
	 * When this event is generated this modifier is removed from the attached
	 * object. If this event is equal to null, this modifier can be removed only
	 * by the attached component.
	 */
	protected final List<MEventListener> until;

	/**
	 * The private registers of this ability
	 */
	private final Expression[] registers;

	/**
	 * The private registers of this ability
	 */
	private final Expression[] indexes;

	/**
	 * The first target to save. If is 0, no card is saved.
	 */
	private final TestOn save1;

	/**
	 * The second target to save. If is 0, no card is saved.
	 */
	private final TestOn save2;
}