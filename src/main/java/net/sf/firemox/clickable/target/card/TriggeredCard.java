/*
 * MTriggeredCard.java
 * Created on 14 f�vr. 2004
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.TriggeredAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.RegisterIndirection;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.ActionManager;
import net.sf.firemox.stack.StackContext;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.stack.TargetHelper;
import net.sf.firemox.stack.TargetedList;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdAbilities;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.Replacement;
import net.sf.firemox.zone.TriggeredBuffer;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.86 Ability source is saved.
 */
public class TriggeredCard extends AbstractCard implements MouseListener,
		StackContext {

	/**
	 * @param triggeredAbility
	 *          the triggered ability associated to this card
	 * @param context
	 *          the context of the associated triggered ability
	 * @param abilityID
	 *          is the ability's Id making this triggered ability to be created.
	 */
	public TriggeredCard(Ability triggeredAbility, ContextEventListener context,
			long abilityID) {
		super(triggeredAbility.getCard().database);
		this.triggeredAbility = triggeredAbility;
		this.context = context;
		this.abilityID = abilityID;
		setSize(new Dimension(CardFactory.cardWidth, CardFactory.cardHeight));
		setPreferredSize(getSize());
		addMouseListener(this);
		controller = triggeredAbility.getCard().getController();
		this.setVisibility(Visibility.PUBLIC);
	}

	/**
	 * Return the target option of the current spell. this target option is owned
	 * by the current spell. May be reseted, changed by the spell itself.
	 * 
	 * @return the targeted list of this context.
	 */
	public TargetedList getTargetedList() {
		return null;
	}

	/**
	 * Return the current context. Null if current ability is not a triggered one.
	 * 
	 * @return the current context. Null if current ability is not a triggered
	 *         one.
	 */
	public ContextEventListener getAbilityContext() {
		return context;
	}

	/**
	 * Return the action manager of this context.
	 * 
	 * @return the action manager of this context.
	 */
	public ActionManager getActionManager() {
		return null;
	}

	/**
	 * Return the card source of the current capcity/spell in the stack
	 * 
	 * @return the card source of the current capcity/spell in the stack
	 */
	public MCard getSourceCard() {
		return triggeredAbility.getCard();
	}

	/**
	 * Play this card as a spell.
	 * 
	 * @return true if this card has been completky played and if the stack can be
	 *         resolved after this call.
	 */
	public boolean newSpell() {
		return StackManager.newSpell(this);
	}

	@Override
	public boolean isACopy() {
		return false;
	}

	@Override
	public int countAllCardsOf(Test test, Ability ability, boolean canBePreempted) {
		if (triggeredAbility.isSystemAbility())
			return 0;
		if (canBePreempted)
			return test.test(ability, this) ? 1 : 0;
		return test.testPreemption(ability, this) ? 1 : 0;
	}

	@Override
	public void checkAllCardsOf(Test test, List<Target> list, Ability ability) {
		if (!triggeredAbility.isSystemAbility()
				&& test.test(ability, this)) {
			list.add(this);
		}
	}

	@Override
	public final boolean isAbility(int abilityType) {
		if (abilityType == IdAbilities.TRIGGERED_ABILITY
				|| abilityType == IdAbilities.ANY)
			return true;
		return false;
	}

	@Override
	public final boolean isSpell() {
		return false;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// draw the card picture
		g2D.drawImage(scaledImage(), null, null);

		if (getParent() == ZoneManager.stack) {
			// is in the stack
			g2D.setColor(Color.magenta);
		} else {
			/*
			 * draw the highlighted rectangle arround the card if not returned, or if
			 * we are in target mode.
			 */
			// draw the rounded black rectangle
			if (isHighLighted) {
				g2D.setColor(highLightColor);
			} else {
				g2D.setColor(Color.BLACK);
			}
		}
		// draw the rounded black rectangle
		g2D.draw3DRect(0, 0, CardFactory.cardWidth - 2, CardFactory.cardHeight - 2,
				false);

		g2D.dispose();
	}

	@Override
	public Image image() {
		if (cachedImage == null) {
			if (triggeredAbility.getPictureName() == null) {
				cachedImage = super.image();
			} else {
				try {
					cachedImage = Picture.loadImage(MToolKit
							.getTbsPicture(triggeredAbility.getPictureName() + ".jpg"));
				} catch (MalformedURLException e) {
					// IGNORING
				}
			}
		}
		return cachedImage;
	}

	@Override
	public Image scaledImage() {
		if (cachedScaledImage == null) {
			cachedScaledImage = Picture.getScaledImage(image());
		}
		return cachedScaledImage;
	}

	/**
	 * The cached image. Is <code>null</code> while the associated image of this
	 * ability is not loaded.
	 */
	private Image cachedImage;

	/**
	 * The scaled cached image. Is <code>null</code> while the associated image
	 * of this ability is not loaded.
	 */
	private Image cachedScaledImage;

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Replacement.isRunning) {
			Log
					.debug("Replacement : considere the mouse click as replacement choice.");
			return;
		}
		StackManager.noReplayToken.take();
		try {
			if (triggeredAbility.isHidden()) {
				throw new InternalError(
						"hidden triggered abilities should not be visible in TBZ");
			}
			Log.debug("mouseClicked triggered ability");
			// only if left button is pressed
			if (ConnectionManager.isConnected()
					&& e.getButton() == MouseEvent.BUTTON1
					&& StackManager.idHandedPlayer == 0
					&& StackManager.actionManager.clickOn(this)) {
				// card has been accepted, we can play it
				sendClickToOpponent();
				StackManager.actionManager.succeedClickOn(this);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	@Override
	public void sendClickToOpponent() {
		// get position of this card
		TriggeredBuffer cont = StackManager.PLAYERS[0].zoneManager.triggeredBuffer;
		// get index of this card within it's container
		int index = -1;
		for (index = cont.getCardCount(); index-- > 0;) {
			if (cont.getTriggeredAbility(index) == this) {
				// send position of this card
				ConnectionManager.send(CoreMessageType.CLICK_TRIGGERED_CARD,
						(byte) index);
				return;
			}
		}
		Log.fatal("Could not find the triggered card to send");
	}

	/**
	 * This method is invoked when opponent has clicked on this object.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public static void clickOn(byte[] data) {
		// waiting for triggered card information
		Log.debug("clickedOn triggeredcard throw input");
		TriggeredCard triggered = getTriggeredCard(data);
		StackManager.actionManager.clickOn(triggered);
		StackManager.actionManager.succeedClickOn(triggered);
	}

	/**
	 * Return the component from information read from opponent.
	 * 
	 * @param data
	 *          data sent by opponent.
	 * @return the triggered card read from the input stream
	 */
	public static TriggeredCard getTriggeredCard(byte[] data) {
		// waiting for card information
		return StackManager.PLAYERS[1].zoneManager.triggeredBuffer
				.getTriggeredAbility(data[0]);
	}

	@Override
	public void moveCard(int newIdPlace, Player newController,
			boolean newIsTapped, int idPosition) {
		// DEBUG
		if (newIdPlace != IdZones.STACK) {
			throw new InternalError(
					"A wrong destination place has been specified for a triggered:"
							+ newIdPlace);
		}
		StackManager.PLAYERS[newController.idPlayer].zoneManager.triggeredBuffer
				.removeTriggered(this);
		reverse(false);
		setSize(new Dimension(CardFactory.cardWidth, CardFactory.cardHeight));
		setPreferredSize(getSize());
		ZoneManager.stack.add(this, 0);
		// TODO updateTooltipText();
	}

	@Override
	public String getTooltipString() {
		StringBuilder toolTip = new StringBuilder(300);

		// html header and card name
		toolTip.append("<html><b>");
		toolTip.append(LanguageManager.getString("card.name"));
		toolTip.append(": </b>");
		if (isVisibleForYou()) {
			// the test added
			// for token cards
			toolTip.append("??");
		} else {
			toolTip.append(database.getLocalName());
			toolTip.append(CardFactory.ttSource);
			toolTip.append(triggeredAbility.getCard().toString());
			toolTip.append("<br><b>");
			toolTip.append(LanguageManager.getString("triggeredability"));
			toolTip.append(": </b>");
			toolTip.append(triggeredAbility.toHtmlString(context));

			// credits
			if (database.getRulesCredit() != null) {
				toolTip.append(CardFactory.ttRulesAuthor);
				toolTip.append(database.getRulesCredit());
			}
		}
		toolTip.append("</html>");
		return toolTip.toString();
	}

	@Override
	public final void mouseEntered(MouseEvent e) {
		CardFactory.previewCard.setImage(image(), database.getLocalName());
		setToolTipText(getTooltipString());
		if (getParent() == ZoneManager.stack) {
			// This spell is in the stack
			TargetHelper.getInstance().addTargetedBy(StackManager.getContextOf(this));
		} else {
			// This spell is not yet in the stack, but in the TBZ
			TargetHelper.getInstance().addTargetedBy(this);
		}
	}

	@Override
	public String toString() {
		return ""
				+ triggeredAbility
				+ ", card="
				+ triggeredAbility.getCard()
				+ (triggeredAbility.getCard() != null
						&& triggeredAbility.getCard() != SystemCard.instance ? "@"
						+ Integer.toHexString(triggeredAbility.getCard().hashCode()) : "")
				+ (triggeredAbility.isHidden() ? ", hidden=true" : "");
	}

	@Override
	public int getValue(int index) {
		throw new InternalError("Triggered Card have no registers");
	}

	@Override
	public void removeModifier(RegisterModifier modifier, int index) {
		throw new InternalError("Should not be called");
	}

	@Override
	public void removeModifier(RegisterIndirection indirection, int index) {
		throw new InternalError("Should not be called");
	}

	/**
	 * The border will be highligthed to a color identifying it easily as a token
	 * component.
	 * 
	 * @see #STACKABLE_COLOR
	 */
	public void highlightStackable() {
		highLight(STACKABLE_COLOR);
	}

	@Override
	public Target getLastKnownTargetable(int timeStamp) {
		throw new InternalError("Should not be called");
	}

	@Override
	public Target getOriginalTargetable() {
		throw new InternalError("Should not be called");
	}

	@Override
	public void addTimestampReference() {
		throw new InternalError("Should not be called");
	}

	@Override
	public int getTimestamp() {
		throw new InternalError("Should not be called");
	}

	@Override
	public void decrementTimestampReference(int timestamp) {
		throw new InternalError("Should not be called");
	}

	public void abortion(AbstractCard card, Ability source) {
		throw new InternalError("Should not be called");
	}

	public Ability getAbortingAbility() {
		throw new InternalError("Should not be called");
	}

	/**
	 * Return the delayed card attached to this ability.
	 * 
	 * @return the delayed card attached to this ability.
	 */
	public DelayedCard getDelayedCard() {
		if (triggeredAbility instanceof TriggeredAbility)
			return ((TriggeredAbility) triggeredAbility).getDelayedCard();
		return null;
	}

	/**
	 * Triggered ability
	 */
	public Ability triggeredAbility;

	/**
	 * Is the ability making this triggered ability to be created.
	 */
	public long abilityID;

	/**
	 * Context of triggered ability
	 */
	protected ContextEventListener context;

	/**
	 * Height of original card to display
	 */
	public static int cardHeight;

	/**
	 * Width of original card to display
	 */
	public static int cardWidth;

	/**
	 * The color used to color the stackable component
	 */
	public static final Color STACKABLE_COLOR = Color.RED;

}