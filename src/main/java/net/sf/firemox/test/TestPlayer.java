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
import net.sf.firemox.clickable.target.player.Player;

/**
 * TestObject.java Created on 25 feb. 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
abstract class TestPlayer extends TestObject {

	/**
	 * Create an instance of TestPlayer
	 * 
	 * @param inputFile
	 * @throws IOException
	 */
	protected TestPlayer(InputStream inputFile) throws IOException {
		super(inputFile);
		player = TestOn.deserialize(inputFile);
	}

	@Override
	public final boolean test(Ability ability, Target tested) {
		return tested instanceof Player && testPlayer(ability, (Player) tested);
	}

	/**
	 * Return the result of test beetwen left and right op applied on the
	 * specified card.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return true if the specified card matches with the test to do
	 */
	protected abstract boolean testPlayer(Ability ability, Player tested);

	/**
	 * The player to test
	 */
	protected TestOn player;
}