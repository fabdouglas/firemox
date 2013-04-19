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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public final class AbilityFactory {

	/**
	 * Create a new instance of this class.
	 */
	private AbilityFactory() {
		super();
	}

	/**
	 * Read and return the next ability from specified inputFile
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>ability type [1]</li>
	 * <li>name name [String]</li>
	 * <li>ability tags [1]</li>
	 * <li>event [Event]</li>
	 * <li>cost [Action[]]</li>
	 * <li>effect [Action[]]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where ability will be read
	 * @param card
	 *          the card owning the returned action
	 * @return the next action read into of specified inputFile
	 * @throws IOException
	 *           during the stream read
	 */
	public static Ability readAbility(InputStream inputFile, MCard card)
			throws IOException {
		final AbilityType idAbility = AbilityType.valueOf(inputFile);
		try {
			switch (idAbility) {
			case ACTIVATED_ABILITY:
				return new ActivatedAbility(inputFile, card);
			case ACTIVATED_ABILITY_PLAYER:
				return new ActivatedAbilityPlayer(inputFile, card);
			case SYSTEM_ABILITY:
				return new SystemAbility(inputFile);
			case TRIGGERED_ABILITY:
				return new TriggeredAbility(inputFile, card);
			case TRIGGERED_ABILITY_SET:
				return new TriggeredAbilitySet(inputFile, card);
			case REPLACEMENT_ABILITY:
				return new ReplacementAbility(inputFile, card);
			case REFERENCED_ABILITY:
				final String referenceName = MToolKit.readString(inputFile);
				if (referenceName.equals(currentReference)
						|| "this".equals(referenceName)) {
					return lastInstance.clone(card);
				}
				final UserAbility ability = (UserAbility) referencedAbilities
						.get(referenceName);
				if (ability == null) {
					throw new InternalError("No ability named as '" + referenceName
							+ "' exists in the rules declaration, card=" + card.getName());
				}
				return ability.clone(card);
			default:
				throw new InternalError("Unknow ability type : " + idAbility);
			}
		} catch (Throwable e) {
			if (currentReference != null) {
				throw new RuntimeException(
						"Error reading ability tag for ability reference "
								+ currentReference, e);
			}
			throw new RuntimeException("Error reading ability tag for ability "
					+ lastInstance.getName(), e);
		}

	}

	/**
	 * Read available action constraints and abilities references.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>number of action constraints [1]</li>
	 * <li>id action constraint i [1]</li>
	 * <li>test as constraint i [...]</li>
	 * </ul>
	 * 
	 * @param dbStream
	 *          the stream header.
	 * @throws IOException
	 *           during the stream read
	 */
	public static void init(InputStream dbStream) throws IOException {
		// the abilities references
		if (referencedAbilities != null) {
			referencedAbilities.clear();
		} else {
			referencedAbilities = new HashMap<String, Ability>();
		}
		for (int i = dbStream.read(); i-- > 0;) {
			currentReference = MToolKit.readString(dbStream);
			referencedAbilities.put(currentReference, readAbility(dbStream,
					SystemCard.instance));
			currentReference = null;
		}
	}

	/**
	 * Set of declared abilities
	 */
	private static Map<String, Ability> referencedAbilities;

	/**
	 * The last created instance of ability created from Stream. This field has
	 * been added to enable recurcive dependencies.
	 */
	static Ability lastInstance;

	/**
	 * The last created instance of reference ability's name created from Stream.
	 */
	private static String currentReference;

}
