/*
 * Created on 4 févr. 2005
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
package net.sf.firemox.action.intlist;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.token.IdTargetList;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
abstract class IntListRemove extends IntList {

	/**
	 * Create an instance of IntListRemove by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>idType [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	protected IntListRemove(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	/**
	 * Read the next TargetListRemove object
	 * 
	 * @param inputFile
	 *          the streamcontaining the definition of next TargetList object.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	static IntListRemove readNextIntListRemove(InputStream inputFile)
			throws IOException {
		IdTargetList idType = IdTargetList.deserialize(inputFile);
		switch (idType) {
		case REMOVE_INDEX:
			return new IntListRemoveIndex(inputFile);
		case REMOVE_LIST:
			return new IntListRemoveList(inputFile);
		case REMOVE_TARGET:
			return new IntListRemoveInt(inputFile);
		default:
			throw new InternalError("Unknown type of target-list.remove : " + idType);
		}
	}

	@Override
	public abstract boolean play(ContextEventListener context, Ability ability);

	/**
	 * Action name.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return action name.
	 * @see Object#toString()
	 */
	@Override
	public String toString(Ability ability) {
		return "int-list.remove";
	}
}
