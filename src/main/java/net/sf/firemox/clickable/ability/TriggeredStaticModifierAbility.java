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

import net.sf.firemox.action.AddModifierFromStaticModifier;
import net.sf.firemox.action.MAction;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class TriggeredStaticModifierAbility extends AbstractAbility {

	/**
	 * Creates a new instance of TriggeredStaticModifierAbility <br>
	 * 
	 * @param event
	 *          the event attached to this ability.
	 * @param refreshAction
	 * @param trigger
	 * @param modifiers
	 */
	public TriggeredStaticModifierAbility(TriggeredEvent event,
			AddModifierFromStaticModifier refreshAction, MCard trigger,
			ModifierModel... modifiers) {
		super("static modifier trigger", event, null, null, TrueFalseAuto.FALSE);
		actionList = new MAction[] {};
		effectList = new MAction[] { refreshAction };
		this.trigger = trigger;
		// override the resolver
		priority = Priority.hidden_high;
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		trigger.getController().zoneManager.triggeredBuffer
				.addHidden(this, context);
		return true;
	}

	@Override
	public MCard getCard() {
		return trigger;
	}

	@Override
	public String getLog(ContextEventListener context) {
		return "updating static ability of " + getCard();
	}

	@Override
	public String getAbilityTitle() {
		return "TriggeredStaticModifierAbility"
				+ (getName() != null ? ": " + getName() : "") + super.getAbilityTitle();
	}

	@Override
	public String toString() {
		return name + ", creator=" + trigger.getName() + ", " + effectList[0];
	}

	private MCard trigger;

}
