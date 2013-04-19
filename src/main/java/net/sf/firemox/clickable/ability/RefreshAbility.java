/*
 * Created on Sep 10, 2004 
 * Original filename was RefreshAbility.java
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
import net.sf.firemox.action.RefreshModifier;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class RefreshAbility extends AbstractAbility {

	/**
	 * Creates a new instance of RefreshAbility <br>
	 * 
	 * @param event
	 *          the event attached to this ability.
	 * @param refreshAction
	 * @param trigger
	 */
	public RefreshAbility(TriggeredEvent event, RefreshModifier refreshAction,
			MCard trigger) {
		super("refresh modifier", event, null, null, TrueFalseAuto.FALSE);
		actionList = new MAction[] {};
		effectList = new MAction[] { refreshAction };
		this.trigger = trigger;
		// override the resolver
		priority = Priority.hidden_high;
		// override the optimizer
		optimizer = Optimization.last;
	}

	@Override
	public void optimizeRegisterToManager() {
		for (Ability ability : MEventListener.TRIGGRED_ABILITIES.get(eventComing
				.getIdEvent())) {
			if (equals(ability)) {
				/*
				 * this refresh modifier is already existing. Instead of registering
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
		if (ability instanceof RefreshAbility) {
			RefreshAbility refreshAbility = (RefreshAbility) ability;
			if (refreshAbility.effectList[0] == effectList[0]) {
				if (refreshAbility.eventComing instanceof MovedCard) {
					return refreshAbility.eventComing instanceof MovedCard
							&& refreshAbility.eventComing.equals(eventComing)
							&& ((MContextTarget) thisContext).getTargetable() == ((MContextTarget) context)
									.getTargetable();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (eventComing != null && eventComing.card != null)
			return eventComing.card.hashCode();
		return super.hashCode();
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		trigger.getController().zoneManager.triggeredBuffer
				.addHidden(this, context);
		return true;
	}

	@Override
	public String toString() {
		return getName() + ", on card " + eventComing.card + ", from modifier:"
				+ trigger;
	}

	@Override
	public String getLog(ContextEventListener context) {
		return "refresh modifier of '" + getCard() + "' on " + eventComing.card;
	}

	@Override
	public MCard getCard() {
		return trigger;
	}

	@Override
	public String getAbilityTitle() {
		return "RefreshAbility" + (getName() != null ? ": " + getName() : "")
				+ super.getAbilityTitle();
	}

	private MCard trigger;

}
