/*
 * Created on Dec 20, 2004
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
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * Push in the stack and resolve hiddden abilities present in the TBZs. No
 * player gets priority.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
final class ResolveHidden extends UserAction implements StandardAction {

	/**
	 */
	private ResolveHidden() {
		super((String) null);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.RESOLVE_HIDDEN;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		StackManager.processRefreshRequests();
		StackManager.actionManager.setHop(-1);
		if (!StackManager.activePlayer().processHiddenTriggered()) {
			StackManager.actionManager.setHop(1);
			return true;
		}
		return false;
	}

	@Override
	public String toString(Ability ability) {
		return "Remove resolved damages";
	}

	/**
	 * The unique instance of this class
	 */
	public static ResolveHidden instance = new ResolveHidden();

}