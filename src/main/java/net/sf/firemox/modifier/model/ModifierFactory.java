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

import net.sf.firemox.modifier.ModifierType;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public final class ModifierFactory {

	private ModifierFactory() {
		// Nothing to do
	}

	/**
	 * Read and return the next action from specified inputFile
	 * 
	 * @param inputFile
	 *          the input stream where modifier will be read
	 * @return the next modifier read from the specified inputFile
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static ModifierModel readModifier(InputStream inputFile)
			throws IOException {
		final ModifierType id = ModifierType.deserialize(inputFile);
		switch (id) {
		case OBJECT_MODIFIER:
			final String objName = MToolKit.readString(inputFile);
			return (ModifierModel) ObjectFactory.getObjectModifierModel(objName)
					.clone();
		case REGISTER_MODIFIER:
			return new RegisterModifierModel(inputFile);
		case REGISTER_INDIRECTION:
			return new RegisterIndirectionModel(inputFile);
		case IDCARD_MODIFIER:
			return new IdCardModifierModel(inputFile);
		case COLOR_MODIFIER:
			return new ColorModifierModel(inputFile);
		case PLAYABLE_ZONE_MODIFIER:
			return new PlayableZoneModifierModel(inputFile);
		case PROPERTY_MODIFIER:
			return new PropertyModifierModel(inputFile);
		case CONTROLLER_MODIFIER:
			return new ControllerModifierModel(inputFile);
		case STATIC_MODIFIER:
			return new StaticModifierModel(inputFile);
		case ABILITY_MODIFIER:
			return new AbilityModifierModel(inputFile);
		case ADDITIONAL_COST_MODIFIER:
			return new AdditionalCostModifierModel(inputFile);
		default:
			throw new InternalError("Unmanaged modifier :" + id);
		}
	}

}
