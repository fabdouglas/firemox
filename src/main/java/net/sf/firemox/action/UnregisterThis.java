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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * This action remove the the current ability from the current listeners. The
 * removed ability will not be activated until it has been registered again. The
 * current ability is not aborted, just removed from listeners.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class UnregisterThis extends MAction implements StandardAction {

	/**
	 * Creates a new instance of UnregisterAbility <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * </ul>
	 * 
	 * @param ability
	 *          the ability to remove from the event listeners
	 */
	private UnregisterThis() {
		super();
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.UNREGISTER_THIS;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		StackManager.triggered.triggeredAbility.removeFromManager();
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "remove current ability";
	}

	/**
	 * The unique instance of this class
	 */
	public static UnregisterThis instance = new UnregisterThis();

}