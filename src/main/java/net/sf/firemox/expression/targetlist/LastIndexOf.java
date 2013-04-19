/*
 * Created on 5 févr. 2005
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
package net.sf.firemox.expression.targetlist;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class LastIndexOf extends TargetList {

	/**
	 * Creates a new instance of LastIndexOf <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>list index : Expression [...]</li>
	 * <li>target : IdToken [2]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public LastIndexOf(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		final Target target = getTarget(ability, tested);
		final List<Target> list = getList(ability, tested, context);
		for (int i = list.size(); i-- > 0;) {
			if (list.get(i) == target) {
				return i;
			}
		}
		return -1;
	}
}
