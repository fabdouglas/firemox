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
 */
public class RegisterModifier extends Modifier {

	/**
	 * Creates a new instance of RegisterModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param index
	 *          the modified index
	 * @param rightExpression
	 *          the expression used to modify the register
	 * @param op
	 *          the operation applied to previous value with the value of this
	 *          modifier.
	 */
	public RegisterModifier(ModifierContext context, int index,
			Expression rightExpression, Operation op) {
		super(context);
		this.op = op;
		this.rightExpression = rightExpression;
		this.index = index;
	}

	@Override
	public void removeFromManager() {
		super.removeFromManager();
		if (!unregisteredModifier) {
			to.removeModifier(this, index);
		}
		unregisteredModifier = true;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshRegisters(to, index);
			}
			return next;
		}
		return super.removeModifier(modifier);
	}

	/**
	 * Return the modified value of the specified one. This value is only modified
	 * if the while condition test succeed.
	 * 
	 * @param oldValue
	 *          the old value
	 * @return the modified value.
	 */
	public int getValue(int oldValue) {
		if (activated) {
			if (next != null) {
				return ((RegisterModifier) next).getValue(op.process(oldValue,
						rightValue));
			}
			return op.process(oldValue, rightValue);
		}
		if (next != null) {
			return ((RegisterModifier) next).getValue(oldValue);
		}
		return oldValue;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		if (whileCondition.test(ability, to)) {
			activated = true;
			int oldValue = rightValue;
			// evaluate the counter
			rightValue = rightExpression.getValue(ability, to, null);
			if (oldValue != rightValue || !oldActivated) {
				StackManager.postRefreshRegisters(to, index);
			}
		} else {
			activated = false;

			if (oldActivated) {
				// this register has changed
				StackManager.postRefreshRegisters(to, index);
			}
		}
	}

	/**
	 * The looked for register index
	 */
	protected int index;

	/**
	 * The operation applied to the left and right values
	 */
	private Operation op;

	/**
	 * right integer value The complex expression to use for the right value. Is
	 * null if the IdToken number is not a complex expression.
	 */
	private Expression rightExpression = null;

	/**
	 * right integer value
	 */
	private int rightValue;

}