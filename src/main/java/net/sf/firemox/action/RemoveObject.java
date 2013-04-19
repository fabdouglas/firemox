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
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Remove an object from the component of target list. The specified test is
 * used to filter the target list components involved by this action.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class RemoveObject extends UserAction implements LoopAction {

	/**
	 * The object's name to remove from the current target list.
	 */
	private final String objectName;

	/**
	 * The test applied on specific modifier to be removed.
	 */
	private final Test objectTest;

	/**
	 * Create an instance of RemoveObject
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>object name [String]</li>
	 * <li>filter [Test]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is ignored
	 * @param card
	 *          owning this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	RemoveObject(InputStream inputFile) throws IOException {
		super(inputFile);
		objectName = MToolKit.readString(inputFile).intern();
		objectTest = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Check there are the required amount of objects attached to the specified
	 * target.
	 * 
	 * @param target
	 *          the component where objects will be removed from
	 * @param nbObjects
	 *          amount of required object
	 * @return true if the specified target contains the required object(s)
	 */
	public boolean checkObject(Target target, int nbObjects) {
		if (!target.isCard()) {
			throw new InternalError(
					"TODO RemoveObject action is only supported for Card component");
		}
		return ObjectFactory.getNbObject(objectName, (MCard) target, objectTest) >= nbObjects;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		final Target target = StackManager.getInstance().getTargetedList().get(
				loopingIndex);
		if (!target.isCard()) {
			throw new InternalError(
					"TODO RemoveObject action is only supported for Card component");
		}
		if (checkTimeStamp(context, (MCard) target)) {
			ObjectFactory
					.removeObjectModifier(objectName, (MCard) target, objectTest);
		}
		return true;
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.REMOVE_OBJECT;
	}

	public int getStartIndex() {
		return StackManager.getInstance().getTargetedList().size() - 1;
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
		return LanguageManagerMDB.getString("removeobject-1", objectName
				.replaceAll("/", ""));
	}

	@Override
	public String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (times == 1) {
			return LanguageManagerMDB.getString("removeobject-1", objectName
					.replaceAll("/", ""));
		}
		if (times == -1) {
			// Preemption
			return LanguageManagerMDB.getString("removeobject-%n",
					objectName.replaceAll("/", "")).replaceAll("%n",
					"" + MdbLoader.unknownSmlManaHtml);
		}
		return LanguageManagerMDB.getString("removeobject-%n",
				objectName.replaceAll("/", "")).replaceAll("%n", "" + times);
	}

	@Override
	public String toString(Ability ability) {
		return "removeobject-" + objectName;
	}

}
