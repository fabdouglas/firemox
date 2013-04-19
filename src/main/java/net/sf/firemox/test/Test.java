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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public abstract class Test {

	/**
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>tested on (idTestedOn) [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this test
	 */
	protected Test(InputStream inputFile) {
		// Log.debug("TEST " + this.getClass() + "(" + getIdTest() + ")");
	}

	/**
	 * Creates a new instance of Test <br>
	 */
	protected Test() {
		super();
	}

	/**
	 * Return this test where values depending on values of this action have been
	 * replaced.
	 * 
	 * @param values
	 *          are referencable values.
	 * @return a parsed test.
	 * @since 0.85
	 */
	public Test getConstraintTest(Map<String, Expression> values) {
		return this;
	}

	/**
	 * Indicates if the specified card matches with the test to do
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return true if the specified card matches with the test to do
	 */
	public abstract boolean test(Ability ability, Target tested);

	/**
	 * Add to the specified list, the events modifying the result of this test.
	 * 
	 * @param res
	 *          is the list of events to fill
	 * @param source
	 *          is the card source of event
	 * @param globalTest
	 *          the optional global test to include in the event test.
	 */
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		// No event associated to this test
	}

	/**
	 * Return the controller making true this test. If several players or no
	 * player can make this test true, the <code>null</code> value is returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context of current ability
	 * @return the controller making true this test. If several player or no
	 *         player can make this test true, the <code>null</code> value is
	 *         returned.
	 */
	public Player getOptimizedController(Ability ability,
			ContextEventListener context) {
		return null;
	}

	/**
	 * Indicates if the specified card matches with the test to do.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return true if the specified card matches with the test to do
	 */
	public boolean testPreemption(Ability ability, Target tested) {
		return test(ability, tested);
	}

}