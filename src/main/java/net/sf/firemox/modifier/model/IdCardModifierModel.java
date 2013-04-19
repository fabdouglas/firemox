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
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.modifier.IdCardModifier;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 Operation is used instead of add/remove boolean
 *        <code>hasNot</code>
 */
public class IdCardModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of IdCardModifiersModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>operation [Operation]
	 * <li>idCard [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	IdCardModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
		this.idCard = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected IdCardModifierModel(IdCardModifierModel other) {
		super(other);
		idCard = other.idCard;
		op = other.op;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final IdCardModifier newModifier = new IdCardModifier(new ModifierContext(
				this, target, ability), liveUpdate ? idCard : new IntValue(idCard
				.getValue(ability, target, null)), op);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	@Override
	public IdCardModifierModel clone() {
		return new IdCardModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		int initIdCard = this.idCard.getValue(ability, null, context);
		int startIndex = CardFactory.exportedIdCardValues.length;
		String tmpString = null;
		while (startIndex-- > 0 && initIdCard != 0) {
			if (MCard.intersectionIdCard(initIdCard,
					CardFactory.exportedIdCardValues[startIndex])) {
				initIdCard &= ~CardFactory.exportedIdCardValues[startIndex];
				if (tmpString == null) {
					tmpString = LanguageManagerMDB.getString("add-idcard-modifier-"
							+ op.getOperator(), CardFactory.exportedIdCardNames[startIndex]);
				} else {
					tmpString += ", "
							+ LanguageManagerMDB.getString("add-idcard-modifier-"
									+ op.getOperator(),
									CardFactory.exportedIdCardNames[startIndex]);
				}
			}
		}
		if (tmpString != null) {
			return tmpString;
		}
		return LanguageManagerMDB.getString("add-idcard-modifier-"
				+ op.getOperator(), "<font color=\"red\">(" + idCard + ")</font>");
	}

	/**
	 * The modified idCard
	 */
	protected Expression idCard;

	/**
	 * Indicates the operation applied to previous value with the value of this
	 * modifier.
	 */
	protected Operation op;

}
