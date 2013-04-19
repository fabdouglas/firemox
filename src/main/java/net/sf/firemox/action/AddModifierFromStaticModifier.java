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

import java.util.Arrays;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.modifier.model.ModifierModel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class AddModifierFromStaticModifier extends MAction implements
		StandardAction {

	/**
	 * Creates a new instance of AddModifierFromStaticModifier <br>
	 * 
	 * @param modifiers
	 */
	public AddModifierFromStaticModifier(ModifierModel... modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.REFRESH_STATIC_MODIFIER;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		/*
		 * this action can only be played with a triggered ability activated with
		 * the 'moved-card' event
		 */
		final MContextTarget context2 = (MContextTarget) context;
		for (ModifierModel modifierModel : modifiers) {
			modifierModel.addModifierFromModel(ability, context2.getOriginalCard());
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return toString();
	}

	@Override
	public String toString() {
		return "add modifiers from static : " + Arrays.toString(modifiers);
	}

	/**
	 * List of static modifiers to add
	 */
	private final ModifierModel[] modifiers;

}