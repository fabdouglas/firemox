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
package net.sf.firemox.action.targetlist;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.operation.IdOperation;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class TargetListFactory {

	/**
	 * Create a new instance of this class.
	 */
	private TargetListFactory() {
		// nothing to do
	}

	/**
	 * Read the next TargetList object.
	 * 
	 * @param inputFile
	 *          the streamcontaining the definition of next TargetList object.
	 * @return the next TargetList object read from the specified stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static TargetList readNextTargetList(InputStream inputFile)
			throws IOException {
		final IdOperation operation = IdOperation.deserialize(inputFile);
		switch (operation) {
		case REMOVE_FIRST:
			return new TargetListRemoveFirst(inputFile);
		case REMOVE_LAST:
			return new TargetListRemoveLast(inputFile);
		case REMOVE_QUEUE:
			return new TargetListRemoveQueue(inputFile);
		case REMOVE_TAIL:
			return new TargetListRemoveTail(inputFile);
		case ADD_ALL:
			return new TargetListAddAll(inputFile);
		case REMOVE:
			return TargetListRemove.readNextTargetListRemove(inputFile);
		case CLEAR:
			return new TargetListClear(inputFile);
		case COLLAPSE_COMBAT:
			return new TargetListCollapseCombat(inputFile);
		case SAVE:
			return TargetListSave.readNextTargetListSave(inputFile);
		case COUNTER:
			return TargetListOccurence.readNextTargetListOccurence(inputFile);
		case FILTER:
			return new TargetListFilter(inputFile);
		case LOAD:
			return TargetListLoad.readNextTargetListLoad(inputFile);
		case REMOVE_ALL:
			return new TargetListRemoveAll(inputFile);
		default:
			throw new InternalError("Unknown operation for target-list : "
					+ operation);
		}
	}
}
