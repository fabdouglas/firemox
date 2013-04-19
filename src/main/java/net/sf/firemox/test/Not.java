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
 * Not.java Created on 25 feb. 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class Not extends TestBoolean {

	/**
	 * Create an instance of And by reading a file. Offset's file must pointing on
	 * the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Not(InputStream inputFile) throws IOException {
		super(inputFile);
		this.test = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Create a new instance of Not given the other test.
	 * 
	 * @param test
	 *          the other test.
	 */
	public Not(Test test) {
		this.test = test;
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return !test.test(ability, tested);
	}

	/**
	 * Left test of this test
	 */
	private Test test;

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		test.extractTriggeredEvents(res, source, globalTest);
	}

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		return super.testPreemption(ability, tested);
	}

	@Override
	public String toString() {
		return "NOT " + test.toString();
	}
}