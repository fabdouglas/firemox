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
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.test.True;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a modifier="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan
 *         </a>
 * @since 0.85 Recalculate attribute is supported
 */
public class RegisterModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of RegisterModifier <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>index [Expression]</li>
	 * <li>right value [Expression]</li>
	 * <li>operation [Operation]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 * @see net.sf.firemox.token.IdTokens
	 */
	RegisterModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		index = ExpressionFactory.readNextExpression(inputFile);
		rightExpression = ExpressionFactory.readNextExpression(inputFile);
		rightExpression.extractTriggeredEvents(linkedEvents, SystemCard.instance,
				True.getInstance());
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected RegisterModifierModel(RegisterModifierModel other) {
		super(other);
		index = other.index;
		rightExpression = other.rightExpression;
		op = other.op;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		int index = this.index.getValue(ability, null, null);
		RegisterModifier newModifier = new RegisterModifier(new ModifierContext(
				this, target, ability), index, liveUpdate ? rightExpression
				: new IntValue(rightExpression.getValue(ability, target, null)), op);
		target.addModifier(newModifier, index);
		newModifier.refresh();
	}

	@Override
	public RegisterModifierModel clone() {
		return new RegisterModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		throw new InternalError("should not be called");
	}

	/**
	 * Return the HTML code representing this action. If this action is named,
	 * it's name will be returned. If not, if existing, the picture associated to
	 * this action is returned. Otherwise, built-in action's text will be
	 * returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 * @param other
	 *          the immediately followed register modifier instance.
	 * @return the HTML code representing this action. If this action is named,
	 *         it's name will be returned. If not, if existing, the picture
	 *         associated to this action is returned. Otherwise, built-in action's
	 *         text will be returned.
	 * @since 0.86
	 */
	public String toHtmlString(Ability ability, ContextEventListener context,
			RegisterModifierModel other) {
		int index = this.index.getPreemptionValue(ability, null, context);
		if (other == null) {
			return LanguageManagerMDB.getString("add-register-modifier-"
					+ op.getOperator() + "-" + index, String.valueOf(rightExpression
					.getValue(ability, null, context)));
		}
		int otherIndex = other.index.getPreemptionValue(ability, null, context);
		if (index > otherIndex) {
			return other.toHtmlString(ability, context, this);
		}
		return LanguageManagerMDB.getString("add-register-modifier-"
				+ op.getOperator() + "-" + index + "-" + other.op.getOperator() + "-"
				+ otherIndex, String.valueOf(rightExpression.getValue(ability, null,
				context)), String.valueOf(other.rightExpression.getValue(ability, null,
				context)));
	}

	/**
	 * The looked for register index
	 */
	protected final Expression index;

	/**
	 * The operation applied to the left and right values
	 */
	protected final Operation op;

	/**
	 * right integer value. The complex expression to use for the right value. Is
	 * null if the IdToken number is not a complex expression.
	 */
	protected final Expression rightExpression;

}
