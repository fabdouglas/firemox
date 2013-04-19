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
package net.sf.firemox.modifier.model;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.ObjectRegisterModifier;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
class ObjectRegisterModifierModel extends RegisterModifierModel implements
		ObjectModifierModel {

	/**
	 * Creates a new instance of ObjectRegisterModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>is this object can be paint [boolean]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 * @see net.sf.firemox.token.IdTokens
	 */
	ObjectRegisterModifierModel(InputStream inputStream) throws IOException {
		super(inputStream);
		paint = inputStream.read() == 1;

		// do not remove modifier if the move is PLAY to PLAY
		linked = false;
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	private ObjectRegisterModifierModel(ObjectModifierModel other) {
		super((ObjectRegisterModifierModel) other);
		this.paint = ((ObjectRegisterModifierModel) other).paint;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final int index = this.getModifiedIndex();
		final ObjectRegisterModifier newModifier = new ObjectRegisterModifier(
				new ModifierContext(this, target, ability), index,
				liveUpdate ? new IntValue(rightExpression.getValue(ability, target,
						null)) : rightExpression, op, paint);
		target.addModifier(newModifier, index);
		newModifier.refresh();
	}

	@Override
	public ObjectRegisterModifierModel clone() {
		return new ObjectRegisterModifierModel(this);
	}

	/**
	 * Return the modified index.
	 * 
	 * @return the modified index.
	 */
	public int getModifiedIndex() {
		return this.index.getValue(null, null, null);
	}

	public String getObjectName() {
		return name;
	}

	@Override
	public void removeObject(MCard fromCard, Test objectTest) {
		final int index = this.getModifiedIndex();
		if (fromCard.registerModifiers[index] != null) {
			fromCard.registerModifiers[index] = (RegisterModifier) fromCard.registerModifiers[index]
					.removeObject(name, objectTest);
		}
		super.removeObject(fromCard, objectTest);
	}

	public int getNbObject(MCard card, Test objectTest) {
		if (card.registerModifiers == null) {
			return 0;
		}
		return card.registerModifiers[ObjectFactory.getObjectRegisterModifierModel(
				name).getModifiedIndex()] == null ? 0
				: card.registerModifiers[ObjectFactory.getObjectRegisterModifierModel(
						name).getModifiedIndex()].getNbObjects(name, objectTest);
	}

	/**
	 * Is this object can be paint.
	 */
	private final boolean paint;

}
