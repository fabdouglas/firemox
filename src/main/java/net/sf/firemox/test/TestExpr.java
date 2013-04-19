/*
 * Created on 25 feb. 2004
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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;

/**
 * This is a class representing a comparaison of two integer values.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public abstract class TestExpr extends Test {

	/**
	 * Create an instance of TestExpr by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>left value [...]</li>
	 * <li>right value [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected TestExpr(InputStream inputFile) throws IOException {
		super(inputFile);
		leftExpression = ExpressionFactory.readNextExpression(inputFile);
		rightExpression = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Create a new instance of TestExpr given left and right expression.
	 * 
	 * @param leftExpression
	 *          the left expression of this test.
	 * @param rightExpression
	 *          the right expression of this test.
	 */
	protected TestExpr(Expression leftExpression, Expression rightExpression) {
		super();
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
	}

	/**
	 * Return the left value of this test.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return the left value of this test.
	 */
	protected int getLeftValue(Ability ability, Target tested) {
		return leftExpression.getValue(ability, tested, null);
	}

	/**
	 * Return the right value of this test.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return the right value of this test.
	 */
	protected int getRightValue(Ability ability, Target tested) {
		return rightExpression.getValue(ability, tested, null);
	}

	@Override
	public abstract boolean test(Ability ability, Target tested);

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		leftExpression.extractTriggeredEvents(res, source, globalTest);
		rightExpression.extractTriggeredEvents(res, source, globalTest);
	}

	/**
	 * left integer expression. The complex expression to use for the left value.
	 * Is null if the IdToken number is not a complex expression.
	 */
	protected final Expression leftExpression;

	/**
	 * right integer expression. The complex expression to use for the right
	 * value. Is null if the IdToken number is not a complex expression.
	 */
	protected final Expression rightExpression;

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		if (!leftExpression.canBePreempted() || !rightExpression.canBePreempted())
			return true;
		return super.testPreemption(ability, tested);
	}
}