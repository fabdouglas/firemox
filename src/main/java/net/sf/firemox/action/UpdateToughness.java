/*
 * UpdateToughness.java 
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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * For each card in the target list, raise the 'updatetoughness' event that
 * should be captured by a defined ability of game. This action raise ONLY this
 * event, and should be called at the end of any ability dealting damages to
 * update cards.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.63
 * @see net.sf.firemox.event.Event#ID__UPDATE_TOUGHNESS
 */
final class UpdateToughness extends UserAction implements StandardAction {

	/**
	 */
	private UpdateToughness() {
		super((String) null);
	}

	/**
	 * return the id of this action. This action has been read from the mdb file.
	 * 
	 * @see Actiontype
	 * @return the id of this action
	 */
	@Override
	public Actiontype getIdAction() {
		return Actiontype.UPDATE_TOUGHNESS;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			net.sf.firemox.event.UpdateToughness
					.dispatchEvent((MCard) StackManager.getInstance().getTargetedList()
							.get(i));
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Update the card's toughness";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class
	 */
	public static UserAction getInstance() {
		if (instance == null) {
			instance = new UpdateToughness();
		}
		return instance;
	}

	/**
	 * The unique instance of this class
	 */
	private static UserAction instance;

}