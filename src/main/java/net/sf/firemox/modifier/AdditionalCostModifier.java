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

import net.sf.firemox.stack.AdditionalCost;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 Operation is used instead of add/remove boolean
 *        <code>hasNot</code>
 */
public class AdditionalCostModifier extends Modifier {

	/**
	 * Creates a new instance of ColorModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param additionalCost
	 *          The additional cost.
	 */
	public AdditionalCostModifier(ModifierContext context,
			AdditionalCost additionalCost) {
		super(context);
		this.additionalCost = additionalCost;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.getInstance().getAdditionalCost().remove(additionalCost);
			}
			return next;
		}
		return super.removeModifier(modifier);
	}

	@Override
	public final void removeFromManager() {
		super.removeFromManager();
		unregisteredModifier = true;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		activated = whileCondition.test(ability, null);

		// this color has changed
		if (oldActivated != activated) {
			if (activated)
				StackManager.getInstance().getAdditionalCost().add(additionalCost);
			else
				StackManager.getInstance().getAdditionalCost().remove(additionalCost);
		}
	}

	/**
	 * The additional cost.
	 */
	protected final AdditionalCost additionalCost;

}