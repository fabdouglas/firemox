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
package net.sf.firemox.clickable.action;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.action.InputChoice;
import net.sf.firemox.action.MAction;
import net.sf.firemox.action.Repeat;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class ToStringHelper {

	/**
	 * Create a new instance of this class.
	 */
	private ToStringHelper() {
		super();
	}

	/**
	 * Return the HTML code representing an action list.
	 * 
	 * @param ability
	 *          the ability owning these actions.
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this ability.
	 */
	public static String toHtmlString(Ability ability,
			ContextEventListener context) {
		final String defaultName = ability.getName();
		StringBuilder buffer = new StringBuilder();
		if (defaultName != null && defaultName.length() > 0) {
			if (defaultName.indexOf("%a") == -1) {
				return LanguageManagerMDB.getString(defaultName);
			}
			// we display and ability name, and action list.toString()
			buffer.append(LanguageManagerMDB.getString(defaultName.replaceAll("%a",
					"")));
		}
		toHtmlString(buffer, ability, ability.actionList(), context);
		if (buffer.length() != 0) {
			buffer.append(" : ");
		}
		toHtmlString(buffer, ability, ability.effectList(), context);
		if (buffer.length() == 0) {
			return null;
		}
		return buffer.toString();
	}

	/**
	 * Return the HTML code representing an action list.
	 * 
	 * @param buffer
	 *          the string buffer used to build the HTML code of this ability.
	 * @param ability
	 *          the ability owning these actions.
	 * @param actions
	 *          the actions to display.
	 * @param context
	 *          the context needed by event activated
	 */
	public static void toHtmlString(StringBuilder buffer, Ability ability,
			MAction[] actions, ContextEventListener context) {
		boolean emptyText = true;
		for (int id = 0; id < actions.length; id++) {
			final MAction action = actions[id];
			String str = null;
			if (action.getIdAction() == Actiontype.REPEAT_ACTION) {
				final int times = ((Repeat) action).getPreemptionTimes(ability, null);
				do {
					str = actions[++id].toHtmlString(ability, times, context);
				} while (str == null && id < actions.length - 1);
				if (str == null) {
					str = ((Repeat) action).toHtmlString(ability, context);
				}
			} else {
				str = action.toHtmlString(ability, context);
				if (action.getIdAction() == Actiontype.CHOICE) {
					id += ((InputChoice) action).getSkipHop();
				}
			}
			if (str != null && str.length() > 0) {
				if (!emptyText) {
					buffer.append(", ");
				} else {
					emptyText = false;
				}
				buffer.append(str);
			}
		}
	}

	/**
	 * Return the HTML code representing an action list.
	 * 
	 * @param ability
	 *          the ability owning these actions.
	 * @param actions
	 *          the actions to display.
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this ability.
	 */
	public static String toHtmlString(Ability ability, MAction[] actions,
			ContextEventListener context) {
		final StringBuilder buffer = new StringBuilder();
		toHtmlString(buffer, ability, actions, context);
		return buffer.toString();
	}

	/**
	 * Return the toString() of the given ability.
	 * 
	 * @param ability
	 *          the ability owning these actions.
	 * @return the toString() of the given ability.
	 */
	public static String toString(Ability ability) {
		final String defaultName = ability.getName();
		StringBuilder buffer = new StringBuilder();
		if (defaultName != null && defaultName.length() > 0) {
			return defaultName;
		}
		toString(buffer, ability, ability.actionList());
		if (buffer.length() != 0) {
			buffer.append(" : ");
		}
		toString(buffer, ability, ability.effectList());
		if (buffer.length() == 0) {
			return null;
		}
		return buffer.toString();
	}

	/**
	 * Return the HTML code representing an action list.
	 * 
	 * @param buffer
	 *          the string buffer used to build the HTML code of this ability.
	 * @param ability
	 *          the ability owning these actions.
	 * @param actions
	 *          the actions to display.
	 */
	public static void toString(StringBuilder buffer, Ability ability,
			MAction[] actions) {
		boolean emptyText = true;
		for (int id = 0; id < actions.length; id++) {
			final MAction action = actions[id];
			String str = action.toString(ability);
			if (str != null && str.length() > 0) {
				if (action.getIdAction() == Actiontype.REPEAT_ACTION) {
					String strTmp = null;
					do {
						strTmp = actions[++id].toString(ability);
					} while ((strTmp == null || strTmp.length() == 0)
							&& id < actions.length - 1);
					str = "[" + strTmp + "]" + str;
				}
				if (!emptyText) {
					buffer.append(", ");
				} else {
					emptyText = false;
				}
				buffer.append(str);
				if (actions[id].getIdAction() == Actiontype.CHOICE) {
					id += ((InputChoice) actions[id]).getSkipHop();
				}
			}
		}
		if (emptyText) {
			buffer.append("{no-text}");
		}
	}

}
