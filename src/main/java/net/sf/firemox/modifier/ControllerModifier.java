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
package net.sf.firemox.modifier;

import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class ControllerModifier extends Modifier {

	/**
	 * Creates a new instance of ControllerModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param newController
	 *          the new controller to set
	 */
	public ControllerModifier(ModifierContext context, TestOn newController) {
		super(context);
		this.newController = newController;
	}

	/**
	 * Return the modified controller
	 * 
	 * @param oldPlayer
	 *          The old player.
	 * @return the modified controller.
	 */
	public Player getPlayer(Player oldPlayer) {
		if (activated) {
			if (next != null) {
				return ((ControllerModifier) next).getPlayer(newController.getPlayer(
						ability, null));
			}
			return newController.getPlayer(ability, null);
		}
		if (next != null) {
			return ((ControllerModifier) next).getPlayer(oldPlayer);
		}
		return oldPlayer;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshController(to);
			}
			return next;
		}
		return super.removeModifier(modifier);
	}

	@Override
	public void removeFromManager() {
		super.removeFromManager();
		if (!unregisteredModifier) {
			to.removeModifier(this);
		}
		unregisteredModifier = true;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		activated = whileCondition.test(ability, to);

		// this controller has changed
		if (oldActivated != activated) {
			StackManager.postRefreshController(to);
		}
	}

	/**
	 * The new controller to set
	 */
	private TestOn newController;

}
