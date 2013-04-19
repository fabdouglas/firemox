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
package net.sf.firemox.operation;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.expression.Expression;

/**
 * Represent an operation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class Operation {

	/**
	 * Creates a new instance of Operation <br>
	 */
	protected Operation() {
		super();
	}

	/**
	 * return the next Expression read from the current offset
	 * 
	 * @param inputFile
	 *          is the file containing this expression
	 * @return the next Expression read from the current offset
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public abstract Expression readNextExpression(InputStream inputFile)
			throws IOException;

	/**
	 * Process a binary operation with two values and return the result.
	 * 
	 * @param leftValue
	 *          the left value of binary operation
	 * @param rightValue
	 *          the right value of binary operation
	 * @return the result
	 */
	public abstract int process(int leftValue, int rightValue);

	/**
	 * Return operator name
	 * 
	 * @return the operator name
	 */
	public abstract String getOperator();

	/**
	 * Is this operation is useless or not
	 * 
	 * @param leftValue
	 *          the left value
	 * @param rightValue
	 *          the right value
	 * @return true if this operation will not modify the given left value
	 *         considering the right value.
	 */
	public boolean isUselessWith(int leftValue, int rightValue) {
		return leftValue == process(leftValue, rightValue);
	}

	@Override
	public String toString() {
		return getOperator();
	}
}