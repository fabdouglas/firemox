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
import net.sf.firemox.modifier.AbilityModifier;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.ObjectAbilityModifier;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class ObjectAbilityModifierModel extends AbilityModifierModel implements
		ObjectModifierModel {

	/**
	 * Is this object can be paint.
	 */
	private final boolean paint;

	/**
	 * Creates a new instance of ObjectAbilityModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>is this object can be paint [boolean]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ObjectAbilityModifierModel(InputStream inputStream) throws IOException {
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
	private ObjectAbilityModifierModel(ObjectModifierModel other) {
		super((AbilityModifierModel) other);
		this.paint = ((ObjectAbilityModifierModel) other).paint;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final Ability[] copyAbilities = new Ability[abilitiesToAdd.length];
		for (int i = copyAbilities.length; i-- > 0;) {
			copyAbilities[i] = abilitiesToAdd[i].clone(target);
		}
		final ObjectAbilityModifier newModifier = new ObjectAbilityModifier(
				new ModifierContext(this, target, ability), op, copyAbilities, paint);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	@Override
	public ObjectAbilityModifierModel clone() {
		return new ObjectAbilityModifierModel(this);
	}

	public String getObjectName() {
		return name;
	}

	@Override
	public void removeObject(MCard fromCard, Test objectTest) {
		if (fromCard.abilityModifier != null) {
			fromCard.abilityModifier = (AbilityModifier) fromCard.abilityModifier
					.removeObject(name, objectTest);
		}
		super.removeObject(fromCard, objectTest);
	}

	public int getNbObject(MCard card, Test objectTest) {
		return card.abilityModifier == null ? 0 : card.abilityModifier
				.getNbObjects(name, objectTest);
	}

}
