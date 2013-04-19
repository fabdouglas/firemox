/*
 * Created on Sep 3, 2004 
 * Original filename was RemoveModifier.java
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
 */
package net.sf.firemox.clickable.ability;

import net.sf.firemox.action.MAction;
import net.sf.firemox.action.RemoveMe;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.Unregisterable;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class RemoveModifier extends AbstractAbility {

	/**
	 * Creates a new instance of RemoveModifier <br>
	 * 
	 * @param event
	 *          the event attached to this ability.
	 * @param modifier
	 */
	public RemoveModifier(TriggeredEvent event, Unregisterable modifier) {
		super("removemyself modifier", event, null, null, TrueFalseAuto.FALSE);
		actionList = new MAction[] {};
		effectList = new MAction[] { new RemoveMe(this) };
		this.modifier = modifier;

		// override the resolver
		priority = Priority.hidden_high;
		// override the optimizer
		optimizer = Optimization.first;
		unregisteringmodifier = false;
	}

	@Override
	public void removeFromManager() {
		if (unregisteringmodifier) {
			// we are already unregistering this modifier
			return;
		}
		unregisteringmodifier = true;
		super.removeFromManager();
		modifier.removeFromManager();
		unregisteringmodifier = false;
	}

	@Override
	public void optimizeRegisterToManager() {
		for (Ability ability : MEventListener.TRIGGRED_ABILITIES.get(eventComing
				.getIdEvent())) {
			if (equals(ability)) {
				/*
				 * this remove modifier is already existing. Instead of registering
				 * another time this modifier, we append with 'or' operator the
				 * conditionnal test
				 */
				MEventListener newEvent = ability.eventComing.appendOr(eventComing);
				if (newEvent != null) {
					ability.eventComing = newEvent;
					return;
				}
			}
		}
		// This modifier has not yet been registered
		MEventListener.TRIGGRED_ABILITIES.get(eventComing.getIdEvent()).add(this);
	}

	@Override
	public boolean equals(ContextEventListener thisContext, Ability ability,
			ContextEventListener context) {
		return ability instanceof RemoveModifier
				&& ((RemoveModifier) ability).modifier == modifier;
	}

	@Override
	public int hashCode() {
		if (eventComing != null && eventComing.card != null)
			return eventComing.card.hashCode();
		return super.hashCode();
	}

	@Override
	public MCard getCard() {
		return eventComing.card;
	}

	@Override
	public String toString() {
		return getName() + ", from card " + eventComing.card;
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		eventComing.card.getController().zoneManager.triggeredBuffer.addHidden(
				this, context);
		return true;
	}

	@Override
	public String getLog(ContextEventListener context) {
		return "removed modifier of '" + getCard() + "'";
	}

	@Override
	public String getAbilityTitle() {
		return "RemoveModifierAbility"
				+ (getName() != null ? ": " + getName() : "") + super.getAbilityTitle();
	}

	/**
	 * The modifier to remove with this action
	 */
	public Unregisterable modifier;

	/**
	 * Tag indicating we are currently unregistering this modifier
	 */
	protected boolean unregisteringmodifier;

}