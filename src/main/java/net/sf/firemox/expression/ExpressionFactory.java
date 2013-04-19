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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.operation.Dummy;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;

/**
 * Expression factory.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public final class ExpressionFactory {

	private ExpressionFactory() {
		// Nothing to do
	}

	/**
	 * Return the next Expression read from the current offset
	 * 
	 * @param input
	 *          is the stream containing this expression.
	 * @return the next Expression read from the current offset.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static Expression readNextExpression(InputStream input)
			throws IOException {
		final IdOperation idOperation = IdOperation.deserialize(input);
		final Operation op = OperationFactory.getOperation(idOperation);
		if (op == Dummy.getInstance()) {
			return Dummy.getInstance().readNextExpression(idOperation, input);
		}
		return op.readNextExpression(input);
	}

}
