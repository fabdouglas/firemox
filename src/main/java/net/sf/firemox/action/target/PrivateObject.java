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
package net.sf.firemox.action.target;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * Add to the target list a named component. The named component must be saved
 * in the <code>privateNamedObjects</code> of the component owning this
 * action.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
final class PrivateObject extends AbstractTarget implements InitAction {

	/**
	 * Create an instance by reading a file Offset's file must pointing on the
	 * first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * </ul>
	 * For the available options, see interface IdTargets
	 * 
	 * @param inputStream
	 *          stream containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	PrivateObject(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	@Override
	public Target getAbstractTarget(ContextEventListener context, Ability ability) {
		return ability.getTargetable().getPrivateNamedObject(getActionName());
	}

	@Override
	public String toString(Ability ability) {
		return "{private object:" + getActionName() + "}";
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return play(context, ability);
	}

}
