/*
 * Created on Aug 31, 2004 
 * Original filename was Xor.java
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

/**
 * The Exclusive Or operation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 */
public final class Xor extends BinaryOperation {

	/**
	 * Creates a new instance of Xor <br>
	 */
	private Xor() {
		super();
	}

	@Override
	public int process(int leftValue, int rightValue) {
		return leftValue & ~rightValue | leftValue & ~rightValue;
	}

	@Override
	public String getOperator() {
		return "LxorR";
	}

	/**
	 * Return the unique instance of this operation.
	 * 
	 * @return the unique instance of this operation.
	 */
	public static Operation getInstance() {
		if (instance == null)
			instance = new Xor();
		return instance;
	}

	/**
	 * The unique instance of this operation
	 */
	private static Operation instance = null;
}