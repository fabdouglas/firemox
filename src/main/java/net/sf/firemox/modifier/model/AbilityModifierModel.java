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
import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.AbilityModifier;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class AbilityModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of AbilityModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>operation [Operation]</li>
	 * <li>abilities [Ability[]]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	AbilityModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
		abilitiesToAdd = new Ability[inputFile.read()];
		for (int i = abilitiesToAdd.length; i-- > 0;) {
			abilitiesToAdd[i] = AbilityFactory.readAbility(inputFile,
					SystemCard.instance);
		}
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final Ability[] copyAbilities = new Ability[abilitiesToAdd.length];
		for (int i = copyAbilities.length; i-- > 0;) {
			copyAbilities[i] = abilitiesToAdd[i].clone(target);
		}
		final AbilityModifier newModifier = new AbilityModifier(
				new ModifierContext(this, target, ability), op, copyAbilities);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected AbilityModifierModel(AbilityModifierModel other) {
		super(other);
		op = other.op;
		abilitiesToAdd = other.abilitiesToAdd;
	}

	@Override
	public AbilityModifierModel clone() {
		return new AbilityModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (abilitiesToAdd.length == 0) {
			return LanguageManagerMDB.getString("add-ability-modifier-"
					+ op.getOperator());
		}
		return LanguageManagerMDB.getString("add-ability-modifier-"
				+ (op.getOperator()), abilitiesToAdd[0].toHtmlString(context));
	}

	@Override
	public String toString() {
		if (abilitiesToAdd.length == 0) {
			return "ability-modifier (" + op + ", NULL)";
		}
		return "ability-modifier (" + op + ", " + abilitiesToAdd[0] + ")";
	}

	/**
	 * The list of abilities added/removed by this modifier
	 */
	protected Ability[] abilitiesToAdd;

	/**
	 * Indicates the operation applied to previous value with the value of this
	 * modifier.
	 */
	protected Operation op;
}
