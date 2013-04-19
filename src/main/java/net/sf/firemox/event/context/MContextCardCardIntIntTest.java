/*
 * Created on 2005/08/25
 * Original filename was MContextCardCardIntIntTest
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
package net.sf.firemox.event.context;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class MContextCardCardIntIntTest extends MContextCardCardIntInt {

	/**
	 * Creates a new instance of MContextCardCardIntIntTest <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param source
	 *          the Mcard object to save to this context.
	 * @param value
	 *          the integer to save to this context.
	 * @param value2
	 *          the integer to save to this context.
	 * @param test
	 *          the attached test of this context.
	 */
	public MContextCardCardIntIntTest(Target dest, MCard source, int value,
			int value2, Test test) {
		this(dest, source, value, value2, test, -1, -1);
	}

	/**
	 * Creates a new instance of MContextCardCardIntIntTest <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param source
	 *          the Mcard object to save to this context.
	 * @param value
	 *          the integer to save to this context.
	 * @param value2
	 *          the integer to save to this context.
	 * @param test
	 *          the test to save to this context.
	 * @param maxTimeStamp1
	 *          is the maximum timestamp allowed for destination card during the
	 *          resolution.
	 * @param maxTimeStamp2
	 *          is the maximum timestamp allowed for source card during the
	 *          resolution.
	 */
	public MContextCardCardIntIntTest(Target dest, MCard source, int value,
			int value2, Test test, int maxTimeStamp1, int maxTimeStamp2) {
		super(dest, source, value, value2, maxTimeStamp1, maxTimeStamp2);
		this.test = test;
	}

	/**
	 * Returns The test that was saved.
	 * 
	 * @return The test that was saved.
	 */
	public final Test getTest() {
		return test;
	}

	/**
	 * The integer that was saved.
	 */
	public Test test;

}