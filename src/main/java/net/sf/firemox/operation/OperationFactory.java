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
package net.sf.firemox.operation;

/**
 * Operation Factory.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class OperationFactory {

	/**
	 * Prevent this class to be instanciated.
	 */
	private OperationFactory() {
		// Nothing to do
	}

	/**
	 * Read and return the correponding operation to the specified operation
	 * identifier
	 * 
	 * @param idOperation
	 *          the operation identifier
	 * @return the operation object corresponding to the specified operation
	 *         identifier
	 */
	public static Operation getOperation(IdOperation idOperation) {
		switch (idOperation) {
		case ADD:
			return Add.getInstance();
		case ADD_ROUNDED:
			return AddHalfRounded.getInstance();
		case ADD_TRUNCATED:
			return AddHalfTruncated.getInstance();
		case AND:
			return And.getInstance();
		case AND_NOT:
			return AndNot.getInstance();
		case ANY:
			return Any.getInstance();
		case CLEAR:
			return Clear.getInstance();
		case DIV_ROUNDED:
			return DivRounded.getInstance();
		case DIV_TRUNCATED:
			return DivTruncated.getInstance();
		case INT_LIST:
			return IntList.getInstance();
		case INCREMENT:
			return Increment.getInstance();
		case DECREMENT:
			return Decrement.getInstance();
		case NEGATIVE:
			return Negative.getInstance();
		case MAXIMUM:
			return Maximum.getInstance();
		case MINIMUM:
			return Minimum.getInstance();
		case REMOVE:
		case MINUS:
			return Remove.getInstance();
		case MULT:
			return Mult.getInstance();
		case OR:
			return Or.getInstance();
		case SET:
			return Set.getInstance();
		case TARGET_LIST:
			return TargetList.getInstance();
		case XOR:
			return Xor.getInstance();

			// added abstract operation to represent final value
		case INT_VALUE:
			return IntValue.getInstance();
		default:
			return Dummy.getInstance();
		}
	}

}
