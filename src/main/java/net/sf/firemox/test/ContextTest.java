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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.MContextCardCardIntIntTest;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public final class ContextTest extends Test {

	/**
	 * create a new instance of TestNull
	 */
	private ContextTest() {
		super();
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		throw new InternalError(" cannot test directly component");
	}

	/**
	 * Return the test of this context.
	 * 
	 * @return the test of this context.
	 */
	public Test getTest() {
		return ((MContextCardCardIntIntTest) StackManager.getInstance()
				.getAbilityContext()).test;
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static ContextTest getInstance() {
		if (instance == null) {
			instance = new ContextTest();
		}
		return instance;
	}

	/**
	 * Represents the unique instance of this class.
	 */
	private static ContextTest instance;

}