/*
 * Created on Nov 8, 2004 
 * Original filename was UnaryExpression.java
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

import java.io.IOException;
import java.io.InputStream;
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
public class UnaryExpression extends Expression {

	/**
	 * Creates a new instance of UnaryExpression <br>
	 * 
	 * @param op
	 *          the operation used by this expression.
	 * @param expr
	 *          the expression used for this expression
	 */
	public UnaryExpression(Operation op, Expression expr) {
		super();
		this.op = op;
		this.expr = expr;
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @param op
	 *          optional operation.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */

	public UnaryExpression(InputStream inputFile, Operation op)
			throws IOException {
		expr = ExpressionFactory.readNextExpression(inputFile);
		this.op = op;
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return op.process(expr.getValue(ability, tested, context), 0);
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		expr.extractTriggeredEvents(res, source, globalTest);
	}

	@Override
	public boolean canBePreempted() {
		return expr.canBePreempted();
	}

	/**
	 * The operation used by this expression
	 */
	private final Operation op;

	/**
	 * The expression used for this expression
	 */
	protected final Expression expr;

}
