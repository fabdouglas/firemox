/*
 * Created on Nov 8, 2004 
 * Original filename was BinaryExpression.java
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
package net.sf.firemox.expression;

import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class BinaryExpression extends Expression {

	/**
	 * Creates a new instance of BinaryExpression <br>
	 * 
	 * @param op
	 *          the binart operator
	 * @param left
	 *          the left expression of this binary expression
	 * @param right
	 *          the right expression of this binary expression
	 */
	public BinaryExpression(Operation op, Expression left, Expression right) {
		super();
		this.op = op;
		this.left = left;
		this.right = right;
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return op.process(left.getValue(ability, tested, context), right.getValue(
				ability, tested, context));
	}

	@Override
	public final void extractTriggeredEvents(List<MEventListener> res,
			MCard source, Test globalTest) {
		left.extractTriggeredEvents(res, source, globalTest);
		right.extractTriggeredEvents(res, source, globalTest);
	}

	/**
	 * The left expression of this expression
	 */
	private Expression left;

	/**
	 * The right expression of this expression
	 */
	private Expression right;

	/**
	 * The operation used by this expression
	 */
	private Operation op;

}
