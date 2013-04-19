/*
 * Target.java
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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.RollBackAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * Add to the target list card(s) or player(s) following the specified mode and
 * the specified type. If mode is 'choose' or 'opponentchoose', then a
 * 'targeted' event is raised when the choice is made. In case of 'all',
 * 'random', 'myself' or any target known with acces register, no event is
 * generated. The friendly test, and the type are required only for the modes
 * 'random', 'all', 'choose' and 'opponentchoose' to list the valids targets.
 * The target list is used by the most actions. <br>
 * For example, if the next action 'damage', all cards/player from the target
 * list would be damaged. When a new ability is added to the stack, a new list
 * is created and attached to it. Actions may modify, acceess it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class Target extends UserAction implements StandardAction,
		RollBackAction {

	/**
	 * Create an instance of MAction by reading a file Offset's file must pointing
	 * on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [Action]</li>
	 * </ul>
	 * For the available options, see interface IdTargets
	 * 
	 * @param actionName
	 *          the action's name.
	 */
	protected Target(String actionName) {
		super(actionName);
	}

	/**
	 * Create an instance of this class from input stream.
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>[super]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          stream containing this action.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected Target(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.TARGET;
	}

	public abstract boolean play(ContextEventListener context, Ability ability);

	/**
	 * Generate event associated to this action. Only one or several events are
	 * generated and may be collected by event listeners. Then play this action
	 * 
	 * @param actionContext
	 *          the context containing data saved by this action during the
	 *          'choose" process.
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 * @return true if the stack can be resolved just after this call.
	 */
	public abstract boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		StackManager.getInstance().getTargetedList().removeLast();
	}

	@Override
	public abstract String toString(Ability ability);

}