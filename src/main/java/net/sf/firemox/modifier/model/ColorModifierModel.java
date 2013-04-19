/*
 * Created on Sep 14, 2004 
 * 
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
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.modifier.ColorModifier;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 Operation is used instead of add/remove boolean
 *        <code>hasNot</code>
 */
public class ColorModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of ColorModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>operation [Operation]</li>
	 * <li>idColor [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ColorModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
		this.idColor = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected ColorModifierModel(ColorModifierModel other) {
		super(other);
		idColor = other.idColor;
		op = other.op;
	}

	@Override
	public ColorModifierModel clone() {
		return new ColorModifierModel(this);
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final ColorModifier newModifier = new ColorModifier(new ModifierContext(
				this, target, ability), liveUpdate ? idColor : new IntValue(idColor
				.getValue(ability, target, null)), op);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		final int idColor = this.idColor.getValue(ability, null, context);
		String tmpString = null;
		for (int i = IdCardColors.CARD_COLOR_VALUES.length; i-- > 1;) {
			if (MCard.hasIdColor(idColor, IdCardColors.CARD_COLOR_VALUES[i])) {
				if (tmpString == null) {
					tmpString = LanguageManager
							.getString(IdCardColors.CARD_COLOR_NAMES[i]);
				} else {
					tmpString += ", "
							+ LanguageManager.getString(IdCardColors.CARD_COLOR_NAMES[i]);
				}
			}
		}
		return LanguageManagerMDB.getString("add-color-modifier-"
				+ op.getOperator(), tmpString);
	}

	/**
	 * The modified idColor
	 */
	protected final Expression idColor;

	/**
	 * Indicates the operation applied to previous value with the value of this
	 * modifier.
	 */
	protected final Operation op;

}
