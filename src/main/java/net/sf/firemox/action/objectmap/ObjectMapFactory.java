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
package net.sf.firemox.action.objectmap;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.operation.IdOperation;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class ObjectMapFactory {

	/**
	 * Create a new instance of this class.
	 */
	private ObjectMapFactory() {
		super();
	}

	/**
	 * Read the next ObjectMap object.
	 * 
	 * @param inputStream
	 *          the streamcontaining the definition of next TargetList object.
	 * @return the next ObjectMap object read from the specified stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static ObjectMap readNextObjectMap(InputStream inputStream)
			throws IOException {
		final IdOperation idOperation = IdOperation.deserialize(inputStream);
		switch (idOperation) {
		case REMOVE:
		case MINUS:
			return new ObjectMapRemove(inputStream);
		case CLEAR:
			return new ObjectMapClear(inputStream);
		case SAVE:
		case ADD:
			return new ObjectMapSave(inputStream);
		default:
			throw new InternalError("Unknown operation for object-map : "
					+ idOperation);
		}
	}
}
