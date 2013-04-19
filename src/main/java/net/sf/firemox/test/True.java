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

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
@XmlTestElement(id = IdTest.TRUE)
public final class True extends Test {

	/**
	 * create a new instance of TestNull
	 */
	private True() {
		super();
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return true;
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return return the unique instance of this class.
	 */
	public static Test getInstance() {
		if (instance == null) {
			instance = new True();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "TRUE";
	}

	/**
	 * represents the unique instance of this class
	 */
	private static True instance;

}