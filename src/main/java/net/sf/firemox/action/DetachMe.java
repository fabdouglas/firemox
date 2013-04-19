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
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.Detached;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class DetachMe extends UserAction implements StandardAction {

	/**
	 * Creates a new instance of DetachMe <br>
	 */
	private DetachMe() {
		super((String) null);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.DETACH_ME;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			final MCard attached = (MCard) StackManager.getInstance()
					.getTargetedList().get(i);
			if (attached.getParent() instanceof MCard)
				Detached.dispatchEvent(attached, (MCard) attached.getParent());
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "raise detach event";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static DetachMe getInstance() {
		if (instance == null) {
			instance = new DetachMe();
		}
		return instance;
	}

	/**
	 * Unique instance of this class.
	 */
	private static DetachMe instance;

}