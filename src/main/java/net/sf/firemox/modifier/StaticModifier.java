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
package net.sf.firemox.modifier;

import net.sf.firemox.action.AddModifierFromStaticModifier;
import net.sf.firemox.clickable.ability.TriggeredStaticModifierAbility;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.test.True;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 Any event interfering with the filter-test make this
 *        static-modifier to be re-evaluated for the concerned card.
 */
public class StaticModifier extends Modifier {

	/**
	 * Creates a new instance of StaticModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param modifiers
	 *          are the modifiers attached to this static way to attach globally
	 *          card in the looked for zone.
	 * @param filterZone
	 *          is the looked for zone where this modifier can be added. Only
	 *          components in this zone are affected by this modifier.
	 */
	public StaticModifier(ModifierContext context, ModifierModel[] modifiers,
			int filterZone) {
		super(context);

		// we add the trigger looking for new cards coming into the zone
		if (filterZone != IdZones.ANYWHERE) {
			final TriggeredStaticModifierAbility refreshAbility = new TriggeredStaticModifierAbility(
					new MovedCard(filterZone, True.getInstance(), new InZone(filterZone,
							TestOn.TESTED), ability.getCard()),
					new AddModifierFromStaticModifier(modifiers), ability.getCard(),
					modifiers);
			abilities.add(refreshAbility);
			refreshAbility.registerToManager();
		}
	}

	@Override
	public void refresh() {
		// TODO remove refresh ability of this modifier
		// Ignored : throw new InternalError("This modifier cannot be refreshed this
		// way");
	}

}
