/*
 * Or.java 
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
 */
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.expression.Expression;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class Or extends BinaryTest {

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
	Or(InputStream inputFile) throws IOException {
		super(inputFile);
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
	public Or(Test leftTest, Test rightTest) {
		super(leftTest, rightTest);
	}

	/**
	 * Return this test where values depending on values of this action have been
	 * replaced.
	 * 
	 * @param values
	 *          are referecable values.
	 * @return a parsed test.
	 * @since 0.85
	 */
	@Override
	public Test getConstraintTest(HashMap<String, Expression> values) {
		return new Or(leftTest.getConstraintTest(values), rightTest
				.getConstraintTest(values));
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return leftTest.test(ability, tested) || rightTest.test(ability, tested);
	}

	/**
	 * Return a new test equals to "leftTest || rightTest"
	 * 
	 * @param leftTest
	 *          the left test
	 * @param rightTest
	 *          the right test
	 * @return a new test equals to "leftTest || rightTest"
	 */
	public static Test append(Test leftTest, Test rightTest) {
		if (leftTest == True.getInstance()) {
			return rightTest;
		}
		if (rightTest == True.getInstance()) {
			return leftTest;
		}
		return new Or(leftTest, rightTest);
	}

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		return leftTest.testPreemption(ability, tested)
				|| rightTest.testPreemption(ability, tested);
	}

	@Override
	public String toString() {
		return "(" + leftTest.toString() + " OR " + rightTest.toString() + ")";
	}
}