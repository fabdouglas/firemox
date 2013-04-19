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

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @see net.sf.firemox.action.Actiontype
 * @since 0.85 named action is supported.
 */
public abstract class UserAction extends MAction {

	/**
	 * Create an instance of MAction
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>action name [String]</li>
	 * <li>debug data [String]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          stream containing this action.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected UserAction(InputStream inputStream) throws IOException {
		this(MToolKit.readString(inputStream), inputStream);
	}

	/**
	 * Creates a new instance of UserAction <br>
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>action name [String]</li>
	 * </ul>
	 * 
	 * @param actionName
	 *          the action's name.
	 * @param inputStream
	 *          stream containing this action.
	 */
	protected UserAction(String actionName, InputStream inputStream) {
		this(actionName, MToolKit.readText(inputStream));
	}

	/**
	 * Creates a new instance of UserAction <br>
	 * 
	 * @param debugData
	 *          debug information.
	 * @param actionName
	 *          the action's name.
	 */
	protected UserAction(String actionName, String debugData) {
		super(debugData);
		if (actionName != null) {
			if (actionName.length() == 0) {
				this.actionName = null;
			} else {
				this.actionName = actionName.intern();
			}
		}
		ActionFactory.currentAction = this;
	}

	/**
	 * Creates a new instance of UserAction <br>
	 * 
	 * @param actionName
	 *          the action's name.
	 */
	protected UserAction(String actionName) {
		this(actionName, (String) null);
	}

	@Override
	public abstract Actiontype getIdAction();

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName != null) {
			if (actionName.charAt(0) == '%') {
				return "";
			}
			if (actionName.charAt(0) == '@') {
				final String picture = ActionFactory.PICTURES.get(actionName);
				if (picture != null) {
					return toHtmlString(ability, picture);
				}
			}
			if (actionName.indexOf("%n") != -1) {
				return LanguageManagerMDB.getString(actionName.replaceAll("%n", "1"));
			}
			return StringUtils.replaceOnce(LanguageManagerMDB.getString(actionName),
					"{this}", ability.getCard().toString());
		}
		// we return only the string representation
		return toString(ability);
	}

	@Override
	public String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (actionName != null && actionName.charAt(0) != '%'
				&& actionName.charAt(0) != '@' && actionName.indexOf("%n") != -1) {
			if (times == 1) {
				return LanguageManagerMDB.getString(actionName.replaceAll("%n", "1"));
			}
			return LanguageManagerMDB.getString(actionName).replaceAll("%n",
					"" + times);
		}
		// we return only the string representation
		return super.toHtmlString(ability, times, context);
	}

	@Override
	public abstract String toString(Ability ability);

	/**
	 * Verify the timestamp of the specified card
	 * 
	 * @param context
	 *          the context to use to determine whether the timestamp is correct.
	 * @param card
	 *          the concerned object.
	 * @return true if the timestamp is correct.
	 */
	protected static boolean checkTimeStamp(ContextEventListener context,
			MCard card) {
		if (context != null && !context.checkTimeStamp(card)) {
			Log.debug("\t... bad timestamp");
			return false;
		}
		return true;
	}

	@Override
	public String getActionName() {
		if (actionName != null
				&& (actionName.startsWith("%") || actionName.startsWith("@"))) {
			return actionName.substring(1);
		}
		return actionName;
	}

	/**
	 * The name of this action
	 */
	protected String actionName;
}