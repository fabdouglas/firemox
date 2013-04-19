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
package net.sf.firemox.action.targetlist;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.token.IdTargetList;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
abstract class TargetListOccurence extends TargetList {

	/**
	 * Create an instance of TargetListOccurence by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	protected TargetListOccurence(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	/**
	 * Read the next TargetListOccurence object
	 * 
	 * @param inputFile
	 *          the streamcontaining the definition of next TargetList object.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	static TargetList readNextTargetListOccurence(InputStream inputFile)
			throws IOException {
		IdTargetList idType = IdTargetList.deserialize(inputFile);
		switch (idType) {
		case OCCURENCE_LIST:
			return new TargetListOccurenceList(inputFile);
		case OCCURENCE_TARGET:
			return new TargetListOccurenceTarget(inputFile);
		default:
			throw new InternalError("Unknown type of target-list.occurrence : "
					+ idType);
		}
	}

	@Override
	public abstract boolean play(ContextEventListener context, Ability ability);

	@Override
	public String toString(Ability ability) {
		return "target-list.occurrence";
	}

}
