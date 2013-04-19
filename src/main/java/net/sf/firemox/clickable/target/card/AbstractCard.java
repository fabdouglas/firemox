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
package net.sf.firemox.clickable.target.card;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.management.MonitorListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.stack.TargetHelper;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.token.VisibilityChange;
import net.sf.firemox.token.Visible;
import net.sf.firemox.ui.Reversable;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.1
 */
public abstract class AbstractCard extends Target implements MonitorListener,
		Visible {

	/**
	 * Creates a new instance of AbstractCard <br>
	 */
	protected AbstractCard() {
		super();
	}

	/**
	 * Creates a new instance of AbstractCard <br>
	 * 
	 * @param databaseCard
	 */
	protected AbstractCard(DatabaseCard databaseCard) {
		this();
		this.database = databaseCard;
	}

	/**
	 * return true if this target is a card
	 * 
	 * @return true if this target is a card
	 */
	@Override
	public boolean isCard() {
		return true;
	}

	/**
	 * Checks all cards corresponding to the specified constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param canBePreempted
	 *          <code>true</code> if the valid targets can be determined before
	 *          runtime.
	 * @return amount of card matching with the specified test
	 */
	public abstract int countAllCardsOf(Test test, Ability ability,
			boolean canBePreempted);

	/**
	 * Checks all cards corresponding to this constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param list
	 *          the list containing the founded cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 */
	public abstract void checkAllCardsOf(Test test, List<Target> list,
			Ability ability);

	/**
	 * Move this card to a new place tapped or not under the control of a player
	 * 
	 * @param newIdPlace
	 *          the new place for this card
	 * @param newController
	 *          new controller of this card. If null, the controller is the owner.
	 * @param newIsTapped
	 *          determines if this card will come tapped or not
	 * @param idPosition
	 *          from IdPositions
	 */
	public void moveCard(int newIdPlace, Player newController,
			boolean newIsTapped, int idPosition) {
		throw new InternalError("shouldn't be called");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		TargetHelper.getInstance().removeTargetedBy();
	}

	/**
	 * Return HTML tooltip string of this card.
	 * 
	 * @return HTML tooltip string of this card.
	 */
	public abstract String getTooltipString();

	/**
	 * Reverse this card if controller is not you
	 */
	public void reverseAsNeeded() {
		reverse(needReverse());
	}

	/**
	 * Is this card need to be reversed to suit to the player view.
	 * 
	 * @return true if this card need to be reversed to suit to the player view.
	 */
	public boolean needReverse() {
		if (getParent() != null && getParent() instanceof MCard) {
			return !StackManager.isYou(((MCard) getParent()).controller);
		}
		return !StackManager.isYou(controller);
	}

	/**
	 * Reverse this card if the specified parameter is true.
	 * 
	 * @param reversed
	 *          if true the card will be turned as if your opponent controls it.
	 */
	public void reverse(boolean reversed) {
		this.reversed = reversed;
		for (int i = getComponentCount(); i-- > 1;) {
			((Reversable) getComponent(i)).reverse(reversed);
		}
	}

	/**
	 * Return the card's picture
	 * 
	 * @return the card's picture
	 */
	public Image image() {
		return database.getImage(this);
	}

	/**
	 * Return the scaled card's picture
	 * 
	 * @return the scaled card's picture
	 */
	public Image scaledImage() {
		return database.getScaledImage(this);
	}

	public final void notifyChange() {
		ui.updateMUI();
		repaint();
	}

	/**
	 * Return the card's picture as it would be displayed in the preview panel.
	 * May be different from the displayed picture.
	 * 
	 * @return the card's picture as it would be displayed in the preview panel.
	 */
	public final Image getPreviewImage() {
		if (isVisibleForYou()) {
			return image();
		}
		return DatabaseFactory.backImage;
	}

	/**
	 * return the card's name
	 * 
	 * @return the card's name
	 */
	@Override
	public String toString() {
		return getCardName();
	}

	/**
	 * Indicates if the current card in the stack is a copy or not
	 * 
	 * @return true if the current card in the stack is a copy or not
	 */
	public abstract boolean isACopy();

	/**
	 * is called when mouse is on this card, will disp a preview
	 * 
	 * @param e
	 *          is the mouse event
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		StringBuilder toolTip = new StringBuilder(100);
		toolTip.append("<html><b>");
		toolTip.append(LanguageManager.getString("card.name"));
		toolTip.append(": </b>");
		toolTip.append(database.getLocalName());
		ui.setToolTipText(toolTip.toString());
	}

	/**
	 * Return the controller of this card
	 * 
	 * @return the controller of this card
	 */
	public Player getController() {
		return controller == null ? SystemCard.instance.getController()
				: controller;
	}

	/**
	 * This function return the result of Component#getName(). Return the english
	 * name, not the localized one.
	 * 
	 * @return the result of Component#getName()
	 */
	public final String getCardName() {
		return database.getCardName();
	}

	/**
	 * Return the database configuration of this card : picture, database
	 * properties (credits, language,...)
	 * 
	 * @return database configuration of this card
	 */
	public final DatabaseCard getDatabase() {
		return database;
	}

	/**
	 * Return UI of this card.
	 * 
	 * @return UI of this card.
	 */
	public VirtualCard getMUI() {
		return ui;
	}

	private void setVisibilityPriv(Visibility containerVisibility) {
		if (this.visibility != containerVisibility) {
			this.visibility = containerVisibility;
			repaint();
		}
	}

	public void setVisibility(Visibility containerVisibility) {
		this.previousVisibility = this.visibility;
		setVisibilityPriv(containerVisibility);
	}

	/**
	 * @param visibility
	 */
	public void updateZoneVisibility(Visibility visibility) {
		if (this.previousVisibility != null) {
			// Ignore this zone visibility change request
			this.previousVisibility = null;
		} else {
			// Update the zone visibility as normal
			setVisibilityPriv(visibility);
		}

	}

	public void restoreVisibility() {
		if (previousVisibility != null) {
			restoreVisibility(previousVisibility);
		}
	}

	public void restoreVisibility(Visibility visibility) {
		this.previousVisibility = null;
		setVisibilityPriv(visibility);
	}

	public boolean isVisibleForYou() {
		return visibility != null && visibility.isVisibleForYou(getController());
	}

	public boolean isVisibleForOpponent() {
		return visibility.isVisibleForOpponent(getController());
	}

	public void increaseFor(Player player, VisibilityChange visibilityChange) {
		setVisibility(visibility.increaseFor(player, visibilityChange));
	}

	public void decreaseFor(Player player, VisibilityChange visibilityChange) {
		setVisibility(visibility.decreaseFor(player, visibilityChange));
	}

	public Visibility getVisibility() {
		return visibility;
	}

	/**
	 * The database configuration of this card : picture, database properties
	 * (credits, language,...)
	 */
	protected DatabaseCard database;

	/**
	 * tell if this card is reversed or not, that means this card is controlled by
	 * opponent or not. A card in side, stack or under your control is not
	 * reversed.
	 */
	public boolean reversed;

	/**
	 * Indicates if this card is returned or not, that means back image is visible
	 * or not.
	 */
	private Visibility visibility;

	/**
	 * The previous visibility of this card.
	 */
	private Visibility previousVisibility;

	/**
	 * The UI of this card.
	 */
	protected VirtualCard ui;

	/**
	 * Player controller
	 */
	public Player controller;
}