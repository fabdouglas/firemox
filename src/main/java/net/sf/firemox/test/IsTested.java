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
 * 
 */
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 * @since 0.91 an additional component 'other' is used.
 */
public class IsTested extends TestCard {

	/**
	 * The other component to test
	 */
	private final TestOn other;

	/**
	 * Create an instance of IsTested by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>card TestOn [1]</li>
	 * <li>other TestOn [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected IsTested(InputStream inputFile) throws IOException {
		super(inputFile);
		other = TestOn.deserialize(inputFile);
	}

	/**
	 * Create an instance of TestedIsMe <br>
	 * 
	 * @param on
	 *          the card that would be compared to the tesed one.
	 */
	public IsTested(TestOn on) {
		this(on, TestOn.TESTED);
	}

	/**
	 * Create an instance of TestedIsMe <br>
	 * 
	 * @param on
	 *          the card that would be compared to the tested one.
	 * @param other
	 *          The other component to test.
	 */
	public IsTested(TestOn on, TestOn other) {
		super(on);
		this.other = other;
	}

	@Override
	public boolean testCard(Ability ability, MCard tested) {
		return other.getTargetable(ability, tested) == on.getTargetable(ability,
				tested);
	}

	/**
	 * Default instance representing the test against ME and TESTED.
	 */
	public static final IsTested TESTED_IS_ME = new IsTested(TestOn.THIS);

	/**
	 * Default instance representing the test against ATTACHED_TO and TESTED.
	 */
	public static final Test TESTED_IS_ATTACHED_TO = new IsTested(TestOn.TESTED,
			TestOn.ATTACHED_TO);

}