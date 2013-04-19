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
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;

/**
 * Replace the tested component by another inside the nested test scope.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class ReplaceTested extends Test {

	/**
	 * The TestOn component replacing the given tested component inside the nested
	 * test.
	 */
	private final TestOn replace;

	/**
	 * The nested test.
	 */
	private final Test nestedTest;

	/**
	 * Create an instance of ReplaceTested reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>replace [TestOn]</li>
	 * <li>nested tested [Test]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ReplaceTested(InputStream inputFile) throws IOException {
		super(inputFile);
		replace = TestOn.deserialize(inputFile);
		nestedTest = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param replace
	 *          he new component.
	 * @param nestedTest
	 *          the nested test.
	 */
	public ReplaceTested(TestOn replace, Test nestedTest) {
		super();
		this.replace = replace;
		this.nestedTest = nestedTest;
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return nestedTest.test(ability, replace.getTargetable(ability, tested));
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		nestedTest.extractTriggeredEvents(res, source, globalTest);
	}
}
