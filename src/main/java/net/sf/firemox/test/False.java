/*
 * Created on Oct 20, 2004 
 * Original filename was False.java
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
package net.sf.firemox.test;

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 */
@XmlTestElement(id = IdTest.FALSE)
public final class False extends Test {

	/**
	 * create a new instance of False
	 */
	private False() {
		super();
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return false;
	}

	/**
	 * Private method used to synchronizely create the <code>False</code> class
	 * instance.
	 */
	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new False();
		}
	}

	/**
	 * Returns the unique instance of this class.
	 * 
	 * @return the unique instance of this class
	 */
	public static Test getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	@Override
	public String toString() {
		return "FALSE";
	}

	/**
	 * represents the unique instance of this class
	 */
	private static False instance;

}