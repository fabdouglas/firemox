/*
 * TargetManager.java
 * Created on 12 oct. 2003
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
package net.sf.firemox.stack;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.zone.MZone;

/**
 * Represents the target settings as color, id card, owner, ... of the target.
 * Before a target will be added to the targeted list, this test is verified in
 * order to valid the target.
 * 
 * @see net.sf.firemox.action.Actiontype
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 new algorythme to determine valid targets
 * @since 0.86 a target cannot be added twice in the target-list
 */
public class TargetManager {

	/**
	 * Highlight the given targets and the corresponding zones.
	 * 
	 * @param validTargets
	 *          the valid targets.
	 */
	public void manageTargets(List<Target> validTargets) {
		final boolean[] highlightedZones = new boolean[IdZones.NB_ZONE * 2];
		Target.targetize(validTargets, highlightedZones);
		if (this.validTargets != null) {
			this.validTargets.clear();
		}
		this.validTargets = validTargets;
		zones.clear();
		for (int i = highlightedZones.length; i-- > 0;) {
			if (highlightedZones[i]) {
				final MZone highlightZone = StackManager.PLAYERS[i / IdZones.NB_ZONE].zoneManager
						.getContainer(i % IdZones.NB_ZONE);
				zones.add(highlightZone);
				highlightZone.highLight(TargetFactory.TARGET_COLOR);
			}
		}
	}

	/**
	 * Indicates that we're no longer looking for a target
	 */
	public void clearTarget() {
		if (validTargets != null) {
			Clickable.disHighlight(validTargets);
			validTargets.clear();
			validTargets = null;
		}
		for (MZone zone : zones) {
			zone.disHighLight();
		}
		zones.clear();
	}

	/**
	 * The current list of Zones containing highlighted targets.
	 */
	private final List<MZone> zones = new ArrayList<MZone>();

	/**
	 * The current list of valid targets. Is null if there was no choice.
	 */
	private List<Target> validTargets = null;

}