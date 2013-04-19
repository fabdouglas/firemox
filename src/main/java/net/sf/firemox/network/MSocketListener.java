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
package net.sf.firemox.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.firemox.action.MessagingAction;
import net.sf.firemox.action.MoveCard;
import net.sf.firemox.action.PayMana;
import net.sf.firemox.clickable.ability.UserAbility;
import net.sf.firemox.clickable.mana.ManaPool;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.card.TriggeredCardChoice;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.network.message.CoreMessage;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * This class is listening to the actions done by the opponent and apply the
 * corresponding actions. These actions may be : click on cards, ability chosen,
 * mana use and auto-options update.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.4
 */
public final class MSocketListener extends Thread {

	/**
	 * Private constructor used to create the unique instance of this class
	 */
	private MSocketListener() {
		super("SocketListener");
		start();
	}

	/**
	 * Create a new instance of this class.
	 */
	public static void init() {
		instance = new MSocketListener();
	}

	/**
	 * The unique instance of this class.
	 */
	private static MSocketListener instance;

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static MSocketListener getInstance() {
		return instance;
	}

	/**
	 * run this thread
	 * 
	 * @see IdMessages
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Synchronizer.SYNCHRONIZER.take();
				final CoreMessage message;
				synchronized (messages) {
					while (messages.isEmpty()) {
						messages.wait();
					}
				}
				message = messages.peek();
				StackManager.noReplayToken.take();
				switch (message.getType()) {
				case CHOICE:
				case COLOR_ANSWER:
				case ANSWER:
				case INTEGER_ANSWER:
				case ZONE_ANSWER:
				case PROPERTY_ANSWER:
					MessagingAction.finishedRemoteMessage(message.getData());
					break;
				case SKIP:
					Log.debug("      ...-> manual skip");
					Player.unsetHandedPlayer();
					StackManager.actionManager.manualSkip();
					break;
				case CLICK_CARD:
					MCard.clickOn(message.getData());
					break;
				case CLICK_ABILITY:
					UserAbility.clickOn(message.getData());
					break;
				case CLICK_PLAYER:
					Player.clickOn(message.getData());
					break;
				case CLICK_ACTION:
					MagicUIComponents.chosenCostPanel.clickOn(message.getData());
					break;
				case CLICK_MANA:
					ManaPool.clickOn(message.getData());
					break;
				case CLICK_PAY_MANA:
					PayMana.clickOn(message.getData());
					break;
				case CLICK_TRIGGERED_CARD:
					TriggeredCard.clickOn(message.getData());
					break;
				case MOVE_ORDER_ANSWER:
					((MoveCard) StackManager.actionManager.currentAction)
							.receiveMoveOrder(message.getData());
					break;
				case TRIGGERED_CARD_CHOICE:
					TriggeredCardChoice.finishedMessage(message.getData());
					break;
				case DISCONNECT:
					// disconnection
					ConnectionManager.notifyDisconnection();
					ConnectionManager.closeConnexions();
					break;
				case REPLACEMENT_ANSWER:
					synchronized (messages) {
						messages.notify();
					}
					StackManager.noReplayToken.release();
					continue;
				default:
					Log.fatal("Unknown message type " + message.getType());
				}
				messages.remove();
				synchronized (messages) {
					messages.notify();
				}
				StackManager.noReplayToken.release();
			}
		} catch (Throwable e) {
			Log.fatal("In SocketListener, occurred exception", e);
			ConnectionManager.notifyDisconnection();
			ConnectionManager.closeConnexions();
			return;
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	/**
	 * Read and return replacement answer.
	 * 
	 * @return the replacement answer.
	 */
	public int readReplacementAnswer() {
		while (messages.isEmpty()) {
			synchronized (messages) {
				try {
					messages.wait();
				} catch (InterruptedException e) {
					return -1;
				}
				if (!messages.isEmpty()
						&& messages.peek().getType() == CoreMessageType.REPLACEMENT_ANSWER) {
					final CoreMessage message = messages.remove();
					synchronized (messages) {
						messages.notify();
					}
					return MToolKit.readInt16(message.getData()[0], message.getData()[1]);
				}
			}
		}
		return -1;
	}

	/**
	 * Close connections.
	 */
	public void closeConnections() {
		stack(new CoreMessage(CoreMessageType.DISCONNECT));
	}

	/**
	 * Queued messages.
	 */
	private final Queue<CoreMessage> messages = new ConcurrentLinkedQueue<CoreMessage>();

	/**
	 * Add the given message to our LIFO queue.
	 * 
	 * @param message
	 *          the input message.
	 */
	public void stack(CoreMessage message) {
		messages.add(message);
		synchronized (messages) {
			messages.notify();
		}
	}

}