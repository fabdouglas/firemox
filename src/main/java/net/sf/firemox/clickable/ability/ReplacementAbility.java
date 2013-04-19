/*
 * Created on Nov 15, 2004 
 * Original filename was ReplacementAbility.java
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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * TODO is it important to keep cost ?
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class ReplacementAbility extends TriggeredAbility {

	/**
	 * Creates a new instance of ReplacementAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [ActivatedAbility]</li>
	 * </ul>
	 * 
	 * @param input
	 *          stream containing this ability
	 * @param card
	 *          referenced card owning this ability.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public ReplacementAbility(InputStream input, MCard card) throws IOException {
		super(input, card);
	}

	/**
	 * Create a fresh instance from another instance of ReplacementAbility
	 * 
	 * @param other
	 *          the instance to clone.
	 * @param event
	 *          The attached activation event.
	 */
	private ReplacementAbility(ReplacementAbility other, MEventListener event) {
		super(other, event);
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		throw new InternalError("Replacement ability cannot trigger");
	}

	@Override
	public void resolveStack() {
		/*
		 * If the current action (the one causing this replacement ability to be
		 * played) is an instance of LoopingAction, it's broken. The remaining
		 * iterations are skipped.
		 */
		if (optimizer == Optimization.action) {
			StackManager.actionManager.setHop(1);
		}
		StackManager.resolveStack();
	}

	@Override
	public Ability clone(MCard container) {
		return new ReplacementAbility(this, eventComing.clone(container));
	}

	@Override
	public boolean isMatching() {
		/*
		 * assuming the event has been checked, we verify this ability has not
		 * already been played in order to prevent infinite loop.
		 */
		return !StackManager.isPlaying(this);
	}

	@Override
	public void removeFromManager() {
		priority.removeFromManager(this);
		if (delayedCard != null) {
			// remove the delayed card from the DBZ, once.
			StackManager.getSpellController().zoneManager.delayedBuffer
					.remove(delayedCard);
			// and remove the linked 'until' abilities to free the useless listeners
			delayedCard.removeFromManager();
			delayedCard = null;
		}
	}

	@Override
	public final boolean isAutoResolve() {
		return true;
	}

	@Override
	public final boolean isHidden() {
		return true;
	}

	@Override
	public final boolean hasHighPriority() {
		return true;
	}

	@Override
	public void registerToManager() {
		priority.registerToManager(this);
	}
}
