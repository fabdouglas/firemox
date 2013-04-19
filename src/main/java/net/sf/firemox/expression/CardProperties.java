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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.CardPropertiesOperation;

/**
 * Expression returning the result of some operations applied on properties of a
 * or two cards.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class CardProperties extends Expression {

	/**
	 * Creates a new instance of CardTypes <br>
	 * <ul>
	 * Structure of InputStream :
	 * <li>operation [CardPropertiesOperation]
	 * <li>card [TestOn]
	 * <li>card2 [TestOn]
	 * <li>lower [Expression]
	 * <li>higher [Expression]
	 * </ul>
	 * 
	 * @param input
	 *          stream containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public CardProperties(InputStream input) throws IOException {
		super();
		operation = CardPropertiesOperation.deserialize(input);
		card = TestOn.deserialize(input);
		card2 = TestOn.deserialize(input);
		lower = ExpressionFactory.readNextExpression(input);
		higher = ExpressionFactory.readNextExpression(input);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return operation.getValue(card.getCard(ability, tested).getProperties(),
				card2.getCard(ability, tested).getProperties(), lower.getValue(ability,
						tested, context), higher.getValue(ability, tested, context));
	}

	/**
	 * Operation to apply on the working properties.
	 */
	private CardPropertiesOperation operation;

	/**
	 * The first tested object.
	 */
	private TestOn card;

	/**
	 * The other tested object.
	 */
	private TestOn card2;

	/**
	 * The minimal property value included used by the operation.
	 */
	private Expression lower;

	/**
	 * The maximal property value included used by the operation.
	 */
	private Expression higher;

}
