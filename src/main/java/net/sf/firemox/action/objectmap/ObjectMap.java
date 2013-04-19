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
package net.sf.firemox.action.objectmap;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.action.UserAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public abstract class ObjectMap extends UserAction implements StandardAction {

	/**
	 * Create an instance of TargetList by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [Action]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	protected ObjectMap(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.OBJECT_MAP;
	}

	public abstract boolean play(ContextEventListener context, Ability ability);

	@Override
	public abstract String toString(Ability ability);

	@Override
	public final String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (actionName != null && actionName.charAt(0) != '%'
				&& actionName.charAt(0) != '@' && actionName.indexOf("%n") != -1) {
			return super.toHtmlString(ability, times, context);
		}
		return "";
	}

}
