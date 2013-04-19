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
package net.sf.firemox.action;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;

/**
 * This class is representing an atom action. Each action can generate more than
 * an event.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.3
 * @since 0.85 named action, and complex constraints are supported.
 */
public abstract class MAction {

	/**
	 * The debug data, may be empty.
	 */
	protected final String debugData;

	/**
	 * Creates a new instance of MAction
	 * 
	 * @param debugData
	 *          optional debug info attached to this action.
	 */
	protected MAction(String debugData) {
		super();
		this.debugData = debugData;
	}

	/**
	 * Creates a new instance of MAction
	 */
	protected MAction() {
		this(null);
	}

	/**
	 * Return the index of this action. As default, this is a zero id
	 * 
	 * @return the index of this action.
	 * @see net.sf.firemox.action.Actiontype
	 */
	public abstract Actiontype getIdAction();

	/**
	 * Return the HTML code representing this action. If no picture is associated
	 * to this action, only text will be returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 * @return the HTML code representing this action. If no picture is associated
	 *         to this action, only text will be returned.
	 * @see #toString(Ability)
	 * @see #toHtmlString(Ability, int, ContextEventListener)
	 * @since 0.85
	 */
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return toString(ability);
	}

	/**
	 * Return the HTML code representing this action. If no picture is associated
	 * to this action, only text will be returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param htmlString
	 *          the HTML code defined for this string.
	 * @return the HTML code representing this action. If no picture is associated
	 *         to this action, only text will be returned.
	 * @see #toString(Ability)
	 * @since 0.85
	 */
	protected String toHtmlString(Ability ability, String htmlString) {
		return htmlString;
	}

	/**
	 * Return the HTML code representing this action. If this action is named,
	 * it's name will be returned. If not, if existing, the picture associated to
	 * this action is returned. Otherwise, built-in action's text will be
	 * returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param times
	 *          the times to repeat this action.
	 * @param context
	 *          is the context attached to this action.
	 * @return the HTML code representing this action. If this action is named,
	 *         it's name will be returned. If not, if existing, the picture
	 *         associated to this action is returned. Otherwise, built-in action's
	 *         text will be returned.
	 * @see #toString(Ability)
	 * @see #toHtmlString(Ability, ContextEventListener)
	 * @since 0.85
	 */
	public String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (times > 1) {
			final String res = toHtmlString(ability, context);
			if (res.length() == 0) {
				return null;
			}
			return "[" + toHtmlString(ability, context) + "] x " + times;
		}
		return toHtmlString(ability, context);
	}

	/**
	 * String representation of this action.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return action name.
	 * @see Object#toString()
	 */
	public abstract String toString(Ability ability);

	@Override
	public String toString() {
		if (debugData != null && debugData.length() > 0)
			return debugData;
		return super.toString();
	}

	/**
	 * Return the given test where values depending on values of this action have
	 * been replaced.
	 * 
	 * @param test
	 *          the test containing eventually some values depending on parameters
	 *          of this action.
	 * @return a parsed test.
	 * @since 0.85
	 */
	public Test parseTest(Test test) {
		return test;
	}

	/**
	 * Return true if this action matches with the given action.
	 * 
	 * @param constraintAction
	 *          the constraint action.
	 * @return true if this action matches with the given action.
	 * @since 0.85
	 */
	public boolean equal(MAction constraintAction) {
		return false;
	}

	/**
	 * Return the name of this action
	 * 
	 * @return the name of this action
	 */
	public String getActionName() {
		return toString();
	}

}