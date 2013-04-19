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
import java.util.Map;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Wrapper;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class ObjectMapClear extends ObjectMap implements FollowAction {

	/**
	 * Create an instance of TargetListClear by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>idType [1]</li>
	 * <li>list index : Expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	ObjectMapClear(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		ability.getTargetable().clearPrivateNamedObject();
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "";
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final Wrapper<Map<String, Target>> mapContext = new Wrapper<Map<String, Target>>();
		actionContext.actionContext = mapContext;
		mapContext.setObject(ability.getTargetable().getPrivateNamedObjects());
		play(context, ability);
	}

	@SuppressWarnings("unchecked")
	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final Map<String, Target> mapContext = ((Wrapper<Map<String, Target>>) actionContext.actionContext)
				.getObject();
		final Target target = ability.getTargetable();
		target.clearPrivateNamedObject();
		if (mapContext != null) {
			for (Map.Entry<String, Target> key : mapContext.entrySet()) {
				target.addPrivateNamedObject(key.getKey(), key.getValue());
			}
		}
	}
}
