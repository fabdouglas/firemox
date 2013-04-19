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
package net.sf.firemox.modifier.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.StaticModifier;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.And;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.test.True;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.72
 * @since 0.86 Any event interfering with the filter-test make this
 *        static-modifier to be re-evaluated for the concerned card.
 */
public class StaticModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of StaticModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>filtering zone id [Zone]</li>
	 * <li>modifiers [ModifierModel[]]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 * @see net.sf.firemox.token.IdTokens
	 */
	StaticModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		filterZone = inputFile.read();
		modifiers = new ModifierModel[inputFile.read()];
		layer = Layer.internal;
		for (int i = modifiers.length; i-- > 0;) {
			modifiers[i] = ModifierFactory.readModifier(inputFile);
			modifiers[i].linked = true;

			/*
			 * Need to add the while condition to say "while {modifier.creator} is in
			 * play"
			 */
			modifiers[i].whileCondition = And.append(modifiers[i].whileCondition,
					new InZone(getActivationZone(), TestOn.THIS));

			// append the while conditions
			modifiers[i].whileCondition = And.append(modifiers[i].whileCondition,
					whileCondition);

			// update the refresh events
			modifiers[i].whileCondition.extractTriggeredEvents(
					modifiers[i].linkedEvents, SystemCard.instance, True.getInstance());

			// add until events for each sub-modifier
			modifiers[i].until.addAll(until);

		}
	}

	@Override
	public ModifierModel clone() {
		throw new InternalError("not yet implemented");
	}

	@Override
	public void addModifierFromModelPriv(Ability ability, MCard target) {

		// proceed
		final List<Target> list = new ArrayList<Target>();
		if (filterZone == IdZones.ANYWHERE) {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(True.getInstance(), list, ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(True.getInstance(), list, ability);
		} else {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.getContainer(filterZone).checkAllCardsOf(True.getInstance(), list,
							ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.getContainer(filterZone).checkAllCardsOf(True.getInstance(), list,
							ability);
		}

		/**
		 * Do not add this modifier for the creator moving into play, since the
		 * incoming 'MovedCard' event will do it for us later.<br>
		 * 
		 * @see net.sf.firemox.action.MoveCard#moveCard(MCard,Player,int,ContextEventListener,int,Ability)
		 */
		if (target != null && target.getIdZone() == IdZones.PLAY) {
			list.remove(target);
		}
		for (ModifierModel modifierModel : modifiers) {
			for (Target targetI : list) {
				modifierModel.addModifierFromModel(ability, (MCard) targetI);
			}
		}
		list.clear();

		// setup static-modifier
		new StaticModifier(new ModifierContext(this, ability.getCard(), ability),
				modifiers, filterZone);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (ModifierModel modifier : modifiers) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(modifier.toHtmlString(ability, context));
		}
		return buffer.toString();
	}

	/**
	 * Is the looked for zone where this modifier can be added. Only components in
	 * this zone are affected by this modifier.
	 */
	private final int filterZone;

	/**
	 * Are the modifiers attached to this static way to attach globally card in
	 * the looked for zone.
	 */
	private final ModifierModel[] modifiers;

}
