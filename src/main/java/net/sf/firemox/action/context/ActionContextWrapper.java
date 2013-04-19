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
package net.sf.firemox.action.context;

import javax.swing.plaf.basic.BasicHTML;

import net.sf.firemox.action.MAction;
import net.sf.firemox.action.Repeat;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.action.JChosenAction;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public final class ActionContextWrapper {

	/**
	 * Action owning this context
	 */
	public MAction action;

	/**
	 * The JCostAction object associated to this context. Is null if the action is
	 * not a ChosenAction class.
	 */
	public JChosenAction actionUI;

	/**
	 * Specific data saved during the init/... steps.
	 */
	public ActionContext actionContext;

	/**
	 * Action index within the chain of cost
	 */
	public int contextID;

	/**
	 * Times to repeat this action
	 */
	public int repeat;

	/**
	 * Times already done. In range [0,repeat].
	 */
	public int done;

	/**
	 * Is <code>-1</code> if this action has not been completed. Otherwise this
	 * corresponds to the record id. Start from <code>0</code>.
	 */
	public int recordIndex = -1;

	/**
	 * Cached toString() value of this action. Initialize at the first use.
	 */
	private String actionStr = null;

	/**
	 * Is this action is completed?
	 * 
	 * @return true if this action is completed.
	 */
	public boolean isCompleted() {
		return recordIndex != -1;
	}

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
	 * @since 0.85
	 */
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (isRefreshText()) {
			actionStr = null;
			final MAction[] actions = StackManager.actionManager.actionList();
			int id = contextID;
			do {
				final MAction action = actions[id];
				String str = null;
				if (action instanceof ChosenAction) {
					str = ((ChosenAction) action).toHtmlString(ability, context, this);
				} else {
					str = action.toHtmlString(ability, context);
				}
				if (str != null && str.length() > 0) {
					if (actionStr == null) {
						actionStr = str;
					} else {
						actionStr += ", " + str;
					}
				}
			} while (++id < actions.length && !(actions[id] instanceof ChosenAction)
					&& !(actions[id] instanceof InitAction)
					&& !(actions[id] instanceof Repeat));
			if (actionStr == null) {
				actionStr = "{unknown}";
			}
			BasicHTML.updateRenderer(actionUI, "<html>" + actionStr);
		}
		// if (repeat > 1)
		// return "(" + done + "/" + repeat + ")" + actionStr;
		return actionStr;
	}

	/**
	 * Refresh the text of associated action UI component.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 */
	public void refreshText(Ability ability, ContextEventListener context) {
		actionStr = null;
		if (actionUI != null) {
			// is action is already associated to a JCostAction, update it
			toHtmlString(ability, context);
			actionUI.repaint();
		}
	}

	/**
	 * Is the text is being to be refreshed.
	 * 
	 * @return true if the text is being to be refreshed.
	 */
	public boolean isRefreshText() {
		return actionStr == null;
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param contextID
	 *          the action ID.
	 * @param action
	 *          the action.
	 * @param actionContext
	 *          the context of this action
	 * @param repeat
	 *          times this action has to be refreshed.
	 */
	public ActionContextWrapper(int contextID, MAction action,
			ActionContext actionContext, int repeat) {
		this.contextID = contextID;
		this.actionContext = actionContext;
		this.action = action;
		this.repeat = repeat;
		done = 0;
	}

}
