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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.ControllerModifier;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class ControllerModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of ControllerModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>player [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ControllerModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		this.newController = TestOn.deserialize(inputFile);

		/*
		 * override the last event saying : if the 'to' leave play, the modifier is
		 * destroyed by this one : if the 'to' moves, the modifier is destroyed
		 */
		// until.removeLast();
		// until.add(new MEventModifiedController(IdZones.ID__PLAY, new IsMe(),
		// MSystemCard.instance));
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final ControllerModifier newModifier = new ControllerModifier(
				new ModifierContext(this, target, ability), newController);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected ControllerModifierModel(ControllerModifierModel other) {
		super(other);
		newController = other.newController;
	}

	@Override
	public ControllerModifierModel clone() {
		return new ControllerModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return LanguageManagerMDB.getString("add-controller-modifier-"
				+ newController);
	}

	/**
	 * The new controller
	 */
	private TestOn newController;

}
