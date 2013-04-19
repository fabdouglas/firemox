/*
 * Created on Sep 3, 2004 
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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.Unregisterable;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class RemoveMe extends MAction implements StandardAction {

	/**
	 * Creates a new instance of RemoveMe <br>
	 * 
	 * @param modifier
	 */
	public RemoveMe(Unregisterable modifier) {
		this.modifier = modifier;
	}

	/**
	 * Return the index of this action. As default, this is a zero id
	 * 
	 * @return the index of this action.
	 * @see Actiontype
	 */
	@Override
	public final Actiontype getIdAction() {
		return Actiontype.REMOVE_ME;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		modifier.removeFromManager();
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "remove modifier : " + modifier;
	}

	/**
	 * The modifier to remove with this action
	 */
	public Unregisterable modifier;

}