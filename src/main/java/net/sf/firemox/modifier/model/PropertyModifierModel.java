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
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.PropertyModifier;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 properties set is calculated as colors/id card and register :
 *        they follow timestamp rule.
 */
public class PropertyModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of PropertyModifier <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>addProperty [boolean]</li>
	 * <li>propertyId [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	PropertyModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		addProperty = inputFile.read() == 1;
		propertyId = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected PropertyModifierModel(PropertyModifierModel other) {
		super(other);
		addProperty = other.addProperty;
		propertyId = other.propertyId;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final PropertyModifier newModifier = new PropertyModifier(
				new ModifierContext(this, target, ability), liveUpdate ? propertyId
						: new IntValue(propertyId.getValue(ability, target, null)),
				addProperty);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	@Override
	public PropertyModifierModel clone() {
		return new PropertyModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		int pValue = propertyId.getValue(ability, null, context);
		final String propertyName = CardFactory.exportedProperties.get(pValue);
		if (propertyName == null) {
			// not name associated to this property
			if (addProperty)
				return LanguageManagerMDB.getString("add-property-modifier-add",
						"<font color='red'>(" + pValue + ")</font>");
			return LanguageManagerMDB.getString("add-property-modifier-remove",
					"<font color='red'>(" + pValue + ")</font>");
		}
		if (addProperty)
			return LanguageManagerMDB.getString("add-property-modifier-add",
					propertyName);
		return LanguageManagerMDB.getString("add-property-modifier-remove",
				propertyName);
	}

	/**
	 * Indicates if this modifier remove occurrence of the given property. If
	 * True, it adds one instance.
	 */
	protected boolean addProperty;

	/**
	 * Property Id to add/remove
	 */
	protected Expression propertyId;

}
