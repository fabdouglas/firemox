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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Propose to choose within several valid actions list. If 'hop' attribute is
 * specified and is 'true', cancel option would be available during the choice.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 * @since 0.86 non valid choices are not proposed
 */
public class InputChoice extends MessagingAction {

	/**
	 * Create an instance of InputChoice by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>message [Msg]</li>
	 * <li>allow cancel [boolean]</li>
	 * <li>hop on cancel [Expression]</li>
	 * <li>action index [1]</li>
	 * <li>action is in effects [boolean]</li>
	 * <li>nb choices [1]</li>
	 * <li>size of choice #1 [int]</li>
	 * <li>size of choice #n [int]</li>
	 * <li>actions of choice #1 [Action[]]</li>
	 * <li>hop action value= SIGMA(i={2..n}, nb actions of choice i) [Expression]</li>
	 * <li>actions of choice #2 [Action[]]</li>
	 * <li>hop action value= SIGMA(i={m..n}, nb actions of choice i) [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	InputChoice(InputStream inputFile) throws IOException {
		super(inputFile);
		cancel = inputFile.read() == 1;
		hopOnCancel = ExpressionFactory.readNextExpression(inputFile);
		actionIndex = inputFile.read();
		actionInEffects = inputFile.read() == 1;
		nbChoices = new int[inputFile.read()];
		for (int i = 0; i < nbChoices.length; i++) {
			nbChoices[i] = inputFile.read();
		}
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.CHOICE;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final List<String> actionListStr = getActionList(ability, context);
		final Player controller = (Player) this.controller.getTargetable(ability,
				context, null);

		int startIndex = actionIndex + 1;
		final MAction[] actionList = getActionList(ability);
		boolean one = false;
		for (int i = 0; i < actionListStr.size(); i++) {
			if (isValidChoice(ability, actionList, startIndex, nbChoices[i])) {
				one = true;
			} else {
				actionListStr.set(i, null);
			}
			startIndex += 1 + nbChoices[i];
		}

		if (one) {
			controller.setActivePlayer();
			if (controller.isYou()) {
				replayAction(context, ability,
						new net.sf.firemox.ui.wizard.Choice(ability, cancel,
								actionListStr));
			}
			// opponent waits for our answer
		} else {
			// No available choice, so cancel option is chosen automatically
			StackManager.getInstance().finishSpell();
		}
		return false;
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		StackManager.actionManager.setHop(((Int) actionContext.actionContext)
				.getInt());
		return true;
	}

	@Override
	protected final CoreMessageType getMessagingActionId() {
		return CoreMessageType.CHOICE;
	}

	@Override
	protected void setAnswer(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext) {
		if (optionAnswer == JOptionPane.NO_OPTION) {
			// cancel has been chosen
			final int hopValue = hopOnCancel.getValue(
					StackManager.actionManager.currentAbility, null, StackManager
							.getInstance().getAbilityContext());
			if (hopValue != 0) {
				StackManager.actionManager.setHop(hopValue + getSkipHop());
				StackManager.resolveStack();
			} else {
				StackManager.getInstance().finishSpell();
			}
		} else {
			// choice has been made
			int sum = 1;
			for (int i = 0; i < indexAnswer; i++) {
				sum += nbChoices[i] + (i < indexAnswer - 1 ? 2 : 1);
			}
			StackManager.actionManager.setHop(sum);
			if (actionContext != null) {
				StackManager.actionManager.getActionContext().actionContext = new Int(
						sum);
			}
			StackManager.resolveStack();
		}
	}

	@Override
	public String toString(Ability ability) {
		return "choose";
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName != null) {
			if (actionName.charAt(0) == '%') {
				return "";
			}
			return LanguageManagerMDB.getString(actionName);
		}
		// we return only the string representation
		final List<String> actionListStr = getActionList(ability, context);
		String res = LanguageManagerMDB.getString("choose");
		for (int i = actionListStr.size(); i-- > 0;) {
			res += " - " + actionListStr.get(i);
		}
		return res;
	}

	/**
	 * Verify if this target action may cause abortion of the current ability.
	 * 
	 * @param ability
	 *          is the ability owning this action. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param actionIndex
	 *          the chosen action index
	 * @return true if this target action may cause abortion of the current
	 *         ability.
	 */
	public final boolean checkTarget(Ability ability, int actionIndex) {
		if (cancel) {
			return true;
		}
		int choiceIndex = actionIndex + 1;
		final MAction[] actionList = getActionList(ability);
		for (int i = 0; i < nbChoices.length; i++) {
			if (!isValidChoice(ability, actionList, choiceIndex, nbChoices[i])) {
				return true;
			}
			// next choice, skipping hidden hop action
			choiceIndex += nbChoices[i] + 1;
		}
		// no valid choice
		return false;
	}

	private boolean isValidChoice(Ability ability, MAction[] actionList,
			int startIndex, int length) {
		for (int j = startIndex; j < startIndex + length; j++) {
			if (actionList[j] instanceof ChosenTarget) {
				if (j != 0
						&& actionList[j - 1].getIdAction() == Actiontype.REPEAT_ACTION
						&& !((ChosenTarget) actionList[j]).checkTarget(ability,
								((Repeat) actionList[j - 1]).getPreemptionTimes(ability,
										ability.getCard()))) {
					return false;
				}
				if (!((ChosenTarget) actionList[j]).checkTarget(ability, 1)) {
					return false;
				}
			}
		}
		return true;
	}

	private MAction[] getActionList(Ability ability) {
		if (actionInEffects) {
			return ability.effectList();
		}
		return ability.actionList();
	}

	/**
	 * Return the hop to do to skip this action.
	 * 
	 * @return the hop to do to skip this action.
	 */
	public int getSkipHop() {
		int res = nbChoices.length - 1;
		for (int i = nbChoices.length; i-- > 0;) {
			res += nbChoices[i];
		}
		return res;
	}

	private List<String> getActionList(Ability ability,
			ContextEventListener context) {
		return getActionList(ability, getActionList(ability), actionIndex + 1,
				context);
	}

	/**
	 * @see net.sf.firemox.clickable.ability.UserAbility#toHtmlString()
	 * @see net.sf.firemox.clickable.target.TriggeredCardChoice#getActionList(Ability,
	 *      MAction[], int)
	 * @see java.awt.Choice#getActionList(Ability)
	 */
	private List<String> getActionList(Ability ability, MAction[] actionList,
			int pStartIndex, ContextEventListener context) {
		final List<String> res = new ArrayList<String>();
		int startIndex = pStartIndex;
		// first, identify
		for (int j = 0; j < nbChoices.length; j++) {
			String msg = null;
			final int end = startIndex + nbChoices[j];
			for (int id = startIndex; id < end; id++) {
				final MAction action = actionList[id];
				String str = null;
				if (action.getIdAction() == Actiontype.REPEAT_ACTION) {
					str = actionList[++id].toHtmlString(ability, ((Repeat) action)
							.getPreemptionTimes(ability, null), context);
				} else {
					str = action.toHtmlString(ability, context);
				}
				if (str != null && str.length() > 0) {
					if (msg == null) {
						msg = str;
					} else {
						msg += ", " + str;
					}
				}
			}
			res.add(msg);
			// skip the hop action
			startIndex = end + 1;
		}
		return res;
	}

	/**
	 * Nb actions per choice. Hop actions are not included.
	 */
	private final int[] nbChoices;

	/**
	 * Allow cancel?
	 */
	private final boolean cancel;

	/**
	 * Index of this action within the ability : effects or cost
	 */
	private final int actionIndex;

	/**
	 * Is this action is in effects?
	 */
	private final boolean actionInEffects;

	/**
	 * Optional non zero jump to do when 'cancel' is pressed. Can only be
	 * specified when 'cancel' is allowed. When this attribute is not specified,
	 * 'cancel' cause the ability to be finished.
	 */
	private final Expression hopOnCancel;
}