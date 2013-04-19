/*
 * Created on 2005/0824
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
package net.sf.firemox.clickable.target.card;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.BackgroundMessaging;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.Wizard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class TriggeredCardChoice extends TriggeredCard implements
		MouseListener, BackgroundMessaging {

	/**
	 * @param triggeredAbility
	 *          the triggered ability associated to this card
	 * @param context
	 *          the context of the associated triggered ability
	 * @param abilityID
	 *          is the ability's Id making this triggered ability to be created.
	 */
	public TriggeredCardChoice(Ability triggeredAbility,
			ContextEventListener context, long abilityID) {
		super(triggeredAbility, context, abilityID);
	}

	/**
	 * Add an alternative ability to the associated triggered ability
	 * 
	 * @param ability
	 *          the ability to add to the choice list.
	 * @param context
	 *          the associated context of added ability.
	 */
	public void addChoice(Ability ability, ContextEventListener context) {
		if (either == null) {
			either = new ArrayList<Ability>();
			contexts = new ArrayList<ContextEventListener>();
		}
		either.add(ability);
	}

	@Override
	public boolean newSpell() {
		if (either == null) {
			return super.newSpell();
		}
		// There is at least two abilities, show the chooser wizard
		final List<String> actionListStr = getActionList();
		StackManager.spellController.setActivePlayer();
		if (StackManager.spellController.isYou()) {
			replayAction(context, triggeredAbility,
					new net.sf.firemox.ui.wizard.Choice(triggeredAbility, false,
							actionListStr));
		} else {
			// opponent waits for our answer
			waitingTriggered = this;
		}
		return false;
	}

	/**
	 * Replay the current action as it was when it has been suspended.
	 * 
	 * @param context
	 *          is the context attached to this action.
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param wizard
	 *          the hidden wizard frame
	 */
	public void replayAction(ContextEventListener context, Ability ability,
			Wizard wizard) {
		wizard.setVisible(true);
		if (Wizard.optionAnswer != Wizard.BACKGROUND_OPTION) {
			// valid answer
			final int res = Wizard.optionAnswer * 256 + Wizard.indexAnswer;
			Log.debug("answer was " + res);
			ConnectionManager.send(CoreMessageType.TRIGGERED_CARD_CHOICE,
					(byte) Wizard.indexAnswer);
			setAnswer(Wizard.indexAnswer);
		}
	}

	private List<String> getActionList() {
		final List<String> res = new ArrayList<String>();
		res.add(triggeredAbility.toHtmlString(context));
		for (int i = 0; i < either.size(); i++) {
			res.add(either.get(i).toHtmlString(contexts.get(i)));
		}
		return res;
	}

	/**
	 * The value is piped to the 'modifyregister' action.
	 * 
	 * @param idAnswer
	 *          the answer value.
	 */
	protected void setAnswer(int idAnswer) {
		if (idAnswer > 0) {
			triggeredAbility = either.get(idAnswer - 1);
			context = contexts.get(idAnswer - 1);
		}
		if (StackManager.newSpell(this)) {
			StackManager.resolveStack();
		}

	}

	/**
	 * The call back method when opponent as made the triggered card choice.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void finishedMessage(byte[] data) {
		waitingTriggered.setAnswer(data[0]);
	}

	@Override
	public String getTooltipString() {
		StringBuilder toolTip = new StringBuilder(300);

		// html header and card name
		toolTip.append("<br><b>");
		toolTip.append(LanguageManager.getString("source"));
		toolTip.append(": </b>");
		if (isVisibleForYou()) {
			toolTip.append(database.getLocalName());
			toolTip.append("<br><b>");
			toolTip.append(LanguageManager.getString("triggeredability"));
			toolTip.append(": </b>");
			toolTip.append(triggeredAbility.toHtmlString(context));
			if (either != null) {
				for (int i = 0; i < either.size(); i++) {
					toolTip.append(either.get(i).toHtmlString(context));
				}
			}
			toolTip.append(CardFactory.ttSource);
			toolTip.append(triggeredAbility.getCard().toString());

			// credits
			if (database.getRulesCredit() != null) {
				toolTip.append(CardFactory.ttRulesAuthor);
				toolTip.append(database.getRulesCredit());
			}
		} else {
			toolTip.append("??");
		}
		toolTip.append("</html>");
		return toolTip.toString();
	}

	@Override
	public String toString() {
		if (either == null)
			return new StringBuilder(super.toString()).append("(++ choices ++ :")
					.append(either.size()).append(")").toString();
		return super.toString() + "(++ choices ++ : 0)";
	}

	/**
	 * List of alternatives of triggered abilities. Is null is there is no choice.
	 */
	private List<Ability> either;

	/**
	 * List of contexts associated to alternatives of triggered abilities. Is null
	 * is there is no choice.
	 */
	private List<ContextEventListener> contexts;

	private static TriggeredCardChoice waitingTriggered;
}
