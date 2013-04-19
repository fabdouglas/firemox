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
 * 
 */
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.ActionFactory;
import net.sf.firemox.action.Actiontype;
import net.sf.firemox.action.MAction;
import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.RemoveObject;
import net.sf.firemox.action.Repeat;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.target.AbstractTarget;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.action.ToStringHelper;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.EventFactory;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.tools.Log;

/**
 * A non-abstract ability.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class UserAbility extends Ability {

	/**
	 * Create an instance of UserAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>event [Event]</li>
	 * <li>cost [Action[]]</li>
	 * <li>effect [Action[]]</li>
	 * </ul>
	 * 
	 * @param input
	 *          stream containing this ability.
	 * @param card
	 *          referenced card owning this ability.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream.
	 * @since 0.81 constraints on actions of this ability are supported.
	 */
	public UserAbility(InputStream input, MCard card) throws IOException {
		super(input);
		try {
			// Read id event
			eventComing = EventFactory.readNextEvent(input, card);

			// Read list of actions
			actionList = ActionFactory.readActionList(input, null);

			// Extract constraints
			eventComing.test = ActionFactory.getConstraints(actionList,
					eventComing.test);

			// Optimize target options
			updateCheckingAction();

			// Read list of effect
			effectList = ActionFactory.readActionList(input, null);
		} catch (Throwable e) {
			throw new RuntimeException("reading UserAbility " + getName() + ", card:"
					+ card.getName(), e);
		}
	}

	/**
	 * Create an instance of UserAbility
	 * 
	 * @param name
	 *          Name of card used to display this ability in a stack
	 * @param actionList
	 *          list of actions to do for activate this ability
	 * @param effectList
	 *          list of effects of this ability
	 * @param optimizer
	 *          the optimizer to use.
	 * @param priority
	 *          the resolution type.
	 * @param eventComing
	 *          event condition of this ability
	 * @param pictureName
	 *          the picture name of this ability. If <code>null</code> the card
	 *          picture will be used instead.
	 */
	protected UserAbility(String name, MAction[] actionList,
			MAction[] effectList, Optimization optimizer, Priority priority,
			MEventListener eventComing, String pictureName) {
		super(name, optimizer, priority, pictureName);
		this.actionList = actionList;
		this.effectList = effectList;
		this.eventComing = eventComing;
		updateCheckingAction();
	}

	private void updateCheckingAction() {
		// Optimize target options
		checkingOptions = NO_TARGET;
		for (int i = actionList.length; i-- > 0;) {
			if (actionList()[i] instanceof ChosenTarget) {
				if (((ChosenTarget) actionList()[i]).isTargeting()) {
					checkingOptions |= REAL_TARGET;
				} else {
					checkingOptions |= SILENT_TARGET;
				}
			} else if (actionList()[i] instanceof RemoveObject && i > 0) {
				if (actionList()[i - 1] instanceof Repeat) {
					if (i > 1
							&& actionList()[i - 2] instanceof AbstractTarget
							&& ((Repeat) actionList()[i - 1]).getPreemptionTimes(this, null) > 0) {
						checkingOptions |= REMOVE_OBJECT;
					}
				} else if (actionList()[i - 1] instanceof AbstractTarget) {
					checkingOptions |= REMOVE_OBJECT;
				}
			}
		}
	}

	/**
	 * Return a copy of this ability.
	 * 
	 * @param container
	 *          the new owner of returned ability.
	 * @return copy of this ability
	 */
	@Override
	public abstract Ability clone(MCard container);

	@Override
	public MCard getCard() {
		return eventComing.card;
	}

	@Override
	public final boolean checkTargetActions() {
		return (checkingOptions & TARGET_OPTIONS) == NO_TARGET
				|| super.checkTargetActions();
	}

	@Override
	public final boolean checkObjectActions() {
		return (checkingOptions & OBJECT_OPTIONS) == NO_TARGET
				|| super.checkObjectActions();
	}

	@Override
	public final boolean recheckTargets() {
		if ((checkingOptions & TARGET_OPTIONS) == REAL_TARGET) {
			return super.recheckTargets();
		}
		return true;
	}

	@Override
	public boolean isMatching() {

		// enougth mana?
		final int[] manaNeeded = manaNeeded(null);
		int unUsed = 0;
		for (int i = IdCommonToken.COLOR_NAMES.length; i-- > IdCommonToken.BLACK_MANA;) {
			if (manaNeeded[i] > getController().mana.getMana(i, this)) {
				return false;
			}
			if (manaNeeded[i] > 0) {
				unUsed += getController().mana.getMana(i, this) - manaNeeded[i];
			} else {
				unUsed += getController().mana.getMana(i, this);
			}
		}
		if (manaNeeded[IdCommonToken.COLORLESS_MANA] > getController().mana
				.getMana(IdCommonToken.COLORLESS_MANA, this)
				+ unUsed) {
			return false;
		}

		// check the target actions of 'cost' part can be played
		return checkTargetActions() && checkObjectActions();
	}

	/**
	 * return the amount of mana needed (constant part only) to play this ability
	 * 
	 * @param context
	 *          the current context of this ability.
	 * @return array of mana needed to play this ability
	 */
	private int[] manaNeeded(ContextEventListener context) {
		final int[] result = new int[IdCommonToken.COLOR_NAMES.length];
		for (MAction action : actionList()) {
			if (action.getIdAction() == Actiontype.PAY_MANA) {
				// this ability need manas
				final int[] actionCost = ((PayMana) action).manaNeeded(this, context);
				for (int i = result.length; i-- > 0;) {
					result[i] += actionCost[i];
				}
			}
		}
		for (MAction action : effectList()) {
			if (action.getIdAction() == Actiontype.PAY_MANA) {
				// this ability need manas
				final int[] actionCost = ((PayMana) action).manaNeeded(this, context);
				for (int i = result.length; i-- > 0;) {
					result[i] += actionCost[i];
				}
			}
		}
		return result;
	}

	@Override
	public final String toString() {
		return ToStringHelper.toString(this);
	}

	@Override
	public String toHtmlString(ContextEventListener context) {
		return ToStringHelper.toHtmlString(this, context);
	}

	@Override
	public final MEventListener eventComing() {
		return eventComing;
	}

	@Override
	public final MAction[] actionList() {
		return actionList;
	}

	@Override
	public final MAction[] effectList() {
		return effectList;
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		if (isHidden()) {
			getController().zoneManager.triggeredBuffer.addHidden(this, context);
		} else {
			getController().zoneManager.triggeredBuffer
					.add(getTriggeredClone(context));
		}
		return true;
	}

	/**
	 * is called when you click on me
	 * 
	 * @param index
	 *          is the index of this ability within the choice list of playable
	 *          abilities of the card owning this ability.
	 */
	public final void mouseClicked(int index) {
		if (StackManager.actionManager.clickOn(this)) {
			Log.debug("clickOn(Mouse):" + getCard() + " -> " + this);
			// send this information to our opponent : card + ability
			final byte[] cardBytes = getCard().getBytes();
			final byte[] toSend = new byte[1 + cardBytes.length];
			System.arraycopy(cardBytes, 0, toSend, 0, cardBytes.length);
			toSend[cardBytes.length] = (byte) index;
			// we inform opponent our choice
			ConnectionManager.send(CoreMessageType.CLICK_ABILITY, toSend);
			StackManager.actionManager.succeedClickOn(this);
		}
	}

	/**
	 * This method is invoked when opponent has clicked on this object. this call
	 * should be done from the event listener.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// waiting for card information
		final MCard card = MCard.getCard(data);
		Log.debug("clickOn(VirtualInput):" + card);

		final int index = data[data.length - 1];
		if (index >= 128) {
			// advanced activated ability
			StackManager.actionManager
					.succeedClickOn(((WaitingAbility) StackManager.actionManager.currentAction)
							.advancedAbilitiesOf(card).get(index - 128));
		} else {
			// normal activated ability
			StackManager.actionManager
					.succeedClickOn(((WaitingAbility) StackManager.actionManager.currentAction)
							.abilitiesOf(card).get(index));
		}
	}

	/**
	 * will contains MAction objects of cost part
	 */
	protected MAction[] actionList;

	/**
	 * will contains MAction objects of effect part
	 */
	protected MAction[] effectList;

	private static final int REAL_TARGET = 2;

	private static final int SILENT_TARGET = 1;

	private static final int NO_TARGET = 0;

	private static final int TARGET_OPTIONS = 3;

	private static final int OBJECT_OPTIONS = 4;

	private static final int REMOVE_OBJECT = 4;

	/**
	 * Is this ability contains targeting or removing object action(s).
	 */
	protected int checkingOptions;

}