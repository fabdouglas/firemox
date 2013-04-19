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
package net.sf.firemox.zone;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.DeckReader;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.MUIManager;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.JExpandedPanel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54.17
 */
public class ZoneManager {

	/**
	 * Available zones by id.
	 */
	private Map<Integer, MZone> zonesById;

	/**
	 * Additional zones of game.
	 */
	static ZoneConfiguration[] additionalZoneConfigurations;

	/**
	 * additional zones of this player
	 */
	public final List<ExpandableZone> additionalZones;

	/**
	 * The initial zone's id where the cards of loaded deck go.
	 */
	static int defaultZoneId;

	/**
	 * This is the current expanded zone. Is null if no zone is expanded.
	 */
	public static ExpandableZone expandedZone;

	/**
	 * This is the current expanded zone. Is null if no zone is expanded.
	 */
	static JPanel expandedPanel;

	private final JPanel additionalZonesPanel;

	/**
	 * All available zones.
	 */
	private final List<MZone> validTargetZones;

	/**
	 * Creates an new instance of ZoneManager
	 * 
	 * @param idPlayer
	 *          id of associated player
	 * @param handSPanel
	 * @param triggeredSPanel
	 * @param delayedSPanel
	 * @param additionalZonesPanel
	 */
	public ZoneManager(int idPlayer, JScrollPane handSPanel,
			JScrollPane triggeredSPanel, JScrollPane delayedSPanel,
			JPanel additionalZonesPanel) {
		final boolean rev = idPlayer == 1;
		this.additionalZonesPanel = additionalZonesPanel;
		this.play = new Play(rev);
		this.hand = new Hand(handSPanel, rev);
		this.triggeredBuffer = new TriggeredBuffer(triggeredSPanel, rev);
		this.delayedBuffer = new DelayedBuffer(delayedSPanel, rev);
		this.idPlayer = idPlayer;
		this.additionalZones = new ArrayList<ExpandableZone>();
		this.validTargetZones = new ArrayList<MZone>();
		this.zonesById = new HashMap<Integer, MZone>();
		this.zonesById.put(IdZones.PLAY, play);
		this.zonesById.put(IdZones.HAND, hand);
		this.zonesById.put(IdZones.TRIGGERED, triggeredBuffer);
		this.zonesById.put(IdZones.DELAYED, delayedBuffer);
		this.zonesById.put(IdZones.STACK, stack);
		this.zonesById.put(IdZones.SIDE, side);
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>play sectors [ZoneSector[]]</li>
	 * <li>additional zones [ZoneConfiguration[]]</li>
	 * <li>default Zone id [integer]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          the stream containing the layout configuration of this zone for a
	 *          specified layout
	 * @throws IOException
	 *           error during the configuration read.
	 */
	private void initTbsImpl() {
		reset();
		additionalZones.clear();
		validTargetZones.clear();
		additionalZonesPanel.removeAll();
		final boolean rev = idPlayer == 1;
		for (ZoneConfiguration zoneConfiguration : additionalZoneConfigurations) {
			final JScrollPane sPanel = new JScrollPane(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			ExpandableZone zone = new ExpandableZone(zoneConfiguration, sPanel, rev);
			additionalZones.add(zone);
			if (zone.canBeGathered()) {
				sPanel.setPreferredSize(new Dimension((IdConst.STD_WIDTH + 10) / 2,
						CardFactory.cardHeight + 10));
			}
		}
		validTargetZones.add(play);
		validTargetZones.add(hand);
		validTargetZones.add(stack);
		validTargetZones.add(side);

		for (ExpandableZone zone : additionalZones) {
			if (zone.canBeGathered()) {
				if (!rev || !Configuration.getBoolean("reverseSide", false)) {
					additionalZonesPanel.add(zone.superPanel, zone.zoneConfiguration
							.getLayoutConstraintYou());
				} else {
					additionalZonesPanel.add(zone.superPanel, zone.zoneConfiguration
							.getLayoutConstraintOpponent());
				}
			}
			this.zonesById.put(zone.getZoneId(), zone);
			validTargetZones.add(zone);
		}
	}

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>play sectors [ZoneSector[]]</li>
	 * <li>additional zones [ZoneConfiguration[]]</li>
	 * <li>default Zone id [integer]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          the stream containing the layout configuration of this zone for a
	 *          specified layout
	 * @throws IOException
	 *           error during the configuration read.
	 */
	public static void initTbs(InputStream inputStream) throws IOException {
		additionalZoneConfigurations = new ZoneConfiguration[inputStream.read()];
		for (int i = 0; i < additionalZoneConfigurations.length; i++) {
			additionalZoneConfigurations[i] = new ZoneConfiguration(inputStream, i
					+ IdZones.FIRST_ADDITIONAL_ZONE);
		}
		defaultZoneId = inputStream.read();
		for (Player player : StackManager.PLAYERS) {
			player.zoneManager.initTbsImpl();
		}
	}

	/**
	 * return the container corresponding to the specified idZone
	 * 
	 * @param idZone
	 *          zone identifier of matched container
	 * @return the container of a specified idZone
	 */
	public MZone getContainer(int idZone) {
		return zonesById.get(idZone);
	}

	/**
	 * 
	 */
	public static void updateReversed() {
		if (StackManager.PLAYERS[1] != null) {
			for (Player player : StackManager.PLAYERS) {
				final ZoneManager zoneManager = player.zoneManager;
				zoneManager.play.updateReversed();
				zoneManager.hand.updateReversed();
				boolean rev = zoneManager.idPlayer == 1;
				if (!zoneManager.additionalZones.isEmpty()) {
					zoneManager.additionalZonesPanel.removeAll();
					for (ExpandableZone zone : zoneManager.additionalZones) {
						if (zone.canBeGathered()) {
							if (rev || !Configuration.getBoolean("reverseSide", false)) {
								zoneManager.additionalZonesPanel.add(zone.superPanel,
										zone.zoneConfiguration.getLayoutConstraintYou());
							} else {
								zoneManager.additionalZonesPanel.add(zone.superPanel,
										zone.zoneConfiguration.getLayoutConstraintOpponent());
							}
							zone.updateReversed();
						}
					}
				}
			}
		}
	}

	/**
	 * Set the zone attribute to their default value, and remove all components
	 */
	public void reset() {
		hand.reset();
		play.reset();
		stack.reset();
		delayedBuffer.reset();
		triggeredBuffer.reset();
		for (MZone zone : additionalZones) {
			zone.reset();
		}
	}

	/**
	 * Checks all cards corresponding to the specified constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return amount of card matching with the specified test
	 */
	public int countAllCardsOf(Test test, Ability ability) {
		int result = 0;
		for (MZone zone : validTargetZones) {
			result += zone.countAllCardsOf(test, ability);
		}
		return result;
	}

	/**
	 * Checks all cards corresponding to the specified constraints.
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param limit
	 *          is the desired count.
	 * @param canBePreempted
	 *          <code>true</code> if the valid targets can be determined before
	 *          runtime.
	 * @return amount of cards matching with the specified test. Highest value is
	 *         <code>limit</code>.
	 */
	public int countAllCardsOf(Test test, Ability ability, int limit,
			boolean canBePreempted) {
		int result = 0;
		for (MZone zone : validTargetZones) {
			if (result >= limit) {
				break;
			}
			result += zone.countAllCardsOf(test, ability, limit - result,
					canBePreempted);
		}
		return result;
	}

	/**
	 * Update some optimization, painter,... etc depending on the current Look And
	 * Feel.
	 */
	public static void updateLookAndFeel() {
		if (MUIManager.LF_SUBSTANCE_CLASSNAME.equals(UIManager.getLookAndFeel()
				.getClass().getName()))
			bgDelegate = new Object();
		else {
			bgDelegate = null;
		}
	}

	/**
	 * Checks all cards corresponding to this constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param list
	 *          the list containing the founded cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 */
	public void checkAllCardsOf(Test test, List<Target> list, Ability ability) {
		for (MZone zone : validTargetZones) {
			zone.checkAllCardsOf(test, list, ability);
		}
	}

	/**
	 * Fill the player's initialization zone with cards found in the given
	 * InputFile
	 * 
	 * @param deck
	 *          the deck of this player.
	 * @param dbFile
	 *          opened file read containing the available cards
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void giveCards(Deck deck, FileInputStream dbFile) throws IOException {
		MZone giveToZone = getContainer(defaultZoneId);
		giveToZone.setVisible(false);
		DeckReader.fillZone(deck, dbFile, getContainer(defaultZoneId),
				StackManager.PLAYERS[idPlayer]);
		giveToZone.setVisible(true);
	}

	/**
	 * Save wallpaper name and back colors of this panel to a specified output
	 * stream.
	 */
	private void saveSettingsInternal() {
		play.saveSettings();
		hand.saveSettings();
		triggeredBuffer.saveSettings();
		delayedBuffer.saveSettings();
		for (MZone zone : additionalZones) {
			zone.saveSettings();
		}
	}

	/**
	 * @param sideSPanel
	 * @param stackSPanel
	 */
	public static void init(JScrollPane sideSPanel, JScrollPane stackSPanel) {
		side = new Side(sideSPanel);
		stack = new Stack(stackSPanel);
		expandedPanel = new JExpandedPanel();
		expandedPanel.setVisible(false);
	}

	/**
	 * Initialize UI of zones
	 */
	public void initUI() {
		MagicUIComponents.magicForm.getContentPane().add(expandedPanel, 0);
		for (MZone zone : zonesById.values()) {
			zone.initUI();
		}
	}

	/**
	 * Save editable string settings of zones.
	 */
	public static void saveSettings() {
		StackManager.PLAYERS[0].zoneManager.saveSettingsInternal();
		StackManager.PLAYERS[1].zoneManager.saveSettingsInternal();
		side.saveSettings();
		stack.saveSettings();
	}

	/**
	 * Return the zone name corresponding to the given zone id.
	 * 
	 * @param idZone
	 *          the zone id.
	 * @return the zone name corresponding to the given zone id.
	 */
	public static String getZoneName(int idZone) {
		return StackManager.PLAYERS[0].zoneManager.getContainer(idZone)
				.getZoneName();
	}

	/**
	 * Return All available zones.
	 * 
	 * @return All available zones.
	 */
	public List<MZone> getValidTargetZones() {
		return validTargetZones;
	}

	/**
	 * hand of this player
	 */
	public Hand hand;

	/**
	 * play zone of this player
	 */
	public Play play;

	/**
	 * triggered buffer of this player (TBZ)
	 */
	public TriggeredBuffer triggeredBuffer;

	/**
	 * delayed buffer of this player (DBZ)
	 */
	public DelayedBuffer delayedBuffer;

	/**
	 * shared stack of players
	 */
	public static Stack stack;

	/**
	 * shared side of players
	 */
	public static Side side;

	/**
	 * Is the controller of this zone manager
	 */
	private int idPlayer;

	/**
	 * The optional Substance background painter.
	 */
	static Object bgDelegate;

}