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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.RemoveModifier;
import net.sf.firemox.clickable.ability.TriggeredAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public enum TestOn implements Serializable {

	/**
	 * The card to use for the test with 'attachedto' is the card the card owning
	 * the test is attached to.
	 */
	ATTACHED_TO("attachedto"),

	/**
	 * The controller of 'attachedto' to.
	 */
	ATTACHED_TO_CONTROLLER("attachedto.controller"),

	/**
	 * The owner of 'attachedto' to.
	 */
	ATTACHED_TO_OWNER("attachedto.owner"),

	/**
	 * The card to use for the test with 'tested.attachedto' is the card the
	 * 'tested' card is attached to.
	 */
	ATTACHED_TO_OF_TESTED("tested.attachedto"),

	/**
	 * Player saved in the current context.
	 */
	CONTEXT_PLAYER("context.player"),

	/**
	 * Player with id saved in the current context.
	 */
	CONTEXT_INT("context.int"),

	/**
	 * Player with id saved in the current context(2).
	 */
	CONTEXT_INT2("context.int2"),

	/**
	 * The card to use for the test with 'context.target' is the first target
	 * saved in the context.
	 */
	CONTEXT_TARGETABLE("context.target"),

	/**
	 * The card to use for the test with 'context.card' is the first card saved in
	 * the context.
	 */
	CONTEXT_CARD1("context.card"),

	/**
	 * The card to use for the test with 'context.card2' is the second card saved
	 * in the context.
	 */
	CONTEXT_CARD2("context.card2"),

	/**
	 * The controller of the second card saved in the context.
	 */
	CONTEXT_CARD2_CONTROLLER("context.card2.controller"),

	/**
	 * The owner of the second card saved in the context.
	 */
	CONTEXT_CARD2_OWNER("context.card2.owner"),

	/**
	 * The controller of the first card saved in the context.
	 */
	CONTEXT_CARD1_CONTROLLER("context.card.controller"),

	/**
	 * The owner of the first card saved in the context.
	 */
	CONTEXT_CARD1_OWNER("context.card.owner"),

	/**
	 * The Targetable cast to MCard object of this context without considering
	 * it's timstamp.
	 */
	CONTEXT_CARD_SHARE("context.card-share"),

	/**
	 * The controller.
	 */
	CONTROLLER("controller"),

	/**
	 * The creator of the current card.
	 */
	CREATOR("creator"),

	/**
	 * The active card in the stack owning the current spell or ability. This
	 * should be used within a pre-condition of event.
	 */
	CURRENT_CARD("currentcard"),

	/**
	 * Current player
	 */
	CURRENT_PLAYER("currentplayer"),

	/**
	 * Current player's opponent
	 */
	CURRENT_PLAYER_OPPONENT("currentplayer.opponent"),

	/**
	 * The card to use for the test with 'context.event-source' is the source of
	 * current event.
	 */
	EVENT_SOURCE("context.event-source"),

	/**
	 * The card to use for the test with 'target-list.first' is the first card of
	 * the target list.
	 */
	FIRST_TARGET("target-list.first"),

	/**
	 * The card to use for the test with 'target-list.first.attachedto' is the
	 * first card of the target list.
	 */
	FIRST_TARGET_ATTACHED_TO("target-list.first.attachedto"),

	/**
	 * The card to use for the test with 'target-list.first.attachedto.owner' is
	 * the first card of the target list.
	 */
	FIRST_TARGET_ATTACHED_TO_OWNER("target-list.first.attachedto.owner"),

	/**
	 * The card to use for the test with 'target-list.first.attachedto.controller'
	 * is the first card of the target list.
	 */
	FIRST_TARGET_ATTACHED_TO_CONTROLLER("target-list.first.attachedto.controller"),

	/**
	 * The card to use for the test with 'target-list.first.controller' is the
	 * first card of the target list.
	 */
	FIRST_TARGET_CONTROLLER("target-list.first.controller"),

	/**
	 * The card to use for the test with 'target-list.first.owner' is the first
	 * card of the target list.
	 */
	FIRST_TARGET_OWNER("target-list.first.owner"),

	/**
	 * The card to use for the test with 'target-list.last' is the last card of
	 * the target list.
	 */
	LAST_TARGET("target-list.last"),

	/**
	 * The card to use for the test with 'target-list.last.attachedto' is the last
	 * card of the target list.
	 */
	LAST_TARGET_ATTACHED_TO("target-list.last.attachedto"),

	/**
	 * The card to use for the test with 'target-list.last.attachedto.owner' is
	 * the last card of the target list.
	 */
	LAST_TARGET_ATTACHED_TO_OWNER("target-list.last.attachedto.owner"),

	/**
	 * The card to use for the test with 'target-list.last.attachedto.controller'
	 * is the last card of the target list.
	 */
	LAST_TARGET_ATTACHED_TO_CONTROLLER("target-list.last.attachedto.controller"),

	/**
	 * The card to use for the test with 'target-list.last.controller' is the last
	 * card of the target list.
	 */
	LAST_TARGET_CONTROLLER("target-list.last.controller"),

	/**
	 * The card to use for the test with 'target-list.last.owner' is the last card
	 * of the target list.
	 */
	LAST_TARGET_OWNER("target-list.last.owner"),

	/**
	 * The modifier creator card (may be different from THIS).
	 */
	MODIFIER_CREATOR("modifier.creator"),

	/**
	 * The modifier creator card (may be different from CONTROLLER).
	 */
	MODIFIER_CREATOR_CONTROLLER("modifier.creator.controller"),

	/**
	 * The modifier creator card (may be different from OWNER).
	 */
	MODIFIER_CREATOR_OWNER("modifier.creator.owner"),

	/**
	 * The opponent.
	 */
	OPPONENT("opponent"),

	/**
	 * The card's owner.
	 */
	OWNER("owner"),

	/**
	 * Saved component
	 */
	SAVED("saved"),

	/**
	 * Saved component(2)
	 */
	SAVED2("saved2"),

	/**
	 * Controller of saved component
	 */
	SAVED_CONTROLLER("saved.controller"),

	/**
	 * Owner of saved component
	 */
	SAVED_OWNER("saved.owner"),

	/**
	 * Controller of saved component(2)
	 */
	SAVED2_CONTROLLER("saved2.controller"),

	/**
	 * Owner of saved component(2)
	 */
	SAVED2_OWNER("saved2.controller"),

	/**
	 * The card to use for the test with 'super.tested' is like 'tested' it's the
	 * tested card outside the current 'counter' block.
	 */
	SUPER("super.tested"),

	/**
	 * The current card's controller. May be different from the controller of
	 * current ability.
	 */
	TARGET_CONTROLLER("target.controller"),

	/**
	 * The current card's owner. May be different from the owner of current
	 * ability.
	 */
	TARGET_OWNER("target.owner"),

	/**
	 * The card to use for the test with 'tested' is the current tested card. It's
	 * the tested card for counter, or the main card of an event.
	 */
	TESTED("tested"),

	/**
	 * The card to use for the test with 'me', 'myself' is the card owning the
	 * test.
	 */
	THIS("this"),

	/**
	 * The controller of current ability. May be a bit different from card's
	 * 'controller'
	 */
	YOU("you");

	private final String xsdName;

	private TestOn(String xsdName) {
		this.xsdName = xsdName;
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public Target getTargetable(Ability ability, Target tested) {
		return getTargetable(ability, null, tested);
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public Player getPlayer(Ability ability, Target tested) {
		return (Player) getTargetable(ability, null, tested);
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the current context.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public Target getTargetable(Ability ability, ContextEventListener context,
			Target tested) {
		if (ability == null)
			return getTargetable(null, SystemCard.instance, context, tested);
		return getTargetable(ability, ability.getCard(), context, tested);
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the current context.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public Player getPlayer(Ability ability, ContextEventListener context,
			Target tested) {
		return (Player) getTargetable(ability, context, tested);
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param card
	 *          is the card owning the current ability.
	 * @param context
	 *          the current context.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public Target getTargetable(Ability ability, MCard card,
			ContextEventListener context, Target tested) {
		switch (this) {
		case ATTACHED_TO:
			if (card.getParent() instanceof MCard) {
				return (MCard) card.getParent();
			}
			return SystemCard.instance;
		case ATTACHED_TO_CONTROLLER:
			if (card.getParent() instanceof MCard) {
				return ((MCard) card.getParent()).controller;
			}
			return SystemCard.instance;
		case ATTACHED_TO_OWNER:
			if (card.getParent() instanceof MCard) {
				return ((MCard) card.getParent()).getOwner();
			}
			return SystemCard.instance;
		case ATTACHED_TO_OF_TESTED:
			if (tested instanceof MCard && tested.getParent() instanceof MCard) {
				return (MCard) tested.getParent();
			}
			return null;
		case CURRENT_PLAYER:
			return StackManager.currentPlayer();
		case CURRENT_PLAYER_OPPONENT:
			return StackManager.currentPlayer().getOpponent();
		case CURRENT_CARD:
			return StackManager.getInstance().getSourceCard();
		case CONTEXT_INT:
			return StackManager.PLAYERS[((MContextCardCardIntInt) getContext(context))
					.getValue()];
		case CONTEXT_INT2:
			return StackManager.PLAYERS[((MContextCardCardIntInt) getContext(context))
					.getValue2()];
		case CONTEXT_TARGETABLE:
		case CONTEXT_PLAYER:
		case CONTEXT_CARD1:
			return ((MContextTarget) getContext(context)).getTargetable();
		case CONTEXT_CARD2:
			return ((MContextCardCardIntInt) getContext(context)).getCard2();
		case CONTEXT_CARD1_CONTROLLER:
			return ((MContextTarget) getContext(context)).getCard().getController();
		case CONTEXT_CARD2_CONTROLLER:
			return ((MContextCardCardIntInt) getContext(context)).getCard2()
					.getController();
		case CONTEXT_CARD1_OWNER:
			return ((MContextTarget) getContext(context)).getCard().getOwner();
		case CONTEXT_CARD2_OWNER:
			return ((MContextCardCardIntInt) getContext(context)).getCard2()
					.getOwner();
		case CONTEXT_CARD_SHARE:
			return ((MContextTarget) getContext(context)).getOriginalCard();
		case EVENT_SOURCE:
			return getContext(context).getEventSource();
		case CREATOR:
			return card.getCreator();
		case FIRST_TARGET:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return StackManager.getInstance().getTargetedList().getFirst();
		case FIRST_TARGET_CONTROLLER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) StackManager.getInstance().getTargetedList().getFirst())
					.getController();
		case FIRST_TARGET_OWNER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) StackManager.getInstance().getTargetedList().getFirst())
					.getOwner();
		case FIRST_TARGET_ATTACHED_TO:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return (MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getFirst()).getParent();
		case FIRST_TARGET_ATTACHED_TO_CONTROLLER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getFirst()).getParent()).getController();
		case FIRST_TARGET_ATTACHED_TO_OWNER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getFirst()).getParent()).getOwner();
		case LAST_TARGET:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return StackManager.getInstance().getTargetedList().getLast();
		case LAST_TARGET_CONTROLLER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) StackManager.getInstance().getTargetedList().getLast())
					.getController();
		case LAST_TARGET_OWNER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) StackManager.getInstance().getTargetedList().getLast())
					.getOwner();
		case LAST_TARGET_ATTACHED_TO:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return (MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getLast()).getParent();
		case LAST_TARGET_ATTACHED_TO_CONTROLLER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getLast()).getParent()).getController();
		case LAST_TARGET_ATTACHED_TO_OWNER:
			if (StackManager.getInstance().getTargetedList().isEmpty()) {
				return null;
			}
			return ((MCard) ((MCard) StackManager.getInstance().getTargetedList()
					.getLast()).getParent()).getOwner();
		case MODIFIER_CREATOR:
			if (ability instanceof RemoveModifier) {
				return ((RemoveModifier) ability).modifier.getCard();
			}
			return ability.getCard();
		case MODIFIER_CREATOR_CONTROLLER:
			if (ability instanceof RemoveModifier) {
				return ((RemoveModifier) ability).modifier.getCard().getController();
			}
			return ability.getCard().getController();
		case MODIFIER_CREATOR_OWNER:
			if (ability instanceof RemoveModifier) {
				return ((RemoveModifier) ability).modifier.getCard().getOwner();
			}
			return ability.getCard().getOwner();
		case OWNER:
			return card.getOwner();
		case OPPONENT:
			return card.getController().getOpponent();
		case SAVED:
			return ((TriggeredAbility) ability).getDelayedCard().saved;
		case SAVED2:
			return ((TriggeredAbility) ability).getDelayedCard().saved2;
		case SAVED_CONTROLLER:
			return ((MCard) ((TriggeredAbility) ability).getDelayedCard().saved)
					.getController();
		case SAVED2_CONTROLLER:
			return ((MCard) ((TriggeredAbility) ability).getDelayedCard().saved2)
					.getController();
		case SAVED_OWNER:
			return ((MCard) ((TriggeredAbility) ability).getDelayedCard().saved)
					.getOwner();
		case SAVED2_OWNER:
			return ((MCard) ((TriggeredAbility) ability).getDelayedCard().saved2)
					.getOwner();
		case SUPER:
			if (net.sf.firemox.expression.Counter.superTested != null) {
				return net.sf.firemox.expression.Counter.superTested;
			}
			return tested;
		case TARGET_CONTROLLER:
			return card.getController();
		case TARGET_OWNER:
			return card.getOwner();
		case TESTED:
			if (tested == null) {
				throw new InternalError("The tested card is null");
			}
			return tested;
		case THIS:
			return card;
		case YOU:
			if (ability != null && !ability.isSystemAbility())
				return ability.getCard().getController();
			return card.getController();
		case CONTROLLER:
			return card.getController();
		default:
			return null;
		}
	}

	/**
	 * Return the given context if not null. Returns the context of current
	 * ability otherwise.
	 * 
	 * @param currentContext
	 *          the known context.
	 * @return the given context if not null. Returns the context of current
	 *         ability otherwise.
	 */
	private ContextEventListener getContext(ContextEventListener currentContext) {
		if (currentContext == null)
			return StackManager.getInstance().getAbilityContext();
		return currentContext;
	}

	/**
	 * Return the target instance cast in Card instance.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested target
	 * @return the card to use for the test
	 */
	public MCard getCard(Ability ability, Target tested) {
		return getCard(ability, null, tested);
	}

	/**
	 * Return the target instance cast in Card instance.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the current context.
	 * @param tested
	 *          the tested target
	 * @return the card to use for the test
	 */
	public MCard getCard(Ability ability, ContextEventListener context,
			Target tested) {
		return (MCard) getTargetable(ability, context, tested);
	}

	/**
	 * Is this always referring to a card.
	 * 
	 * @return true if this is always referring to a card.
	 */
	public boolean isCard() {
		switch (this) {
		case ATTACHED_TO:
		case ATTACHED_TO_OF_TESTED:
		case CURRENT_CARD:
		case CONTEXT_CARD1:
		case CONTEXT_CARD2:
		case CONTEXT_CARD_SHARE:
		case EVENT_SOURCE:
		case CREATOR:
		case FIRST_TARGET_ATTACHED_TO:
		case LAST_TARGET_ATTACHED_TO:
		case THIS:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Is this always referring to a player.
	 * 
	 * @return true if this is always referring to a player.
	 */
	public boolean isPlayer() {
		switch (this) {
		case FIRST_TARGET_CONTROLLER:
		case FIRST_TARGET_OWNER:
		case FIRST_TARGET_ATTACHED_TO_CONTROLLER:
		case FIRST_TARGET_ATTACHED_TO_OWNER:
		case LAST_TARGET_CONTROLLER:
		case LAST_TARGET_OWNER:
		case LAST_TARGET_ATTACHED_TO_CONTROLLER:
		case LAST_TARGET_ATTACHED_TO_OWNER:
		case ATTACHED_TO_CONTROLLER:
		case ATTACHED_TO_OWNER:
		case CURRENT_PLAYER:
		case CURRENT_PLAYER_OPPONENT:
		case CONTEXT_PLAYER:
		case CONTEXT_CARD1_CONTROLLER:
		case CONTEXT_CARD2_CONTROLLER:
		case CONTEXT_CARD1_OWNER:
		case CONTEXT_CARD2_OWNER:
		case OWNER:
		case OPPONENT:
		case SAVED_CONTROLLER:
		case SAVED2_CONTROLLER:
		case SAVED_OWNER:
		case SAVED2_OWNER:
		case TARGET_CONTROLLER:
		case TARGET_OWNER:
		case YOU:
		case CONTROLLER:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Return true if the associated value can be evaluated without ability
	 * context.
	 * 
	 * @return true if the associated value can be evaluated without ability
	 *         context.
	 */
	public boolean canBePreempted() {
		switch (this) {
		case FIRST_TARGET_ATTACHED_TO:
		case LAST_TARGET_ATTACHED_TO:
		case FIRST_TARGET:
		case FIRST_TARGET_CONTROLLER:
		case FIRST_TARGET_OWNER:
		case FIRST_TARGET_ATTACHED_TO_CONTROLLER:
		case FIRST_TARGET_ATTACHED_TO_OWNER:
		case LAST_TARGET:
		case LAST_TARGET_CONTROLLER:
		case LAST_TARGET_OWNER:
		case LAST_TARGET_ATTACHED_TO_CONTROLLER:
		case LAST_TARGET_ATTACHED_TO_OWNER:
			return false;
		default:
			return true;
		}
	}

	/**
	 * Return the value corresponding to the true register index exactly as it
	 * will be when the ability will be executed. return the real number of a
	 * specifid idNumber. Since this number may reference to a token, a code
	 * matching is ran to determine which is the associated value.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param index
	 *          is the number where the real value will be extracted
	 * @return the real number associated to the specified idNumber
	 */
	public int getPreemptedValue(Ability ability, int index) {
		if (canBePreempted()) {
			return getTargetable(ability, null).getValue(index);
		}
		return -1;
	}

	/**
	 * Write the enum corresponding to the given xsd name to the given output
	 * stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 * @param xsdName
	 *          the Xsd name of this TestOn.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void serialize(OutputStream out, String xsdName)
			throws IOException {
		final TestOn value = valueOfXsd(xsdName);
		if (value == null) {
			throw new IllegalArgumentException("Invalid xsd attribute name : "
					+ xsdName);
		}
		value.serialize(out);
	}

	/**
	 * Return null of enum value corresponding to the given Xsd name.
	 * 
	 * @param xsdName
	 *          the Xsd name of this TestOn.
	 * @return null of enum value corresponding to the given Xsd name.
	 */
	public static TestOn valueOfXsd(String xsdName) {
		for (TestOn value : values()) {
			if (value.xsdName.equals(xsdName)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Write this enum to the given output stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void serialize(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enum from the given stream.
	 * 
	 * @param input
	 *          the stream containing the enum to read.
	 * @return the enum from the given stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static TestOn deserialize(InputStream input) throws IOException {
		return values()[input.read()];
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
	 */
	public String toHtmlString(Ability ability, ContextEventListener context) {
		switch (this) {
		case THIS:
			return null;
		default:
			return toString().toLowerCase();
		}
	}

}