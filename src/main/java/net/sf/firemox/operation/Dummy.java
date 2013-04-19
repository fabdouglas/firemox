/*
 * Created on 19 mars 2005
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
 */
package net.sf.firemox.operation;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.expression.BaseRegisterIntValue;
import net.sf.firemox.expression.BitCount;
import net.sf.firemox.expression.CardColors;
import net.sf.firemox.expression.CardProperties;
import net.sf.firemox.expression.CardTypes;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.HighestAmong;
import net.sf.firemox.expression.IfThenElse;
import net.sf.firemox.expression.LowestAmong;
import net.sf.firemox.expression.Position;

/**
 * No standard expression is associated to this operation. Special expression
 * such as {@link net.sf.firemox.expression.BitCount} are plugged in this
 * operation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public final class Dummy extends Operation {

	/**
	 * Creates a new instance of Dummy <br>
	 */
	private Dummy() {
		super();
	}

	@Override
	public Expression readNextExpression(InputStream input) {
		throw new InternalError("should not be called");
	}

	/**
	 * Return operator name
	 * 
	 * @return the operator name
	 */
	@Override
	public String getOperator() {
		return "dummy";
	}

	@Override
	public int process(int leftValue, int rightValue) {
		return 0;
	}

	/**
	 * return the next Expression read from the current offset
	 * 
	 * @param idExpression
	 *          expression type
	 * @param input
	 *          is the file containing this expression
	 * @return the next Expression read from the current offset
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public Expression readNextExpression(IdOperation idExpression,
			InputStream input) throws IOException {
		switch (idExpression) {
		case IF_THEN_ELSE:
			return new IfThenElse(input);
		case HIGHEST_AMONG:
			return new HighestAmong(input);
		case LOWEST_AMONG:
			return new LowestAmong(input);
		case POSITION:
			return new Position(input);
		case BIT_COUNT:
			return new BitCount(input);
		case BASE_REGISTER_INT_VALUE:
			return new BaseRegisterIntValue(input);
		case CARD_COLORS:
			return new CardColors(input);
		case CARD_TYPES:
			return new CardTypes(input);
		case CARD_PROPERTIES:
			return new CardProperties(input);
		case COUNTER:
			return new net.sf.firemox.expression.Counter(input);
		case REF_VALUE:
			return new net.sf.firemox.expression.ReferenceValue(input);
		case STRING_METHOD:
			return new net.sf.firemox.expression.StringMethod(input);
		case OBJECT_VALUE:
			return new net.sf.firemox.expression.ObjectValue(input);
		case MANA_PAID:
			return new net.sf.firemox.expression.ManaPaid(input);
		case ABSTRACT_VALUE:
			return new net.sf.firemox.expression.AbstractValue(input);
		case REGISTER_ACCESS:
			return new net.sf.firemox.expression.RegisterAccess(input);
		case TEST_ON:
			return new net.sf.firemox.expression.TestOn(input);
		case TO_CODE:
			return new net.sf.firemox.expression.ToCode(input);
		case TO_INDEX:
			return new net.sf.firemox.expression.ToIndex(input);
		case DECK_COUNTER:
			return new net.sf.firemox.expression.DeckCounter(input);
		default:
			throw new InternalError("Unknown expression identifier : " + idExpression);
		}
	}

	/**
	 * Return the unique instance of this operation.
	 * 
	 * @return the unique instance of this operation.
	 */
	public static Dummy getInstance() {
		if (instance == null)
			instance = new Dummy();
		return instance;
	}

	/**
	 * The unique instance of this operation
	 */
	private static Dummy instance = null;
}
