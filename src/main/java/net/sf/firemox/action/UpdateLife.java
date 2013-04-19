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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.UpdatedLife;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * Raise the 'updatelife' event that should be captured by an defined ability of
 * play. This action raise ONLY this event, and should be called at the end of
 * any ability dealting damages to update player status.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.63
 * @see net.sf.firemox.event.Event#ID__UPDATE_LIFE
 */
final class UpdateLife extends UserAction implements StandardAction {

	/**
	 */
	private UpdateLife() {
		super((String) null);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.UPDATE_LIFE;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		UpdatedLife.dispatchEvent();
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
		return "Update the player's life";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class
	 */
	public static UserAction getInstance() {
		if (instance == null) {
			instance = new UpdateLife();
		}
		return instance;
	}

	/**
	 * The unique instance of this class
	 */
	private static UserAction instance;

}