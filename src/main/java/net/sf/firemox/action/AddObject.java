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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.model.ObjectFactory;
import net.sf.firemox.modifier.model.ObjectModifierModel;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * 
 */
class AddObject extends UserAction implements LoopAction {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>[super]</li>
	 * <li>object name [String]</li>
	 * <li>model [ObjectModel]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          stream containing this action.
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @see Actiontype
	 */
	AddObject(InputStream inputFile) throws IOException {
		super(inputFile);
		final String objectName = MToolKit.readString(inputFile).intern();
		final ObjectModifierModel object = ObjectFactory
				.getObjectModifierModel(objectName);
		if (object == null) {
			throw new RuntimeException("Unknown Object '" + objectName + "'");
		}
		objectModifierModel = (ObjectModifierModel) object.clone();
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.ADD_OBJECT;
	}

	@Override
	public String toString(Ability ability) {
		return "addobject-" + objectModifierModel.getObjectName();
	}

	@Override
	public String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (times == 1) {
			return LanguageManagerMDB.getString("addobject-1-"
					+ objectModifierModel.getObjectName().replaceAll("/", ""));
		}
		if (times == -1) {
			// Preemption
			return LanguageManagerMDB.getString("addobject-%n",
					objectModifierModel.getObjectName().replaceAll("/", "")).replaceAll(
					"%n", "" + MdbLoader.unknownSmlManaHtml);
		}
		return LanguageManagerMDB.getString("addobject-%n",
				objectModifierModel.getObjectName().replaceAll("/", "")).replaceAll(
				"%n", "" + times);
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
		return LanguageManagerMDB.getString("addobject-1", objectModifierModel
				.getObjectName().replaceAll("/", ""));
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		final Target target = StackManager.getInstance().getTargetedList().get(
				loopingIndex);
		if (!target.isCard()) {
			throw new InternalError(
					"AddObject action is only supported for Card component");
		}
		if (checkTimeStamp(context, (MCard) target)) {
			objectModifierModel.addModifierFromModel(ability, (MCard) target);
			target.repaint();
		}
		return true;
	}

	public int getStartIndex() {
		return StackManager.getInstance().getTargetedList().size() - 1;
	}

	/**
	 * The object to add to the current target list.
	 */
	private ObjectModifierModel objectModifierModel;
}
