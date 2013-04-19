/*
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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.tools.MToolKit;

/**
 * Test if the tested target contains the given private named target.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
class HasPrivateObject extends TestObject {

	/**
	 * Create an instance of HasPrivateNamedTargetable by reading a file. Offset's
	 * file must pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>used target for test [1]
	 * <li>named target + '\0' : String [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	HasPrivateObject(InputStream inputFile) throws IOException {
		super(inputFile);
		privateNamedTargetable = MToolKit.readString(inputFile);
	}

	@Override
	public final boolean test(Ability ability, Target tested) {
		return on.getTargetable(ability, tested).getPrivateNamedObject(
				privateNamedTargetable) != null;
	}

	/**
	 * The name of looked for target.
	 */
	protected String privateNamedTargetable;

}