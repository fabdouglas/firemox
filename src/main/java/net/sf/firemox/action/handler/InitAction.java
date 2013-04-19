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
package net.sf.firemox.action.handler;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * An init action is an action played during the initialization phase of an
 * ability, and also replayed during the replay phase.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public interface InitAction extends Replayable {

	/**
	 * No generated event. Let the active player playing this action.<br>
	 * Generally, this method is used to initialize the context of this action
	 * such as required mana, or fixing some values.<br>
	 * Be aware this method can be called several times if there it is in a loop
	 * (hop usage), so manage this case to prevent a reset of context each time
	 * this method is called during the initialization phase.
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
	boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);

	/**
	 * Generate event associated to this action. One or several events are
	 * generated and may be collected by event listeners. Then play this action.<br>
	 * In this method, you should use the context you have stored in the
	 * initialization phase with the init method.<br>
	 * This method is only called once, so all stuffs (action and event) must be
	 * done in one shot.
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
	boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);
}
