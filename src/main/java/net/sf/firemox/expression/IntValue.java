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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91 the pre-empted value of ALL, is ONE.
 */
public class IntValue extends Expression {

	/**
	 * Creates a new instance of IntValue <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>int 16 value [2]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public IntValue(InputStream inputFile) throws IOException {
		super();
		value = MToolKit.getConstant(MToolKit.readInt16(inputFile));
	}

	/**
	 * Creates a new instance of IntValue with a specified value.
	 * 
	 * @param value
	 *          integer value of this expression
	 */
	public IntValue(int value) {
		super();
		this.value = value;
	}

	@Override
	public int getPreemptionValue(Ability ability, Target tested,
			ContextEventListener context) {
		if (value == IdConst.ALL) {
			return 1;
		}
		return super.getPreemptionValue(ability, tested, context);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	/**
	 * The integer value of this expression
	 */
	public int value;

}
