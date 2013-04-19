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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.ui.wizard.Wizard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public abstract class MessagingAction extends UserAction implements
		BackgroundMessaging, StandardAction, InitAction {

	/**
	 * Create an instance of message by reading a file Offset's file must pointing
	 * on the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>controller [TestOn]</li>
	 * <li>text to display +'/0' [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	MessagingAction(InputStream inputFile) throws IOException {
		super(inputFile);
		controller = TestOn.deserialize(inputFile);
		text = MToolKit.readString(inputFile);
		if (text.startsWith("$") || text.startsWith("%")) {
			text = LanguageManagerMDB.getString(text.substring(1));
		}
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
	public final void replayAction(ContextEventListener context, Ability ability,
			Wizard wizard) {
		wizard.setContext(context);
		wizard.setAction(this);
		wizard.setVisible(true);
		if (Wizard.optionAnswer != Wizard.BACKGROUND_OPTION) {
			// valid answer
			boolean taken = false;
			if (!StackManager.noReplayToken.takeNoBlock()) {
				taken = true;
			}
			ConnectionManager.send(getMessagingActionId(),
					(byte) Wizard.optionAnswer, (byte) Wizard.indexAnswer);
			finishedMessage(Wizard.optionAnswer, Wizard.indexAnswer, context,
					ability, StackManager.actionManager.getActionContextNull());
			if (taken) {
				StackManager.noReplayToken.release();
			}
		}
	}

	/**
	 * The returned value is piped to the 'modify-register' action.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static final void finishedRemoteMessage(byte[] data) {
		finishedMessage(data[0], data[1], StackManager.getInstance()
				.getAbilityContext(), StackManager.currentAbility,
				StackManager.actionManager.getActionContextNull());
	}

	/**
	 * The returned value is piped to the 'modify-register' action.
	 * 
	 * @param optionAnswer
	 *          the option value. yes/no/cancel,...
	 * @param indexAnswer
	 *          the optional value.
	 * @param context
	 *          the context of playing ability.
	 * @param ability
	 *          the playing ability.
	 * @param actionContext
	 *          the context of this action.
	 */
	public static final void finishedMessage(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext) {
		Log.debug("option was " + optionAnswer + ", index was " + indexAnswer);
		Player.unsetHandedPlayer();
		((MessagingAction) StackManager.actionManager.currentAction).setAnswer(
				optionAnswer, indexAnswer, context, ability, actionContext);
	}

	public final boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		play(context, ability);
		return false;
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// nothing to do
		return true;
	}

	public abstract boolean play(ContextEventListener context, Ability ability);

	/**
	 * The value is piped to the 'modify-register' action.
	 * 
	 * @param optionAnswer
	 *          the option value. yes/no/cancel,...
	 * @param indexAnswer
	 *          the optional value.
	 * @param context
	 *          the context of playing ability.
	 * @param ability
	 *          the playing ability.
	 * @param actionContext
	 *          the context of this action.
	 */
	protected abstract void setAnswer(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext);

	/**
	 * Return the message id
	 * 
	 * @return the message id
	 */
	protected abstract CoreMessageType getMessagingActionId();

	/**
	 * The the player would answer to this message.
	 */
	protected TestOn controller;

	/**
	 * The text to display
	 */
	protected String text;
}
