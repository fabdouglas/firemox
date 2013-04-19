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
package net.sf.firemox.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.clickable.target.player.Opponent;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.clickable.target.player.You;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.ui.layout.DivideLayout;
import net.sf.firemox.zone.ExpandableZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class TableTop {

	/**
	 * Tabbed zone panel.
	 */
	public final JTabbedPane tabbedPane;

	/**
	 * The full table component
	 */
	private final JPanel gameFullPane;

	/**
	 * The component representing the table : plays + hands
	 */
	public final JPanel tablePanel;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param settingRes
	 *          is the settings file where settings are loaded from.
	 * @param playerTabbedPanel
	 *          the panel where player related panels will be added.
	 * @param previewTabbedPanel
	 *          the panel containing chat, log, preview panel
	 */
	private TableTop(JTabbedPane playerTabbedPanel, JTabbedPane previewTabbedPanel) {
		// your "more info" panel
		TableTop.instance = this;
		final JPanel youMorePanel = new JPanel(new BorderLayout());
		playerTabbedPanel.add(youMorePanel, UIHelper.getIcon("you.gif"));
		playerTabbedPanel.setToolTipTextAt(playerTabbedPanel
				.indexOfComponent(youMorePanel), "<html>"
				+ LanguageManager.getString("youmore.tooltip") + "<br><br>"
				+ MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("youmoreTTtip"));

		// the opponent's "more info" panel
		final JPanel opponentMorePanel = new JPanel(new BorderLayout());
		playerTabbedPanel.add(opponentMorePanel, UIHelper.getIcon("opponent.gif"));
		playerTabbedPanel.setToolTipTextAt(playerTabbedPanel
				.indexOfComponent(opponentMorePanel), "<html>"
				+ LanguageManager.getString("opponentmore.tooltip"));

		// tabbed panels : stack, opponent TBZ, your TBZ, side
		tabbedPane = new JTabbedPane(SwingConstants.BOTTOM,
				JTabbedPane.SCROLL_TAB_LAYOUT);

		// initialize player and zone managers

		// YOU

		// HAND
		final JScrollPane youHandSPanel = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		youHandSPanel.setMinimumSize(new Dimension(90, 60));
		youHandSPanel.getVerticalScrollBar().setBlockIncrement(45);
		youHandSPanel.getVerticalScrollBar().setUnitIncrement(45);
		youHandSPanel.setOpaque(false);

		// OPPONENT

		// HAND
		final JScrollPane opponentHandSPanel = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		opponentHandSPanel.setMinimumSize(new Dimension(90, 60));
		opponentHandSPanel.getVerticalScrollBar().setBlockIncrement(45);

		final JScrollPane stackSPanel = new JScrollPane();
		final JScrollPane youTriggeredSPanel = new JScrollPane();
		final JScrollPane opponentTriggeredSPanel = new JScrollPane();
		final JScrollPane youDelayedSPanel = new JScrollPane();
		final JScrollPane opponentDelayedSPanel = new JScrollPane();
		final JScrollPane sideSPanel = new JScrollPane();

		tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(new EtchedBorder());

		// Initialize the zones manager
		ZoneManager.init(sideSPanel, stackSPanel);

		final JPanel youAdditionalZones = new JPanel(new BorderLayout());
		final JPanel opponentAdditionalZones = new JPanel(new BorderLayout());

		// create players
		StackManager.PLAYERS[0] = new You(new ZoneManager(0, youHandSPanel,
				youTriggeredSPanel, youDelayedSPanel, youAdditionalZones), youMorePanel);
		StackManager.PLAYERS[1] = new Opponent(new ZoneManager(1,
				opponentHandSPanel, opponentTriggeredSPanel, opponentDelayedSPanel,
				opponentAdditionalZones), opponentMorePanel);

		final JFlipFlapPanel commonPanel = new JFlipFlapPanel(playerTabbedPanel,
				previewTabbedPanel, tabbedPane, youAdditionalZones,
				opponentAdditionalZones);
		MagicUIComponents.magicForm.getContentPane().add(commonPanel,
				BorderLayout.WEST);
		MagicUIComponents.magicForm.getContentPane().add(tablePanel,
				BorderLayout.CENTER);
		gameFullPane = new JPanel(null);
		gameFullPane.setBorder(null);
		gameFullPane.setOpaque(false);
		gameFullPane.setLayout(new DivideLayout(gameFullPane));

		// Initialize UIs
		StackManager.PLAYERS[0].initUI();
		StackManager.PLAYERS[1].initUI();

		// move the sides from JSplit to the common panel
		gameFullPane.add(StackManager.PLAYERS[1].mainPanel);
		gameFullPane.add(StackManager.PLAYERS[0].mainPanel);
		tablePanel.add(gameFullPane, BorderLayout.CENTER);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param playerTabbedPanel
	 *          the panel where player related panels will be added.
	 * @param previewTabbedPanel
	 *          the panel containing chat, log, preview panel
	 */
	public static void init(JTabbedPane playerTabbedPanel,
			JTabbedPane previewTabbedPanel) {
		new TableTop(playerTabbedPanel, previewTabbedPanel);
	}

	/**
	 * Initialize zones depending on the current TBS.
	 */
	public void initTbs() {
		for (Player player : StackManager.PLAYERS) {
			initTbs(player);
		}
		EventManager.initTbsUI();
	}

	/**
	 * Add the specified additional zones around the stack & TBZ zones.
	 * 
	 * @param player
	 *          the player to initialize zones.
	 */
	private void initTbs(Player player) {
		final Collection<ExpandableZone> additionalZones = player.zoneManager.additionalZones;
		final JScrollPane delayedSPanel = player.zoneManager.delayedBuffer.superPanel;
		final JScrollPane triggeredSPanel = player.zoneManager.triggeredBuffer.superPanel;
		if (player.isYou()) {
			// COMMON : STACK
			final JScrollPane stackSPanel = ZoneManager.stack.superPanel;
			tabbedPane.add(stackSPanel, UIHelper.getTbsIcon("zones/stack.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(stackSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.stack.tooltip"));

			// YOU : TBZ
			tabbedPane.add(triggeredSPanel, UIHelper
					.getTbsIcon("zones/triggered_you.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(triggeredSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.yourtbz.tooltip")
							+ "<br><br>" + MagicUIComponents.HTML_ICON_TIP
							+ LanguageManagerMDB.getString("zone.yourtbzTTtip") + "<br>"
							+ MagicUIComponents.HTML_ICON_TIP
							+ LanguageManagerMDB.getString("zone.yourtbzTTtip2"));

			// YOU : DBZ
			tabbedPane.add(delayedSPanel, UIHelper
					.getTbsIcon("zones/delayed_you.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(delayedSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.youdbz.tooltip"));
		} else {

			// OPPONENT : TBZ
			tabbedPane.add(triggeredSPanel, UIHelper
					.getTbsIcon("zones/triggered_opponent.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(triggeredSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.opponenttbz.tooltip"));

			// OPPONENT : DBZ
			tabbedPane.add(delayedSPanel, UIHelper
					.getTbsIcon("zones/delayed_opponent.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(delayedSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.opponentdbz.tooltip"));
		}

		// Additional zones
		for (ExpandableZone additionalZone : additionalZones) {
			final JScrollPane zoneSPanel = additionalZone.superPanel;
			if (!additionalZone.canBeGathered()) {
				if (player.isYou()) {
					tabbedPane.add(zoneSPanel, UIHelper.getTbsIcon("zones/"
							+ additionalZone.getZoneName() + "_you.gif"));
				} else {
					tabbedPane.add(zoneSPanel, UIHelper.getTbsIcon("zones/"
							+ additionalZone.getZoneName() + "_opponent.gif"));
				}
				tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(zoneSPanel),
						"<html>" + additionalZone);
			}
		}

		// COMMON : SIDE
		if (!player.isYou()) {
			final JScrollPane sideSPanel = ZoneManager.side.superPanel;
			tabbedPane.add(sideSPanel, UIHelper.getTbsIcon("zones/side.gif"));
			tabbedPane.setToolTipTextAt(tabbedPane.indexOfComponent(sideSPanel),
					"<html>" + LanguageManagerMDB.getString("zone.side.tooltip"));
		}
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static TableTop getInstance() {
		if (instance == null) {
			Log.fatal("Table top is not yet initialized");
		}
		return instance;
	}

	/**
	 * Unique instance of this class.
	 */
	private static transient TableTop instance;

}
