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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.operation.Add;
import net.sf.firemox.operation.Remove;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdTokens;

/**
 * This action is used to modifiy a register of a player or a card. <br>
 * This action can use the target list when is played : the address
 * (idToken=register name + register index) must be IdTokens#TARGET. So the
 * target list must set before this action would be played.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.8
 */
class ModifyTargetListRegister extends ModifyRegister implements LoopAction {

	/**
	 * Create an instance of ModifyTargetListRegister by reading a file Offset's
	 * file must pointing on the first byte of this action <br>
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
	ModifyTargetListRegister(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MODIFY_TARGET_LIST_REGISTER;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		Target target = StackManager.getInstance().getTargetedList().get(
				loopingIndex);
		return ModifyTargetableRegister.modifyRegister(ability.getCard(), target,
				index.getValue(ability, target, context), getValue(ability, target,
						context), op);
	}

	public int getStartIndex() {
		return StackManager.getInstance().getTargetedList().size() - 1;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		throw new InternalError("should not been called");
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
		if (op == Add.getInstance()) {
			if (index == IdTokens.LIFE) {
				return "Gain " + value + " life";
			}
			if (index == IdTokens.POISON) {
				return "Gain " + value + " poison";
			}
			if (index == IdCommonToken.DAMAGE) {
				return "Add " + value + " damage";
			}
		}
		if (op == Remove.getInstance()) {
			if (index == IdTokens.LIFE) {
				return "Lose " + value + " life";
			}
			if (index == IdTokens.POISON) {
				return "Lose " + value + " poison";
			}
			if (index == IdCommonToken.DAMAGE) {
				return "Remove " + value + " damage";
			}
		} else {
			if (index == IdTokens.LIFE) {
				return op.toString() + " " + value + " life";
			}
			if (index == IdTokens.POISON) {
				return op.toString() + " " + value + " poison";
			}
			if (index == IdCommonToken.DAMAGE) {
				return op.toString() + value + " damage";
			}
		}
		// return op.getClass().getSimpleName() + " (L=" + index + ",R=" + idNumber
		// + ")";
		return "";
	}
}