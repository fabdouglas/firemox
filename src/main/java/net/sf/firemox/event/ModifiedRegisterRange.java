/*
 * MEventModifiedRegisterRange.java
 * Created on 2005/08/23
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
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.operation.Any;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.Register;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class ModifiedRegisterRange extends ModifiedRegister {

	/**
	 * Create an instance of MEventModifiedRegisterRange by reading a file
	 * Offset's file must pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>sup [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ModifiedRegisterRange(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
		sup = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Creates a new instance of MEventModifiedRegisterRange specifying all
	 * attributes of this class. All parameters are copied, not cloned. So this
	 * new object shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param sourceTest
	 *          the test applied on card modifying the card.
	 * @param testModified
	 *          the test applied on the modified component.
	 * @param card
	 *          is the card owning this card
	 * @param op
	 *          the looked for operation. Any or specific operation instance.
	 * @param register
	 *          the modified register
	 * @param inf
	 *          the inferior range looked for this event
	 * @param sup
	 *          the superior range looked for this event
	 */
	public ModifiedRegisterRange(int idZone, Test sourceTest, Test testModified,
			MCard card, Operation op, Register register, Expression inf,
			Expression sup) {
		super(idZone, sourceTest, testModified, card, op, register, inf);
		this.sup = sup;
	}

	@Override
	public MEventListener clone(MCard card) {
		return new ModifiedRegisterRange(idZone, test, testModified, card, op,
				register, index, sup);
	}

	@Override
	public boolean isMatching(Ability ability, Target modified, MCard source,
			Operation op, int register, int index) {
		final int indexValue = this.index.getValue(ability, modified, null);
		return this.register.ordinal() == register && indexValue <= index
				&& indexValue >= index
				&& (this.op == op || this.op == Any.getInstance())
				&& testModified.test(ability, modified) && test(ability, source);
	}

	/**
	 * The superior range looked for this event
	 */
	protected Expression sup;

}