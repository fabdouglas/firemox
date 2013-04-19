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
package net.sf.firemox.action.target;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;

/**
 * Add to the target list card(s) or player(s) following the specified mode and
 * the specified type. If mode is 'choose' or 'opponentchoose', then a
 * 'targeted' event is raised when the choice is made. In case of 'all',
 * 'random', 'myself' or any target known with acces register, no event is
 * generated. The friendly test, and the type are necessary only for the modes
 * 'random', 'all', 'choose' and 'opponentchoose' to list the valids targets.
 * The target list is used by the most actions. <br>
 * For example, if the next action 'damage', all cards/player from the target
 * list would be damaged. When a new ability is added to the stack, a new list
 * is created and attached to it. Actions may modify, acceess it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public final class SilentTarget extends ChosenTarget {

	/**
	 * Create an instance by reading a file Offset's file must pointing on the
	 * first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * </ul>
	 * For the available options, see interface IdTargets
	 * 
	 * @param type
	 *          the concerned target's type previously read by the factory
	 * @param options
	 *          the options previously read by the factory
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	SilentTarget(int type, int options, InputStream inputFile) throws IOException {
		super(type, options, inputFile);
	}

	@Override
	public String toString(Ability ability) {
		return "Choose" + super.toString(ability);
	}

	@Override
	public boolean isTargeting() {
		return false;
	}
}