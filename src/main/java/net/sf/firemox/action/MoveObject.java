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
import net.sf.firemox.test.True;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Move an object from a component to another component.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class MoveObject extends UserAction implements LoopAction {

	/**
	 * Create an instance of MoveObject
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>object name [String]</li>
	 * <li>from [TestOn]</li>
	 * <li>to [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is ignored
	 * @param card
	 *          owning this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	MoveObject(InputStream inputFile) throws IOException {
		super(inputFile);
		objectName = MToolKit.readString(inputFile).intern();
		from = TestOn.deserialize(inputFile);
		to = TestOn.deserialize(inputFile);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.MOVE_OBJECT;
	}

	@Override
	public String toString(Ability ability) {
		return "moveobject-" + objectName;
	}

	@Override
	public String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (times == 1) {
			return LanguageManagerMDB.getString("moveobject-1", objectName
					.replaceAll("/", ""), from.toString(), to.toString());
		}
		if (times == -1) {
			// Preemption
			return LanguageManagerMDB.getString("moveobject-%n",
					objectName.replaceAll("/", ""), from.toString(), to.toString())
					.replaceAll("%n", "" + MdbLoader.unknownSmlManaHtml);
		}
		return LanguageManagerMDB.getString("moveobject-%n",
				objectName.replaceAll("/", ""), from.toString(), to.toString())
				.replaceAll("%n", "" + times);
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
		return LanguageManagerMDB.getString("moveobject-1", objectName.replaceAll(
				"/", ""), from.toString(), to.toString());
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		// final Targetable from = StackManager.getInstance().getTargetedList().get(
		// loopingIndex);
		final MCard from = this.from.getCard(ability, null);
		if (!from.isCard()) {
			throw new InternalError(
					"TODO MoveObject action is only supported for Card component");
		}
		if (checkTimeStamp(context, from)) {
			ObjectFactory.removeObjectModifier(objectName, from, True.getInstance());
			MCard to = this.to.getCard(ability, null);
			ObjectFactory.getObjectModifierModel(objectName).addModifierFromModel(
					ability, to);
			from.repaint();
			to.repaint();
		}
		return true;
	}

	/**
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param target
	 *          the component where objects will be removed from
	 * @param nbObjects
	 *          amount of required object
	 * @return true if the specified target contains the required object(s)
	 */
	public boolean checkObject(Ability ability, Target target, int nbObjects) {
		if (!target.isCard()) {
			throw new InternalError(
					"TODO MmoveObject action is only supported for Card component");
		}
		return ObjectFactory.getNbObject(objectName, (MCard) target, True
				.getInstance()) >= nbObjects;
	}

	public int getStartIndex() {
		return 0;
	}

	/**
	 * The object's name to remove from the current target list.
	 */
	private final String objectName;

	/**
	 * The object's source of move.
	 */
	private final TestOn from;

	/**
	 * The object's destination of move.
	 */
	private final TestOn to;

}
