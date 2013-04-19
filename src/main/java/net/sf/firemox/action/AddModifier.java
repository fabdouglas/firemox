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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.model.ModifierFactory;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.modifier.model.RegisterIndirectionModel;
import net.sf.firemox.modifier.model.RegisterModifierModel;
import net.sf.firemox.modifier.model.StaticModifierModel;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Add a modifier on the components of target-list.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class AddModifier extends UserAction implements StandardAction {

	/**
	 * Creates a new instance of AddModifier <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>super [UserAction]</li>
	 * <li>modifiers [ModifierModel]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          stream containing this action.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	AddModifier(InputStream inputFile) throws IOException {
		super(inputFile);
		int count = inputFile.read();
		modifiers = new ModifierModel[count];
		for (int i = count; i-- > 0;) {
			modifiers[i] = ModifierFactory.readModifier(inputFile);
		}
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ADD_MODIFIER;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (ModifierModel modifier : modifiers) {
			if (modifier instanceof StaticModifierModel) {
				// static modifiers do not use the target list
				modifier.addModifierFromModel(ability, null);
			} else {
				for (Target target : StackManager.getInstance().getTargetedList().list) {
					if (target.isCard() && checkTimeStamp(context, (MCard) target)) {
						modifier.addModifierFromModel(ability, (MCard) target);
					}
				}
			}
		}
		return true;
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName != null) {
			if (actionName.charAt(0) == '%') {
				return "";
			}
			if (actionName.charAt(0) == '@') {
				final String picture = ActionFactory.PICTURES.get(actionName);
				if (picture != null) {
					return toHtmlString(ability, picture);
				}
			} else {
				return LanguageManagerMDB.getString(actionName);
			}
		}
		// no action name
		StringBuilder result = null;
		for (int i = 0; i < modifiers.length; i++) {
			if (modifiers[i] instanceof RegisterModifierModel) {
				final String tmpResult = ((RegisterModifierModel) modifiers[i])
						.toHtmlString(ability, context, i == modifiers.length - 1
								|| !(modifiers[i + 1] instanceof RegisterModifierModel) ? null
								: (RegisterModifierModel) modifiers[++i]);
				if (result == null) {
					result = new StringBuilder(tmpResult);
				} else {
					result.append(", ");
					result.append(tmpResult);
				}
				continue;
			} else if (modifiers[i] instanceof RegisterIndirectionModel) {
				final String tmpResult = ((RegisterIndirectionModel) modifiers[i])
						.toHtmlString(
								ability,
								context,
								i == modifiers.length - 1
										|| !(modifiers[i + 1] instanceof RegisterIndirectionModel) ? null
										: (RegisterIndirectionModel) modifiers[++i]);
				if (result == null) {
					result = new StringBuilder(tmpResult);
				} else {
					result.append(", ");
					result.append(tmpResult);
				}
				continue;
			}
			if (result == null) {
				result = new StringBuilder(modifiers[i].toHtmlString(ability, context));
			} else {
				result.append(", ");
				result.append(modifiers[i].toHtmlString(ability, context));
			}
		}
		if (result == null) {
			return null;
		}
		return result.toString();
	}

	@Override
	public String toString(Ability ability) {
		return "add modifiers";
	}

	/**
	 * The modifiers model to add to the target list
	 */
	private ModifierModel[] modifiers;

}