/*
 * Created on 20 févr. 2004
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
package net.sf.firemox.action;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.LethalDamaged;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * Raise the 'lethaldamage' event that should be captured by an defined ability
 * of play. This action raise ONLY this event, and should be called at the end
 * of any ability dealting damages to update cards. <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.63
 * @see net.sf.firemox.event.Event#LETHAL_DAMAGE
 */
final class LethalDamage extends UserAction implements LoopAction {

	/**
	 * Create an instance of MActionUpdateCard by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 */
	private LethalDamage() {
		super((String) null);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.LETHAL_DAMAGE;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		final MCard card = (MCard) StackManager.getInstance().getTargetedList()
				.get(loopingIndex);
		if (checkTimeStamp(context, card) && LethalDamaged.tryAction(card)) {
			LethalDamaged.dispatchEvent(card);
			return true;
		}
		return false;
	}

	public int getStartIndex() {
		return StackManager.getInstance().getTargetedList().size() - 1;
	}

	@Override
	public String toString(Ability ability) {
		return "Lethal damage";
	}

	/**
	 * The unique instance of this class
	 */
	public static LethalDamage instance = new LethalDamage();

}