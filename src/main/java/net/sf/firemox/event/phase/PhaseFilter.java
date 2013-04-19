/*
 * Created on 14 mars 2005
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
package net.sf.firemox.event.phase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.stack.EventManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public enum PhaseFilter {

	/**
	 * This code is used to do a test on phase's index
	 */
	INDEX_PHASE_FILTER,

	/**
	 * This code is used to do a test on phase's identifier
	 */
	ID_PHASE_FILTER;

	/**
	 * Return the identifier or the index of current phase.
	 * 
	 * @return the identifier or the index of current phase.
	 */
	public int getPhaseFilter() {
		switch (this) {
		case INDEX_PHASE_FILTER:
			return EventManager.phaseIndex;
		case ID_PHASE_FILTER:
		default:
			return EventManager.currentIdPhase;
		}
	}

	/**
	 * Set the identifier or the index of next phase.
	 * 
	 * @param idPhase
	 *          the identifier or the index of next phase.
	 */
	public void setNextPhase(int idPhase) {
		switch (this) {
		case INDEX_PHASE_FILTER:
			EventManager.nextPhaseIndex = idPhase;
			break;
		case ID_PHASE_FILTER:
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				if (EventManager.turnStructure[i].id == idPhase) {
					EventManager.nextPhaseIndex = i;
					return;
				}
			}
			throw new InternalError("Unknow idPhase (id) : " + idPhase);
		default:
		}

	}

	/**
	 * Wrtite this enum to the given output stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 * @throws IOException
	 *           error while writing event's id
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enum from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enum to read.
	 * @return the enum from the given input stream.
	 * @throws IOException
	 *           error while reading event's id.
	 */
	public static PhaseFilter valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}
}
