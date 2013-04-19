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

import net.sf.firemox.clickable.target.card.DelayedCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80 When this ability is unregistered, the corresponding delayed
 *        card, if specified is also removed from the DBZ.
 * @since 0.82 Triggered abilities are registered avoiding multiple instance of
 *        same effects. By default, triggered ability is always registered, but
 *        refresh and remover modifier are added as needed.
 * @since 0.86 mana check is not done in the "isMathing" method since many manas
 *        can be played during the ability cost phase.
 */
public class TriggeredAbility extends UserAbility {

	/**
	 * Creates a new instance of TriggeredAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [ActivatedAbility]</li>
	 * </ul>
	 * 
	 * @param input
	 *          stream containing this ability
	 * @param card
	 *          referenced card owning this ability.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected TriggeredAbility(InputStream input, MCard card) throws IOException {
		super(input, card);
	}

	/**
	 * Create a fresh instance from another instance of TriggeredAbility
	 * 
	 * @param other
	 *          the instance to clone.
	 * @param event
	 *          The attached activation event.
	 */
	protected TriggeredAbility(TriggeredAbility other, MEventListener event) {
		super(other.getName(), other.actionList, other.effectList, other.optimizer,
				other.priority, event, other.pictureName);
		this.playAsSpell = other.playAsSpell;
	}

	@Override
	public Ability clone(MCard container) {
		return new TriggeredAbility(this, eventComing.clone(container));
	}

	/**
	 * @param delayedCard
	 */
	public void setDelayedCard(DelayedCard delayedCard) {
		this.delayedCard = delayedCard;
	}

	@Override
	public void removeFromManager() {
		super.removeFromManager();
		if (delayedCard != null) {
			// remove the delayed card from the DBZ, once.
			StackManager.getSpellController().zoneManager.delayedBuffer
					.remove(delayedCard);
			// and remove the linked 'until' abilities to free the useless listeners
			delayedCard.removeFromManager();
			delayedCard = null;
		}
	}

	@Override
	public boolean isMatching() {
		// check only the target actions of 'cost' part can be played
		return checkTargetActions() && checkObjectActions();
	}

	@Override
	public final String toHtmlString(ContextEventListener context) {
		return new StringBuilder(eventComing.toHtmlString(this, context)).append(
				"<br>").append(super.toHtmlString(context)).toString();
	}

	@Override
	public String getLog(ContextEventListener context) {
		return "Triggered '" + toHtmlString(context) + "' of " + getCard();
	}

	@Override
	public String getAbilityTitle() {
		return LanguageManager.getString("triggeredability")
				+ (getName() != null ? " : &nbsp;" + getName() : "")
				+ super.getAbilityTitle();
	}

	/**
	 * Return true if this ability is a delayed one.
	 * 
	 * @return true if this ability is a delayed one.
	 */
	public boolean isDelayedAbility() {
		return delayedCard != null;
	}

	/**
	 * Return the delayed card. May be null.
	 * 
	 * @return the delayed card. May be null.
	 */
	public DelayedCard getDelayedCard() {
		return delayedCard;
	}

	/**
	 * The delayed card corresponding to this ability. If is not null, when this
	 * ability is unregistered, the delayed card is removed from the DBZ
	 */
	protected DelayedCard delayedCard = null;

}