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
import net.sf.firemox.clickable.target.card.DelayedCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
class ModifyAbilityRegister extends ModifyRegister {

	/**
	 * Create an instance of ModifyAbilityRegister by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li> [super]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ModifyAbilityRegister(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MODIFY_ABILITY_REGISTER;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		DelayedCard delayedCard = StackManager.triggered.getDelayedCard();
		delayedCard.registers[index.getValue(ability, null, context)] = op.process(
				delayedCard.registers[index.getValue(ability, null, context)],
				getValue(ability, null, context));
		return true;
	}

	/**
	 * return the string representation of this action
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return the string representation of this action
	 * @see Object#toString()
	 */
	@Override
	public String toString(Ability ability) {
		return "Modify attachedto register";
	}
}