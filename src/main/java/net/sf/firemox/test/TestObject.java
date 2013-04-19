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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;

/**
 * TestObject.java Created on 25 feb. 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public abstract class TestObject extends Test {

	/**
	 * Create an instance of TestObject
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>used target for test [1]
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this test
	 * @throws IOException
	 */
	protected TestObject(InputStream inputFile) throws IOException {
		super(inputFile);
		on = TestOn.deserialize(inputFile);
	}

	/**
	 * Creates a new instance of TestObject <br>
	 * 
	 * @param on
	 *          The test manager giving the objet (card, player, ability,..) on
	 *          which the test would be applyed on
	 */
	protected TestObject(TestOn on) {
		this.on = on;
	}

	@Override
	public abstract boolean test(Ability ability, Target tested);

	/**
	 * The test manager giving the objet (card, player, ability,..) on which the
	 * test would be applyed on
	 */
	protected final TestOn on;

}