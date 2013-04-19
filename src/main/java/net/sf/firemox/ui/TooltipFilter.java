/*
 * Created on Nov 16, 2004 
 * Original filename was TooltipFilter.java
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
package net.sf.firemox.ui;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.tools.Log;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class TooltipFilter {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>display powerANDtoughness yes=1,no=0 [1]</li>
	 * <li>display states yes=1,no=0 [1]</li>
	 * <li>display types yes=1,no=0 [1]</li>
	 * <li>display colors yes=1,no=0 [1]</li>
	 * <li>display properties yes=1,no=0 [1]</li>
	 * <li>display damage yes=1,no=0 [1]</li>
	 * <li>filter [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing the states picture
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public TooltipFilter(InputStream inputFile) throws IOException {
		powerANDtoughness = inputFile.read() == 1;
		states = inputFile.read() == 1;
		types = inputFile.read() == 1;
		colors = inputFile.read() == 1;
		properties = inputFile.read() == 1;
		damage = inputFile.read() == 1;
		test = TestFactory.readNextTest(inputFile);
	}

	/**
	 * The instance of TooltipFilter containing all fields set to
	 * <code>true</code>
	 */
	public static TooltipFilter fullInstance = new TooltipFilter();

	/**
	 * Creates a new instance of TooltipFilter with all displayed information <br>
	 */
	private TooltipFilter() {
		powerANDtoughness = true;
		states = true;
		types = true;
		colors = true;
		properties = true;
		damage = true;
	}

	/**
	 * Return true if this tooltip can be displayed for this specified card.
	 * 
	 * @param card
	 *          the tested card.
	 * @return true if this tooltip can be displayed for this specified card.
	 */
	public boolean suits(MCard card) {
		try {
			return test.test(null, card);
		} catch (NullPointerException e) {
			Log
					.error("Test of tooltip filter should 'onTestedCard'. Fix this error and rebuild this TBS\n\t error : "
							+ e.getStackTrace()[0]);
			return false;
		}
	}

	/**
	 * Are the power / toughness displayed
	 */
	public boolean powerANDtoughness;

	/**
	 * Are the states displayed
	 */
	public boolean states;

	/**
	 * Are the types displayed
	 */
	public boolean types;

	/**
	 * Are the properties displayed
	 */
	public boolean properties;

	/**
	 * Are the colors displayed
	 */
	public boolean colors;

	/**
	 * Are the damage displayed
	 */
	public boolean damage;

	/**
	 * The filtering test of this tooltip.
	 */
	public Test test;

}
