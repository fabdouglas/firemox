/*
 * Created on Nov 8, 2004 
 * Original filename was IntValue.java
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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * Return the register value of a component without considering the modifiers.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public class BaseRegisterIntValue extends RegisterAccess {

	/**
	 * Creates a new instance of BaseRegisterIntValue <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [RegisterAccess]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file representing this object
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public BaseRegisterIntValue(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public boolean isXvalue() {
		return false;
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return register.getTargetable().getTargetable(ability, tested)
				.getValueIndirection(index.getValue(ability, tested, context));
	}

	@Override
	public int getPreemptionValue(Ability ability, Target tested) {
		return register.getTargetable().getPreemptedValue(ability,
				index.getValue(ability, tested, null));
	}

}
