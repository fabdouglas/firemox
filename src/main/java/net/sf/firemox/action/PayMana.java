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
import java.util.List;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.ManaCost;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.RollBackAction;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.listener.WaitingMana;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.mana.Mana;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.RegisterAccess;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.MCommonVars;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.MZone;

/**
 * Used to pay mana, remove directly mana from the mana pool <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.70 'auto use colored mana as colorless mana' AI has been upgraded to
 *        manage case where only one colored mana can be used as colorless mana
 * @since 0.71 'auto use colored mana as colorless mana' AI has been upgraded to
 *        prevent the opponent to be informed you are using this option.
 * @since 0.85 Mana pay can be aborted.
 * @see net.sf.firemox.action.Actiontype#PAY_MANA
 */
public class PayMana extends UserAction implements ChosenAction, InitAction,
		WaitingMana, WaitingAbility, RollBackAction {

	/**
	 * If true, mana operation are nor ignored.
	 */
	public static boolean useMana;

	/**
	 * The maximum number of mana symbols displayed in context menu.
	 */
	public static int thresholdColored;

	/**
	 * This method is invoked when opponent has clicked on manas.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// waiting for mana information
		if (!(StackManager.actionManager.currentAction instanceof PayMana)) {
			Log.fatal("Current action is not 'pay mana' but '"
					+ StackManager.actionManager.currentAction.getClass().getName());
		}

		final int color = data[0];
		final PayMana action = (PayMana) StackManager.actionManager.currentAction;
		StackManager.actionManager.succeedClickOn(action.controller.getPlayer(
				StackManager.currentAbility, null).mana.manaButtons[color]);
	}

	/**
	 * Return Html string corresponding to the given mana pool. Colors with no
	 * mana in the mana pool are ignored. If a color appear more than
	 * THRESHOLD_COLORED, the <code>{color} x {amount}</code> will be used. If
	 * the given code is empty the <code>{0}</code> value will be returned.
	 * 
	 * @param manaPool
	 *          the amount of mana to pay
	 * @return Html string corresponding to the given mana pool.
	 */
	public static String toHtmlString(int[] manaPool) {
		String res = null;

		if (manaPool[0] != 0) {
			res = MToolKit.getHtmlMana(0, manaPool[0]);
		}

		for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 1;) {
			if (manaPool[j] != 0) {
				if (res == null) {
					res = "";
				}
				if (manaPool[j] > PayMana.thresholdColored) {
					res += MToolKit.getHtmlMana(j, 1) + "x" + manaPool[j];
				} else {
					res += MToolKit.getHtmlMana(j, manaPool[j]);
				}
			}
		}

		if (res == null) {
			return " " + MToolKit.getHtmlMana(0, 0);
		}
		return res;
	}

	/**
	 * The test manager
	 */
	private TestOn on;

	/**
	 * The complex expression to use for the right value. Is null if the IdToken
	 * number is not a complex expression.
	 */
	private Expression[] codeExpr = null;

	/**
	 * The player paying this mana
	 */
	public TestOn controller;

	/**
	 * Create an instance of PayMana by reading a file Offset's file must pointing
	 * on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>the player paying this mana [TestOn]</li>
	 * <li>mode IdTokens#MANA_POOL[2]
	 * <li>[on [Expression]]</li>
	 * <li>COLORLESS [Expression]</li>
	 * <li>BLACK [Expression]</li>
	 * <li>BLUE [Expression]</li>
	 * <li>GREEN [Expression]</li>
	 * <li>RED [Expression]</li>
	 * <li>WHITE [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	PayMana(InputStream inputFile) throws IOException {
		super(inputFile);
		controller = TestOn.deserialize(inputFile);
		int code0 = MToolKit.readInt16(inputFile);
		if (code0 == IdTokens.MANA_POOL) {
			on = TestOn.deserialize(inputFile);
		} else {
			codeExpr = new Expression[IdCommonToken.PAYABLE_COLOR_NAMES.length];
			for (int i = 0; i < codeExpr.length; i++) {
				codeExpr[i] = ExpressionFactory.readNextExpression(inputFile);
			}
		}
	}

	public List<Ability> abilitiesOf(MCard card) {
		return WaitChosenActionChoice.getInstance().abilitiesOf(card);
	}

	public List<Ability> advancedAbilitiesOf(MCard card) {
		return WaitChosenActionChoice.getInstance().advancedAbilitiesOf(card);
	}

	public boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		if (!useMana) {
			return true;
		}

		final Player controller = this.controller.getPlayer(ability, context, null);
		final ManaCost manaContext = (ManaCost) actionContext.actionContext;

		if (manaContext.isNullRequired()) {
			// no more mana to pay
			return true;
		}

		// highlight the selectable mana
		for (int idColor = IdCardColors.CARD_COLOR_NAMES.length; idColor-- > 0;) {
			if (controller.mana.getMana(idColor, ability) > 0
					&& (manaContext.requiredMana[idColor] > 0 || manaContext.requiredMana[0] > 0)) {
				controller.mana.manaButtons[idColor].highLight(null);

			} else {
				controller.mana.manaButtons[idColor].disHighLight();
			}
		}
		controller.mana.repaint();

		// update the ticket
		MagicUIComponents.magicForm.moreInfoLbl.setText("<html>"
				+ LanguageManager.getString("manatopay",
						toHtmlString(manaContext.manaPaid),
						toHtmlString(manaContext.requiredMana)));
		actionContext.refreshText(ability, context);

		// display playable mana source abilities
		WaitChosenActionChoice.getInstance().play(controller);

		if (controller.isYou() && MCommonVars.autoMana) {
			// enougth colored mana?
			int remainingColoredMana = 0;
			int uniqueColored = -1;

			for (int i = IdCardColors.CARD_COLOR_NAMES.length; i-- > 1;) {
				if (manaContext.requiredMana[i] > controller.mana.getMana(i, ability)) {
					// no enougth colored mana
					return false;
				}

				// calculate remaining colored mana
				if (remainingColoredMana != 0) {
					uniqueColored = -1;
				} else {
					uniqueColored = i;
				}
				remainingColoredMana += controller.mana.getMana(i, ability)
						- manaContext.requiredMana[i];
			}

			if (manaContext.requiredMana[0] <= controller.mana.getMana(0, ability)) {
				// we can pay all required colorless mana as normal
				validateCode(manaContext.requiredMana, controller);
			} else if (manaContext.requiredMana[0] == controller.mana.getMana(0,
					ability)
					+ remainingColoredMana) {
				// we spend all remaining colored mana to pay the required colorless
				final int[] validCode = new int[IdCardColors.CARD_COLOR_NAMES.length];
				for (int i = validCode.length; i-- > 0;) {
					validCode[i] = controller.mana.getMana(i, ability);
				}
				validateCode(validCode, controller);
			} else if (manaContext.requiredMana[0] < controller.mana.getMana(0)
					+ remainingColoredMana) {
				// we have enough colored mana to pay required colorless mana
				if (uniqueColored != -1) {
					// only one color can be used as colorless mana
					final int[] validCode = new int[6];
					validCode[0] = controller.mana.getMana(0, ability);
					for (int i = validCode.length; i-- > 1;) {
						validCode[i] = manaContext.requiredMana[i];
					}
					validCode[uniqueColored] += manaContext.requiredMana[0]
							- validCode[0];
					validateCode(validCode, controller);
				} else {
					// colored mana will be automatically used as colorless mana
					final int[] coloredHand = new int[IdCardColors.CARD_COLOR_NAMES.length];
					final MZone hand = controller.zoneManager.hand;
					// count all manas used for all cards in the hand
					for (int i = hand.getComponentCount(); i-- > 0;) {
						final int[] colors = hand.getCard(i).cachedRegisters;
						for (int color = coloredHand.length; color-- > 1;) {
							if (colors[color] > coloredHand[color - 1]) {
								coloredHand[color] = colors[color];
							}
						}
					}

					int colorlessToPay = manaContext.requiredMana[0]
							- controller.mana.getMana(0, ability);
					int totalColored = 0;
					final int[] manaPaid = new int[IdCardColors.CARD_COLOR_NAMES.length];
					for (int color = manaPaid.length; color-- > 1;) {
						manaPaid[color] = manaContext.requiredMana[color];
						totalColored = 1 + coloredHand[color];
					}
					manaPaid[0] = controller.mana.getMana(0, ability);
					final int ratioRemove = totalColored < colorlessToPay ? 1
							: totalColored / colorlessToPay;

					while (colorlessToPay-- > 0) {
						int miniColor = -1;
						int mini = 0;
						for (int color = manaPaid.length; color-- > 1;) {
							if ((miniColor == -1 || mini > coloredHand[color])
									&& controller.mana.getMana(color, ability) - manaPaid[color] > 0) {
								// we setthe nex=t color to remove
								miniColor = color;
								mini = coloredHand[color];
							}
						}
						if (miniColor == -1) {
							throw new InternalError("couldn't find the color to use");
						}
						coloredHand[miniColor] -= ratioRemove;
						manaPaid[miniColor]++;
					}
					validateCode(manaPaid, controller);
				}
			}
			return false;
		}

		return false;
	}

	public boolean clickOn(Ability ability) {
		return WaitChosenActionChoice.getInstance().clickOn(ability);
	}

	public boolean clickOn(Mana mana) {
		final Player controller = this.controller.getPlayer(
				StackManager.currentAbility, null);
		final ManaCost manaContext = (ManaCost) StackManager.actionManager
				.getActionContext().actionContext;
		// one less colorless mana to pay
		if (controller.isYou() && mana.getMana() > 0) {
			if (mana.color == 0) {
				return manaContext.requiredMana[0] > 0;
			}
			return manaContext.requiredMana[mana.color] > 0
					|| manaContext.requiredMana[0] > 0;
		}
		return false;
	}

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
	public void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// mana paid are not restored, this action is simply paused
		controller.getPlayer(ability, context, null).mana.disHighLight();
	}

	/**
	 * Called when this action is finished (aborted or completed). No stack
	 * operation should be done here.<br>
	 * For this action, all mana are dishilighted.
	 */
	public void finished() {
		final Player controller = this.controller.getPlayer(
				StackManager.currentAbility, null);
		for (Mana mana : controller.mana.manaButtons) {
			mana.disHighLight();
		}
		WaitChosenActionChoice.getInstance().finished();
	}

	private int[] getCode(Ability ability, ContextEventListener context) {
		if (on != null) {
			return on.getCard(ability, null).cachedRegisters;
		}

		final int[] code = new int[codeExpr.length];
		for (int i = 6; i-- > 0;) {
			final Expression expr = codeExpr[i];
			if (expr instanceof RegisterAccess && ((RegisterAccess) expr).isXvalue()) {
				code[i] = -1;
			} else {
				code[i] = codeExpr[i].getValue(ability, null, context);
			}
		}
		return code;
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.PAY_MANA;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// Calculate the needed mana only if this not already done.
		if (actionContext.actionContext == null) {
			actionContext.actionContext = new ManaCost(manaNeeded(ability, context));
		}
		return true;
	}

	/**
	 * Return the amount of mana needed (constant part only) to play this ability
	 * As default, we return an empty number for all manas.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the context of playing ability.
	 * @return array of mana needed to play this ability
	 */
	public int[] manaNeeded(Ability ability, ContextEventListener context) {
		if (!useMana) {
			return IdConst.EMPTY_CODE;
		}

		// this action need our manas
		if (on != null) {
			return on.getCard(ability, null).cachedRegisters;
		}
		final int[] realCode = new int[codeExpr.length];
		for (int i = realCode.length; i-- > 0;) {
			realCode[i] = codeExpr[i].getValue(ability, null, context);
		}
		return realCode;
	}

	public boolean manualSkip() {
		// cancel the ability since required manas are not paid
		StackManager.cancel();
		return false;
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		assert ((ManaCost) actionContext.actionContext).isNullRequired();
		// pay the mana
		final int[] manaPaid = ((ManaCost) actionContext.actionContext).manaPaid;
		final Player controller = this.controller.getPlayer(ability, context, null);
		for (int idColor = controller.mana.manaButtons.length; idColor-- > 0;) {
			controller.mana.removeMana(idColor, manaPaid[idColor], ability);
		}
		return true;
	}

	/**
	 * No generated event. Rollback an action.
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
	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// restore the mana pool
		final Player controller = this.controller.getPlayer(
				StackManager.currentAbility, context, null);
		((ManaCost) actionContext.actionContext).restoreMana(controller.mana);
	}

	public boolean succeedClickOn(Ability ability) {
		finished();
		StackManager.newSpell(ability, ability.isMatching());
		return true;
	}

	public boolean succeedClickOn(Mana mana) {
		finished();
		final Player controller = this.controller.getPlayer(
				StackManager.currentAbility, null);

		if (mana.getMana() == 0) {
			Log.fatal("Player has not this mana : " + mana);
		}

		final ManaCost manaCost = (ManaCost) StackManager.actionManager
				.getActionContext().actionContext;
		if (mana.color == 0) {
			if (manaCost.requiredMana[mana.color] > 0
					&& controller.mana.getMana(0) > 0) {
				manaCost.payMana(0, 0, 1, controller.mana);
			} else {
				Log.fatal("No colorless mana can be paid this way : " + mana);
			}
		} else {
			if (manaCost.requiredMana[mana.color] > 0) {
				// pay a colored mana
				manaCost.payMana(mana.color, mana.color, 1, controller.mana);
			} else if (manaCost.requiredMana[0] > 0) {
				// pay a colorless mana with colored one
				manaCost.payMana(0, mana.color, 1, controller.mana);
			} else {
				Log.fatal("This colored mana is not required : " + mana);
			}
		}

		// send the event to opponent
		if (controller.isYou()) {
			ConnectionManager.send(CoreMessageType.CLICK_PAY_MANA, (byte) mana.color);
		}

		// check if there is a required mana to pay
		return choose(StackManager.actionManager.getActionContext(), StackManager
				.getInstance().getAbilityContext(), StackManager.currentAbility);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return toHtmlString(getCode(ability, context));
	}

	public String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		final ManaCost manaCost = (ManaCost) actionContext.actionContext;
		for (int color = manaCost.requiredMana.length; color-- > 0;) {
			if (manaCost.requiredMana[color] > 0) {
				// at least one required mana
				return toHtmlString(manaCost.manaCost) + ", required "
						+ toHtmlString(manaCost.requiredMana);
			}
		}
		// no remaining mana
		return toHtmlString(manaCost.manaCost);
	}

	@Override
	public String toString(Ability ability) {
		String res = null;

		// this action need our manas
		final int[] code = getCode(ability, null);

		if (code[0] != 0) {
			res = "" + code[0];
		}

		for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 1;) {
			if (code[j] != 0) {
				if (res == null) {
					res = "";
				}
				res += IdCommonToken.COLOR_NAMES[j] + "x" + code[j] + ",";
			}
		}
		if (res == null) {
			return MToolKit.getHtmlMana(0, 0);
		}
		return res;
	}

	private void validateCode(int[] valideMana, Player controller) {
		for (int i = controller.mana.manaButtons.length; i-- > 0;) {
			if (valideMana[i] > 0) {
				StackManager.actionManager
						.succeedClickOn(controller.mana.manaButtons[i]);
				return;
			}
		}
	}
}