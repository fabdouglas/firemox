/*
 * Created on 20 mars 2005
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

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
@XmlTestElement(id = IdTest.IS_ABILITY)
public class IsAbility extends TestObject {

	/**
	 * Create an instance of IsAbility by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this test
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	IsAbility(InputStream inputFile) throws IOException {
		super(inputFile);
		abilityType = inputFile.read();
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return on.getTargetable(ability, tested).isAbility(abilityType);
	}

	private int abilityType;
}