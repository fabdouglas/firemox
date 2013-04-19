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
package net.sf.firemox.zone;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54.17
 * @since 0.60.20 this zone contains now a special abstract place where abstract
 *        triggered abilities are place when they are awakened. Abilities placed
 *        into this zone are resolved prior the normal ones
 */
public class TriggeredBuffer extends MZone {

	/**
	 * The zone name.
	 */
	public static final String ZONE_NAME = "tbz";

	/**
	 * create a new instance of TriggeredBuffer
	 * 
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param you
	 *          is this zone is controlled by you.
	 * @since 0.3 feature "reverseImage" implemented
	 * @since 0.80 feature "reverseImage" removed, since this panel has been moved
	 *        in a JTabbedPanel with the stack
	 * @since 0.80 Support the 'highlight' feature
	 * @see IdZones
	 */
	TriggeredBuffer(JScrollPane superPanel, boolean you) {
		super(IdZones.TRIGGERED, new FlowLayout(FlowLayout.CENTER, 2, 2),
				superPanel, !you, ZONE_NAME);
		this.you = you;
		this.reverseImage = false;
	}

	@Override
	public void initUI() {
		super.initUI();
		addHierarchyBoundsListener(new HierarchyBoundsListener() {

			public void ancestorMoved(HierarchyEvent evt) {
				// Ignore this event
			}

			public void ancestorResized(HierarchyEvent evt) {
				updatePanel();
			}
		});
	}

	/**
	 * Return the first ability with the 'high-priority' tag, or the triggered
	 * ability without this tag, or null if there are several triggered ability
	 * with the same priority and if the 'auto-stack' option is disabled.The
	 * 'auto-stack' order is FIFO.
	 * 
	 * @return the first ability with the 'high-priority' tag. Return null value
	 *         if none was found.
	 * @since 0.71 option 'auto-stack' has been added. In case of several
	 *        triggered cards have the same priority, the play choose the first
	 *        one and stack it automatically.
	 */
	public TriggeredCard chooseAbility() {
		Component[] cps = this.getComponents();
		for (int i = 0; i < cps.length; i++) {
			TriggeredCard triggered = (TriggeredCard) cps[i];
			Ability ability = triggered.triggeredAbility;
			int oldActivePlayer = StackManager.idActivePlayer;
			StackManager.idActivePlayer = triggered.controller.idPlayer;
			if (ability.hasHighPriority()) {
				if (ability.eventComing().reCheck(triggered)) {
					StackManager.idActivePlayer = oldActivePlayer;
					return triggered;
				}
				StackManager.idActivePlayer = oldActivePlayer;
				removeTriggered(triggered);
				return chooseAbility();
			}
			if (!ability.eventComing().reCheck(triggered)) {
				removeTriggered(triggered);
			}
			StackManager.idActivePlayer = oldActivePlayer;
		}
		if (this.getComponentCount() == 1) {
			// only one triggered ability to choose.
			return (TriggeredCard) getComponent(0);
		}
		return null;
	}

	/**
	 * Removes the specified component from this container. This method also
	 * notifies the layout manager to remove the component from this container's
	 * layout via the <code>removeLayoutComponent</code> method.
	 * 
	 * @param triggered
	 *          the component to be removed
	 */
	public void removeTriggered(TriggeredCard triggered) {
		super.remove(triggered);
	}

	/**
	 * Removes the specified component from this container. This method also
	 * notifies the layout manager to remove the component from this container's
	 * layout via the <code>removeLayoutComponent</code> method.
	 * 
	 * @param comp
	 *          the component to be removed
	 * @deprecated use 'removeTriggered(MTriggeredCard)' instead
	 */
	@Override
	@Deprecated
	public void remove(Component comp) {
		throw new InternalError(
				"@deprecated, use 'removeTriggered(MTriggeredCard)' instead.");
	}

	/**
	 * update this hand
	 */
	@Override
	public void updatePanel() {
		superPanel.setViewportView(this);
		setPreferredSize(getLayout().preferredLayoutSize(this));
	}

	/**
	 * Add a card to this zone.
	 * 
	 * @param card
	 *          the card to add
	 */
	@Override
	public void addTop(MCard card) {
		card.reverseAsNeeded();
		super.addBottom(card);
	}

	/**
	 * Add the given ability to the special zone 'abstract triggered'. This zone
	 * is resolved prior to the standard abilities
	 * 
	 * @param ability
	 *          the ability (should be an abstract one) toe the special zone
	 *          'abstract triggered'
	 * @param context
	 *          the associated context to this ability. This context would be
	 *          restored when this ability would be played. Commonly this context
	 *          contains information that was looked for by the event of this
	 *          ability.
	 */
	public void addHidden(Ability ability, ContextEventListener context) {
		if (!ability.isHidden()) {
			Log.error("This ability has not the hidden tag as normal, "
					+ "resolve will be forced");
		}
		if (ability.optimizer
				.addTo(ability, context, ability.priority.getAbstractZone(
						abstractLowestZone, abstractZone, abstractHighestZone))) {
			Log.debug("added to TBZ : " + ability
					+ MToolKit.getLogCardInfo(ability.getCard()));
		}
	}

	@Override
	public void add(Component comp, Object constraints) {
		throw new InternalError("should not be called");
	}

	/**
	 * Add the triggered ability to this zone and update it.
	 * 
	 * @param triggeredCard
	 *          the triggered card to this zone
	 */
	public void add(TriggeredCard triggeredCard) {
		Log.debug("added to TBZ : added untagged triggered ability "
				+ triggeredCard.triggeredAbility.getName() + ", Tcard="
				+ triggeredCard.getCardName()
				+ MToolKit.getLogCardInfo(triggeredCard.triggeredAbility.getCard()));
		if (triggeredCard.triggeredAbility.isHidden()) {
			throw new InternalError("ability shouldn't be tagged as hidden");
		}
		super.add(triggeredCard);
		updatePanel();
	}

	/**
	 * Return the triggered abilities positioned at the specified index
	 * 
	 * @param index
	 *          is the index where is the element
	 * @return the triggered abilities at the specified index
	 */
	public TriggeredCard getTriggeredAbility(int index) {
		return (TriggeredCard) getComponent(index);
	}

	/**
	 * Add to the stack the first abstract triggered abilities present into the
	 * abstract place, and resolve it since this one should have the 'hidden' tag.
	 * The returned value is true if the abstract zone contained at least one
	 * triggered ability.
	 * 
	 * @param tmpIdPlayer
	 *          the player that would get virtually priority.
	 * @return true if one of the abstract places contained at least a triggered
	 *         ability. Return false if no hidden ability was found.
	 */
	public boolean resolveHiddenHighLevel(int tmpIdPlayer) {
		// the TBZ contains abstract abilities, first play the highest priority
		while (!abstractHighestZone.isEmpty()) {
			final TriggeredCard triggered = abstractHighestZone.remove(0);
			final int oldActivePlayer = StackManager.idActivePlayer;
			StackManager.idActivePlayer = triggered.controller.idPlayer;
			if (triggered.triggeredAbility.eventComing().reCheck(triggered)) {
				StackManager.idHandedPlayer = tmpIdPlayer;
				StackManager.actionManager.succeedClickOn(triggered);
				return true;
			}
			/*
			 * The triggered does not verify the second test, we release the reference
			 * to card timestamp locked by context of this triggered.
			 */
			triggered.getAbilityContext().removeTimestamp();
			StackManager.idActivePlayer = oldActivePlayer;
		}
		return false;
	}

	/**
	 * Add to the stack the first abstract triggered abilities present into the
	 * abstract place, and resolve it since this one should have the 'hidden' tag.
	 * The returned value is true if the abstract zone contained at least one
	 * triggered ability.
	 * 
	 * @param tmpIdPlayer
	 *          the player that would get virtually priority.
	 * @return true if one of the abstract places contained at least a triggered
	 *         ability. Return false if no hidden ability was found.
	 */
	public boolean resolveHiddenNormalLevel(int tmpIdPlayer) {
		// the TBZ contains abstract abilities, first play the highest priority
		while (!abstractZone.isEmpty()) {
			final TriggeredCard triggered = abstractZone.remove(0);
			final int oldActivePlayer = StackManager.idActivePlayer;
			StackManager.idActivePlayer = triggered.controller.idPlayer;
			if (triggered.triggeredAbility.eventComing().reCheck(triggered)) {
				StackManager.idActivePlayer = oldActivePlayer;
				StackManager.idHandedPlayer = tmpIdPlayer;
				StackManager.actionManager.succeedClickOn(triggered);
				return true;
			}
			/*
			 * The triggered does not verify the second test, we release the reference
			 * to card timestamp locked by context of this triggered.
			 */
			triggered.getAbilityContext().removeTimestamp();
			StackManager.idActivePlayer = oldActivePlayer;
		}
		return false;
	}

	/**
	 * Add to the stack the first abstract triggered abilities present into the
	 * abstract place, and resolve it since this one should have the 'hidden' tag.
	 * The returned value is true if the abstract zone contained at least one
	 * triggered ability.
	 * 
	 * @param tmpIdPlayer
	 *          the player that would get virtually priority.
	 * @return true if one of the abstract places contained at least a triggered
	 *         ability. Return false if no hidden ability was found.
	 */
	public boolean resolveHiddenLowestLevel(int tmpIdPlayer) {
		// the TBZ contains abstract abilities, first play the highest priority
		while (!abstractLowestZone.isEmpty()) {
			final TriggeredCard triggered = abstractLowestZone.remove(0);
			final int oldActivePlayer = StackManager.idActivePlayer;
			StackManager.idActivePlayer = triggered.controller.idPlayer;
			if (triggered.triggeredAbility.eventComing().reCheck(triggered)) {
				StackManager.idHandedPlayer = tmpIdPlayer;
				StackManager.actionManager.succeedClickOn(triggered);
				return true;
			}
			/*
			 * The triggered does not verify the second test, we release the reference
			 * to card timestamp locked by context of this triggered.
			 */
			triggered.getAbilityContext().removeTimestamp();
			StackManager.idActivePlayer = oldActivePlayer;
		}
		return false;
	}

	/**
	 * Highlight cards of this container with the STACKABLE color.
	 * 
	 * @see net.sf.firemox.clickable.target.card.TriggeredCard#highlightStackable()
	 */
	public void highlightStackable() {
		for (int i = getComponentCount(); i-- > 0;) {
			((TriggeredCard) getComponent(i)).highlightStackable();
		}
		highLight(TriggeredCard.STACKABLE_COLOR);
	}

	@Override
	public int getControllerIdPlayer() {
		return you ? 0 : 1;
	}

	@Override
	public boolean isMustBePaintedReversed(MCard card) {
		return false;
	}

	/**
	 * Represents the abstract zone where abstract abilities are placed
	 */
	private List<TriggeredCard> abstractZone = new ArrayList<TriggeredCard>();

	/**
	 * Represents the abstract zone where abstract abilities with the lowest
	 * priority are placed
	 */
	private List<TriggeredCard> abstractLowestZone = new ArrayList<TriggeredCard>();

	/**
	 * Represents the abstract zone where abstract abilities with the highest
	 * priority are placed
	 */
	private List<TriggeredCard> abstractHighestZone = new ArrayList<TriggeredCard>();

	/**
	 * is this zone is controlled by you.
	 */
	private boolean you;
}