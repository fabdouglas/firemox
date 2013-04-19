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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.ObjectArray;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.token.VisibilityChange;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * Set the player's zone visible/hidden to him/opponent. This action use the
 * target list to determine the zone owner. The 'toOwner' field is there to
 * determine whom this zone will be shown/hidden. If this attribute is 'true',
 * the visibility to targeted player will be modified. Otherwise, the visibility
 * of opponent of player will be modified.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 */
class ZoneVisibility extends UserAction implements StandardAction,
		FollowAction, ChosenAction {

	/**
	 * Create an instance of ZoneVisibility by reading a file Offset's file must
	 * pointing on the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>zone identifier [1]</li>
	 * <li>show=1,hide=0 [1]</li>
	 * <li>to him=0,to opponent=1,to you=2 [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @param card
	 *          owning this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ZoneVisibility(InputStream inputFile) throws IOException {
		super(inputFile);
		idZone = inputFile.read();
		visible = inputFile.read() != 0;
		idFor = VisibilityChange.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ZONE_VISIBILITY;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		// set the visibility of card for the targeted player
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isPlayer()) {
				final Player player = (Player) StackManager.getInstance()
						.getTargetedList().get(i);
				if (visible) {
					player.zoneManager.getContainer(idZone).increaseFor(player, idFor);
				} else {
					player.zoneManager.getContainer(idZone).decreaseFor(player, idFor);
				}
			}
		}
		return true;
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		ObjectArray<Visibility> contextN = new ObjectArray<Visibility>(StackManager
				.getInstance().getTargetedList().size() * 2);
		actionContext.actionContext = contextN;
		// set the visibility of card for the targeted player
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isPlayer()) {
				final Player player = (Player) StackManager.getInstance()
						.getTargetedList().get(i);
				final MZone zone = player.zoneManager.getContainer(idZone);
				final Visibility visibility = zone.getVisibility();
				contextN.setObject(i, visibility);
				if (visible) {
					zone.increaseFor(player, idFor);
				} else {
					zone.decreaseFor(player, idFor);
				}
				contextN.setObject(2 * i + 1, zone.getVisibility());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		ObjectArray<Visibility> contextN = (ObjectArray<Visibility>) actionContext.actionContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isPlayer()) {
				final Player player = (Player) StackManager.getInstance()
						.getTargetedList().get(i);
				player.zoneManager.getContainer(idZone).setVisibility(
						contextN.getObject(2 * i));
			}
		}
	}

	@Override
	public String toString(Ability ability) {
		switch (idFor) {
		case controller:
			return LanguageManagerMDB.getString(visible ? "reveal-to-player"
					: "hide-to-player", LanguageManagerMDB.getString("zone."
					+ ZoneManager.getZoneName(idZone)));
		case opponent:
			return LanguageManagerMDB.getString(visible ? "reveal-to-opponent"
					: "hide-to-opponent", LanguageManagerMDB.getString("zone."
					+ ZoneManager.getZoneName(idZone)));
		default:
			return LanguageManagerMDB.getString(visible ? "look" : "hide",
					LanguageManagerMDB.getString("zone."
							+ ZoneManager.getZoneName(idZone)));
		}
	}

	public boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		simulate(actionContext, context, ability);
		return true;
	}

	public void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// Nothing to do
	}

	@SuppressWarnings("unchecked")
	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		ObjectArray<Visibility> contextN = (ObjectArray<Visibility>) actionContext.actionContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isPlayer()) {
				final Player player = (Player) StackManager.getInstance()
						.getTargetedList().get(i);
				player.zoneManager.getContainer(idZone).setVisibility(
						contextN.getObject(i * 2 + 1));
			}
		}
		return true;
	}

	public String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		return toString(ability);
	}

	/**
	 * Indicates if the zone of the list of players would be hidden to his/her
	 * opponent.
	 */
	private boolean visible;

	/**
	 * Indicates the zone identifier to show/hide.
	 */
	private int idZone;

	/**
	 * Indicates if the zone would be hidden/shown to the targeted player, to the
	 * opponent of targeted player, or to you
	 */
	private VisibilityChange idFor;

}