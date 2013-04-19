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

import net.sf.firemox.expression.Expression;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 Operation is used instead of add/remove boolean
 *        <code>hasNot</code>
 */
public class ColorModifier extends Modifier {

	/**
	 * Creates a new instance of ColorModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param idColor
	 *          the color to add/remove/...
	 * @param op
	 *          the operation applied to previous value with the value of this
	 *          modifier.
	 */
	public ColorModifier(ModifierContext context, Expression idColor, Operation op) {
		super(context);
		this.idColor = idColor;
		this.op = op;
	}

	/**
	 * Return the modified idColor
	 * 
	 * @param oldIdColor
	 *          The previous idColor.
	 * @return the modified idColor.
	 */
	public int getIdColor(int oldIdColor) {
		if (activated) {
			if (next != null) {
				return ((ColorModifier) next).getIdColor(op.process(oldIdColor, idColor
						.getValue(ability, to, null)));
			}
			return op.process(oldIdColor, idColor.getValue(ability, to, null));
		}
		if (next != null) {
			return ((ColorModifier) next).getIdColor(oldIdColor);
		}
		return oldIdColor;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshColor(to);
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
		}
		unregisteredModifier = true;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		activated = whileCondition.test(ability, to);

		// this color has changed
		if (oldActivated != activated) {
			StackManager.postRefreshColor(to);
		}
	}

	/**
	 * The modified idColor
	 */
	protected Expression idColor;

	/**
	 * Indicates the operation applied to previous value with the value of this
	 * modifier.
	 */
	protected Operation op;

}