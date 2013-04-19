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
 */
package net.sf.firemox.modifier;

import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.operation.Add;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.Remove;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class AbilityModifier extends Modifier {

	/**
	 * Creates a new instance of AbilityModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param op
	 *          the operation applied to previous value with the value of this
	 *          modifier.
	 * @param abilitiesToAdd
	 *          the list of abilities added/removed by this modifier
	 */
	public AbilityModifier(ModifierContext context, Operation op,
			Ability[] abilitiesToAdd) {
		super(context);
		this.abilitiesToAdd = abilitiesToAdd;
		this.op = op;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshAbilities(to);
			}
			return next;
		}
		return super.removeModifier(modifier);
	}

	@Override
	public final void removeFromManager() {
		super.removeFromManager();
		if (!unregisteredModifier) {
			to.removeModifier(this);
			if (activated && op == Add.getInstance()) {
				// unregister the granted abilities
				for (Ability ability : abilitiesToAdd) {
					ability.removeFromManager();
				}
			}
		}
		unregisteredModifier = true;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		activated = whileCondition.test(ability, to);

		// this card type identifier has changed
		if (oldActivated != activated) {
			StackManager.postRefreshAbilities(to);
		}
	}

	/**
	 * Add/ remove ability of attached object
	 * 
	 * @param toAdd
	 *          list of abilities attached to the object
	 */
	public void calculateDeltaAbilities(List<Ability> toAdd) {
		if (activated) {
			if (op == Add.getInstance()) {
				// add the granted abilities
				for (Ability ability : abilitiesToAdd) {
					toAdd.add(ability);
				}
			} else if (op == Remove.getInstance()) {
				// remove the specified abilities
				for (Ability ability : abilitiesToAdd) {
					toAdd.remove(ability);
				}
			} else {
				// remove all abilities
				toAdd.clear();
			}
		}
		if (next != null) {
			((AbilityModifier) next).calculateDeltaAbilities(toAdd);
		}
	}

	/**
	 * Indicates the operation applied to previous value with the value of this
	 * modifier. Only Remove/add/clear instance.
	 */
	protected Operation op;

	/**
	 * The list of abilities added/removed by this modifier
	 */
	private Ability[] abilitiesToAdd;
}