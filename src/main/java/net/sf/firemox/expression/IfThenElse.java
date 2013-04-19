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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class IfThenElse extends Expression {

	/**
	 * Creates a new instance of IfThenElse <br>
	 * 
	 * @param inputFile
	 * @throws IOException
	 */
	public IfThenElse(InputStream inputFile) throws IOException {
		super();
		condition = TestFactory.readNextTest(inputFile);
		exprTrue = ExpressionFactory.readNextExpression(inputFile);
		exprFalse = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return condition.test(ability, tested) ? exprTrue.getValue(ability, tested,
				context) : exprFalse.getValue(ability, tested, context);
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		exprTrue.extractTriggeredEvents(res, source, globalTest);
		exprFalse.extractTriggeredEvents(res, source, globalTest);
	}

	/**
	 * The test used to determine which value will be returned
	 */
	private Test condition;

	/**
	 * The returned expression for true evaluation
	 */
	private Expression exprTrue;

	/**
	 * The returned expression for false evaluation
	 */
	private Expression exprFalse;

}
