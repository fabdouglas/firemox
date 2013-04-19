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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.CanICast;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * An ability that can be manually played.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class ActivatedAbility extends UserAbility {

	/**
	 * Creates a new instance of ActivatedAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
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
	public ActivatedAbility(InputStream input, MCard card) throws IOException {
		super(input, card);
		updateManaAbilityTag();
	}

	@Override
	public Ability clone(MCard container) {
		return new ActivatedAbility(this, container);
	}

	/**
	 * Create a fresh instance from another instance of ActivatedAbility
	 * 
	 * @param other
	 *          the instance to clone.
	 * @param card
	 *          referenced card owning the new copy.
	 */
	protected ActivatedAbility(ActivatedAbility other, MCard card) {
		super(other.getName(), other.actionList, other.effectList, other.optimizer,
				other.priority, other.eventComing.clone(card), other.pictureName);
		this.playAsSpell = other.playAsSpell;
		updateManaAbilityTag();
	}

	/**
	 * Iterate on actions of effect part looking for a 'give mana' action. If one
	 * or several are found, the <code>idCard</code> of canIcast event will be
	 * replaced by <code>IdTokens.MANA_ABILITY</code>
	 */
	private void updateManaAbilityTag() {
		if (eventComing instanceof CanICast) {
			((CanICast) eventComing).updateManaAbilityTag(effectList);
		}
	}

	@Override
	public void resolveStack() {
		if (priority.isAutoResolve()) {
			if (StackManager.actionManager.advancedMode) {
				/*
				 * we are in the middle of cost part, and the activated previous ability
				 * has been inserted. Instead of resolving the stack, we replay the
				 * current action which must be a Waiting and ChosenAction action.
				 */
				StackManager.actionManager.reactivate();
			} else {
				/*
				 * this ability has been played entirely, and removed from stack.
				 * Instead of calling the normal 'resolveStack' method as normal, we
				 * give to the same active player the priority to play another
				 * auto-resolving ability or a 'normal' one
				 */
				StackManager.activePlayer().waitTriggeredBufferChoice(true);
			}
		} else {
			super.resolveStack();
		}
	}

	@Override
	public String getLog(ContextEventListener context) {
		return toHtmlString(context) + "' of " + getCard();
	}

	@Override
	public String getAbilityTitle() {
		return LanguageManager.getString("activatedability")
				+ (getName() != null ? ": " + getName() : "") + super.getAbilityTitle();
	}

}