/*
 * Created on Oct 26, 2004 
 * Original filename was Any.java
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

import java.io.InputStream;

import net.sf.firemox.expression.Expression;

/**
 * This is an Null operation. No expression can use it. Represent all
 * operations.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 * @see net.sf.firemox.operation.IdOperation
 */
public final class Any extends Operation {

	/**
	 * Creates a new instance of Any <br>
	 */
	private Any() {
		super();
	}

	@Override
	public Expression readNextExpression(InputStream inputFile) {
		throw new InternalError("should not be called");

	}

	@Override
	public int process(int leftValue, int rightValue) {
		throw new InternalError("should not be called");
	}

	/**
	 * Return operator name
	 * 
	 * @return the operator name
	 */
	@Override
	public String getOperator() {
		return "Any";
	}

	/**
	 * Return the unique instance of this operation.
	 * 
	 * @return the unique instance of this operation.
	 */
	public static Operation getInstance() {
		if (instance == null) {
			instance = new Any();
		}
		return instance;
	}

	/**
	 * The unique instance of this operation
	 */
	private static Operation instance = null;
}
