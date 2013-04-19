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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.ObjectArray;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdCardColors;

/**
 * This action is used to modifiy the amount of required mana.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 */
class ModifyRequiredMana extends ModifyRegister implements InitAction,
		ChosenAction {

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
	ModifyRequiredMana(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MODIFY_REQUIRED_MANA;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		if (actionContext.actionContext == null) {
			actionContext.actionContext = new ObjectArray<Integer>(
					IdCardColors.CARD_COLOR.length);
		}
		int index = this.index.getValue(ability, null, context);
		int value = valueExpr.getValue(ability, ability.getCard(), context);
		StackManager.actionManager.updateRequiredMana(op, index, value);
		return true;
	}

	public boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return true;
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return true;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final int reg = index.getValue(ability, null, context);
		StackManager.actionManager.updateRequiredMana(op, reg, getValue(ability,
				ability.getCard(), context));
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "modify-required-mana";
	}

	public void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// nothing to do
	}

	public String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		return toHtmlString(ability, context);
	}
}