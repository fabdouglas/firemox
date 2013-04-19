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
 */
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.RollBackAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.ModifiedRegister;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.operation.Any;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdTokens;

/**
 * This action is used to modifiy a register of a player or a card. <br>
 * This action can use the target list when is played : the address
 * (idToken=register name + register index) must be IdTokens#TARGET. So the
 * target list must set before this action would be played.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 * @since 0.85 useless operation are ignored and not genrated.
 */
public class ModifyTargetableRegister extends ModifyRegister implements
		ChosenAction, InitAction, RollBackAction {

	/**
	 * Create an instance of ModifyRegister by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ModifyTargetableRegister(InputStream inputFile) throws IOException {
		super(inputFile);
		register = TestOn.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MODIFY_TARGETABLE_REGISTER;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		actionContext.actionContext = new Int(getValue(ability, null, context));
		return true;
	}

	public boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return true;
	}

	public void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// Nothing to do
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return modifyRegister(StackManager.getRealSource(ability.getCard()),
				register.getTargetable(ability, null), index.getValue(ability, null,
						context), ((Int) actionContext.actionContext).getInt(), op);
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// TODO ModyRegister rollback --> not yet implemented
	}

	public String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		return toHtmlString(ability, context);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		return modifyRegister(StackManager.getRealSource(ability.getCard()),
				register.getTargetable(ability, null), index.getValue(ability, null,
						context), getValue(ability, null, context), op);
	}

	/**
	 * Generate event associated to this action. Only one or several events are
	 * generated and may be collected by event listeners. Then play this action
	 * 
	 * @param source
	 *          the source, and the concerned card to this modification
	 * @param target
	 *          the component to modify
	 * @param index
	 *          the register's index
	 * @param value
	 *          right value of operation
	 * @param op
	 *          identifier of the operation applied to the specified card
	 * @return true
	 */
	public static boolean modifyRegister(MCard source, Target target, int index,
			int value, Operation op) {
		if (target.isPlayer())
			return modifyRegister(source, (Player) target, index, op, value);
		return modifyRegister(source, (MCard) target, index, op, value);
	}

	private static boolean modifyRegister(MCard source, Player player, int index,
			Operation op, int rightValue) {
		if (index == IdTokens.MANA_POOL) {
			// no care about operation, we empty the mana pool
			player.mana.setToZero();
			return true;
		}

		// Generate event only if necessary
		if (!op.isUselessWith(index, rightValue)) {
			if (!ModifiedRegister.tryAction(player, source, IdTokens.PLAYER, index,
					op, rightValue)) {
				// this action has been replaced
				return false;
			}
			// we continue : dispatch event, and process to the register modification
			player.setValue(index, op, rightValue);
			ModifiedRegister.dispatchEvent(player, source, IdTokens.PLAYER, index,
					op, rightValue);
		}
		return true;
	}

	private static boolean modifyRegister(MCard source, MCard card, int index,
			Operation op, int rightValue) {
		// Generate event only if necessary
		if (op.isUselessWith(card.getValue(index), rightValue)) {
			return true;
		}
		boolean replaced = false;
		if (index < IdTokens.FIRST_FREE_CARD_INDEX) {
			// notify this modification to the replacement abilities
			replaced = !ModifiedRegister.tryAction(card, source, IdTokens.CARD,
					index, op, rightValue);
		}

		if (!replaced) {
			// also, we notify the modification of this register
			card.setValue(index, op, rightValue);
			if (index < IdTokens.FIRST_FREE_CARD_INDEX) {
				ModifiedRegister.dispatchEvent(card, source, IdTokens.CARD, index, op,
						rightValue);
			}
			return true;
		}
		return false;
	}

	@Override
	public Test parseTest(Test test) {
		final HashMap<String, Expression> values = new HashMap<String, Expression>();
		values.put("%value", valueExpr);
		return test.getConstraintTest(values);
	}

	@Override
	public boolean equal(MAction constraintAction) {
		if (!(constraintAction instanceof ModifyTargetableRegister)) {
			return false;
		}
		final ModifyTargetableRegister other = (ModifyTargetableRegister) constraintAction;
		return (other.op == op || other.op == Any.getInstance())
				&& register == other.register
				&& index instanceof IntValue
				&& ((IntValue) index).value == ((IntValue) index).value
				&& valueExpr instanceof IntValue
				&& (((IntValue) valueExpr).value == IdConst.ALL || ((IntValue) valueExpr).value == ((IntValue) other.valueExpr).value);
	}

	@Override
	public String toString(Ability ability) {
		final int index = this.index.getValue(ability, null, null);
		String value = null;
		try {
			value = "" + valueExpr.getValue(ability, null, null);
		} catch (Exception e) {
			value = "?";
		}
		return op.getClass().getSimpleName() + " (L=" + index + ",R=" + value + ")";
	}

	/**
	 * represents the token to modify
	 */
	protected final TestOn register;
}