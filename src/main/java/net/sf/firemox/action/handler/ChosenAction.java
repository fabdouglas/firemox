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
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public interface ChosenAction extends Replayable {

	/**
	 * No generated event. Let the active player playing this action.
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
	boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);

	/**
	 * No generated event. Unset this action as current one.
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
	void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);

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
	boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability);

	/**
	 * Return the HTML code representing this action. If this action is named,
	 * it's name will be returned. If not, if existing, the picture associated to
	 * this action is returned. Otherwise, built-in action's text will be
	 * returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return the HTML code representing this action. If this action is named,
	 *         it's name will be returned. If not, if existing, the picture
	 *         associated to this action is returned. Otherwise, built-in action's
	 *         text will be returned.
	 * @param context
	 *          is the context attached to this action.
	 * @param actionContext
	 *          the context of this action.
	 * @since 0.86
	 */
	String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext);
}
