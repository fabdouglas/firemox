/*
 * Created on Aug 31, 2004 
 * Original filename was UnaryOperation.java
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
package net.sf.firemox.operation;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.UnaryExpression;

/**
 * Represents an unary operation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public abstract class UnaryOperation extends Operation {

	/**
	 * Creates a new instance of UnaryOperation <br>
	 */
	public UnaryOperation() {
		super();
	}

	@Override
	public Expression readNextExpression(InputStream inputFile)
			throws IOException {
		return new UnaryExpression(this, ExpressionFactory
				.readNextExpression(inputFile));
	}

	@Override
	public abstract int process(int leftValue, int rightValue);

	@Override
	public abstract String getOperator();
}