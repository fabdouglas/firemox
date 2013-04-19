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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.expression.Expression;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public abstract class BinaryTest extends TestBoolean {

	/**
	 * Create an instance of Or by reading a file. Offset's file must pointing on
	 * the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>left expression [...]</li>
	 * <li>right expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected BinaryTest(InputStream inputFile) throws IOException {
		super(inputFile);
		leftTest = TestFactory.readNextTest(inputFile);
		rightTest = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of Or specifying all attributes of this class. All
	 * parameters are copied, not cloned. So this new object shares the card and
	 * the specified codes
	 * 
	 * @param leftTest
	 *          the left boolean expression
	 * @param rightTest
	 *          the right boolean expression
	 */
	protected BinaryTest(Test leftTest, Test rightTest) {
		super();
		this.leftTest = leftTest;
		this.rightTest = rightTest;
	}

	@Override
	public abstract boolean test(Ability ability, Target tested);

	/**
	 * Left test of this test
	 */
	protected Test leftTest;

	/**
	 * Right test of this test
	 */
	protected Test rightTest;

	/**
	 * Return this test where values depending on values of this action have been
	 * replaced.
	 * 
	 * @param values
	 *          are referecable values.
	 * @return a parsed test.
	 * @since 0.85
	 */
	public abstract Test getConstraintTest(HashMap<String, Expression> values);

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		leftTest.extractTriggeredEvents(res, source, globalTest);
		rightTest.extractTriggeredEvents(res, source, globalTest);
	}

	@Override
	public abstract boolean testPreemption(Ability ability, Target tested);
}