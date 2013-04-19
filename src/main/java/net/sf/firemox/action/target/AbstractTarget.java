/*
 * Created on 15 nov. 2003
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
package net.sf.firemox.action.target;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * Add to the target list card(s) or player(s) following the specified mode and
 * the specified type. If mode is 'choose' or 'opponent-choose', then a
 * 'targeted' event is raised when the choice is made. In case of 'all',
 * 'random', 'me' or any predicted target, no event is generated. The test, and
 * the type are necessary only for the modes 'random', 'all', 'choose' and
 * 'opponent-choose' to list the valid targets. The target list is used by the
 * most actions. <br>
 * For example, if the next action 'damage', all cards/player from the target
 * list would be damaged. When a new ability is added to the stack, a new list
 * is created and attached to it. Actions may modify, access it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public abstract class AbstractTarget extends net.sf.firemox.action.Target
		implements StandardAction, ChosenAction {

	/**
	 * Create an instance of MAction by reading a file Offset's file must pointing
	 * on the first byte of this action <br>
	 * 
	 * @param actionName
	 *          the action name.
	 */
	AbstractTarget(String actionName) {
		super(actionName);
	}

	/**
	 * Create an instance of this class from input stream.
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>super [Action]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          stream containing this action.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	AbstractTarget(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	public final void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// Nothing to do
	}

	public final boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return play(context, ability);
	}

	/**
	 * No generated event. Simulate an action.
	 * 
	 * @param actionContext
	 *          the context containing data saved by this action during the
	 *          'choose" process.
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 */
	public final void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		play(context, ability);
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return play(context, ability);
	}

	public final String toHtmlString(Ability ability,
			ContextEventListener context, ActionContextWrapper actionContext) {
		if (actionContext.repeat > 1) {
			return toHtmlString(ability, actionContext.repeat, context) + "("
					+ actionContext.done + "/" + actionContext.repeat + ")";
		}
		return toHtmlString(ability, context);
	}

	@Override
	public final boolean play(ContextEventListener context, Ability ability) {
		return StackManager.getInstance().getTargetedList().list
				.add(getAbstractTarget(context, ability));
	}

	/**
	 * Return the target added by this action.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 * @return the target added by this action.
	 */
	public abstract Target getAbstractTarget(ContextEventListener context,
			Ability ability);

	@Override
	public abstract String toString(Ability ability);

}