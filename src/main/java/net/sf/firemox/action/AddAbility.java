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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.clickable.ability.RemoveModifier;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.EventFactory;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;

/**
 * Create a new ability and add it to the current target list. This ability is
 * linked to many optional 'until' events.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
class AddAbility extends UserAction implements StandardAction {

	/**
	 * The ability to add
	 */
	private final Ability abilityToAdd;

	/**
	 * When this event is generated this modifier is removed from the attached
	 * object. If this event is equal to null, this modifier can be removed only
	 * by the attached component.
	 */
	private final List<MEventListener> until;

	/**
	 * Create an instance of AddAbility by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>ability to Add [Ability]</li>
	 * <li>exists until this triggered event [Event[]]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	AddAbility(InputStream inputFile) throws IOException {
		super(inputFile);

		abilityToAdd = AbilityFactory.readAbility(inputFile, SystemCard.instance);

		// until events
		int count = inputFile.read();
		until = new ArrayList<MEventListener>(count);
		while (count-- > 0) {
			until.add(EventFactory.readNextEvent(inputFile, SystemCard.instance));
		}

	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ADD_ABILITY;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		// create ability, and add it to the card
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			Target to = StackManager.getInstance().getTargetedList().get(i);
			Ability clonedAbility = null;
			if (to.isCard()) {
				clonedAbility = abilityToAdd.clone((MCard) to);
			} else {
				// the target is a player
				clonedAbility = abilityToAdd.clone(((Player) to).playerCard);
			}

			// create the 'until' triggers of this ability
			for (int j = until.size(); j-- > 0;) {
				clonedAbility.addLinkedAbility(new RemoveModifier(
						(TriggeredEvent) until.get(j), clonedAbility));
			}

			// register the ability
			clonedAbility.registerToManager();
			to.cachedAbilities.add(clonedAbility);
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Add ability";
	}

}