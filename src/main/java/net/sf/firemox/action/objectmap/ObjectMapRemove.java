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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Wrapper;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
class ObjectMapRemove extends ObjectMap implements FollowAction {

	/**
	 * Create an instance of ObjectMapRemove by reading a file Offset's file
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>object's name + '\0' [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	ObjectMapRemove(InputStream inputFile) throws IOException {
		super(inputFile);
		objectName = MToolKit.readString(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		ability.getTargetable().removePrivateNamedObject(objectName);
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "";
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final Wrapper<Target> mapContext = new Wrapper<Target>();
		actionContext.actionContext = mapContext;
		mapContext.setObject(ability.getTargetable().getPrivateNamedObject(
				objectName));
		play(context, ability);
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final Target oldTargetable = (Target) ((Wrapper<?>) actionContext.actionContext)
				.getObject();
		if (oldTargetable != null) {
			ability.getTargetable().addPrivateNamedObject(objectName, oldTargetable);
		}
	}

	/**
	 * The object's name to manipulate.
	 */
	private String objectName;
}