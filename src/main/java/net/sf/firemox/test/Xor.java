/*
 * Created on Aug 3, 2004 
 * Original filename was Xor.java
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

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.expression.Expression;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
@XmlTestElement(id = IdTest.XOR)
public class Xor extends BinaryTest {

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
	Xor(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	/**
	 * Creates a new instance of Xor specifying all attributes of this class. All
	 * parameters are copied, not cloned. So this new object shares the card and
	 * the specified codes
	 * 
	 * @param leftTest
	 *          the left boolean expression
	 * @param rightTest
	 *          the right boolean expression
	 */
	private Xor(Test leftTest, Test rightTest) {
		super(leftTest, rightTest);
	}

	@Override
	public Test getConstraintTest(HashMap<String, Expression> values) {
		return new Xor(leftTest.getConstraintTest(values), rightTest
				.getConstraintTest(values));
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		boolean left = leftTest.test(ability, tested);
		boolean right = rightTest.test(ability, tested);
		return left && !right || !left && right;
	}

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		boolean left = leftTest.testPreemption(ability, tested);
		boolean right = rightTest.testPreemption(ability, tested);
		return left && !right || !left && right;
	}

	@Override
	public String toString() {
		return "(" + leftTest.toString() + " XOR " + rightTest.toString() + ")";
	}
}