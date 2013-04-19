/*
 * And.java 
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
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class And extends BinaryTest {

	/**
	 * Create an instance of And by reading a file. Offset's file must pointing on
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
	And(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	/**
	 * Return a new test equals to "leftTest && rightTest"
	 * 
	 * @param tests
	 *          the list of tests to append
	 * @return a new test equals to "leftTest && rightTest"
	 */
	public static Test append(Test... tests) {
		Test workingTest = null;
		for (Test test : tests) {
			if (test != null && test != True.getInstance()) {
				if (workingTest != null)
					workingTest = new And(workingTest, test);
				else
					workingTest = test;
			}
		}
		if (workingTest == null)
			return True.getInstance();
		return workingTest;
	}

	/**
	 * Creates a new instance of And specifying all attributes of this class. All
	 * parameters are copied, not cloned. So this new object shares the card and
	 * the specified codes
	 * 
	 * @param leftTest
	 *          the left boolean expression
	 * @param rightTest
	 *          the right boolean expression
	 */
	private And(Test leftTest, Test rightTest) {
		super(leftTest, rightTest);
	}

	@Override
	public Test getConstraintTest(HashMap<String, Expression> values) {
		return new And(leftTest.getConstraintTest(values), rightTest
				.getConstraintTest(values));
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return leftTest.test(ability, tested) && rightTest.test(ability, tested);
	}

	@Override
	public Player getOptimizedController(Ability ability,
			ContextEventListener context) {
		final Player controller = leftTest.getOptimizedController(ability, context);
		if (controller == null) {
			return rightTest.getOptimizedController(ability, context);
		}
		if (rightTest.getOptimizedController(ability, context) == null) {
			return controller;
		}
		return null;
	}

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		return leftTest.testPreemption(ability, tested)
				&& rightTest.testPreemption(ability, tested);
	}

	@Override
	public String toString() {
		return "(" + leftTest.toString() + " AND " + rightTest.toString() + ")";
	}

}