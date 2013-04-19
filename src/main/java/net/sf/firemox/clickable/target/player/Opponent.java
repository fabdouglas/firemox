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
package net.sf.firemox.clickable.target.player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.firemox.clickable.mana.ManaPool;
import net.sf.firemox.network.Synchronizer;
import net.sf.firemox.stack.MPhase;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class Opponent extends Player {

	/**
	 * Creates a new instance of Opponent <br>
	 * 
	 * @param zoneManager
	 *          the zoneManager of this player
	 * @param morePanel
	 *          the panel containing player info.
	 */
	public Opponent(ZoneManager zoneManager, JPanel morePanel) {
		super(1, new ManaPool(true), zoneManager, morePanel);
	}

	@Override
	public void setHandedPlayer() {
		super.setHandedPlayer();
		MagicUIComponents.waitingLabel.setForeground(Color.RED);
		Synchronizer.waitOpponent();
	}

	@Override
	public void initUI() {
		super.initUI();
		nickName = new JLabel("??");
		nickNamePanel.add(nickName);
		updateReversed();
	}

	@Override
	public void resetPhases(MPhase[] phases) {
		if (Configuration.getBoolean("reverseSide", false)) {
			this.phases.removeAll();
			for (MPhase phase : phases) {
				this.phases.add(phase);
			}
			this.phases.doLayout();
		} else {
			super.resetPhases(phases);
		}
	}

	@Override
	public boolean isYou() {
		return false;
	}

	@Override
	public void updateReversed() {
		mainPanel.remove(phases);
		mainPanel.remove(infoPanel);
		infoPanel.removeAll();
		phases.removeAll();
		mana.updateReversed();

		final JScrollPane handS = (JScrollPane) zoneManager.hand.getParent()
				.getParent();
		if (Configuration.getBoolean("reverseSide", false)) {
			infoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
			infoPanel.add(mana);
			infoPanel.add(poisonLabel);
			infoPanel.add(lifeLabel);
			infoPanel.add(this);
			mainPanel.add(phases, BorderLayout.EAST);
			handSplitter.setLeftComponent(handS);
			handSplitter.setRightComponent(zoneManager.play);
		} else {
			infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			infoPanel.add(this);
			infoPanel.add(lifeLabel);
			infoPanel.add(poisonLabel);
			infoPanel.add(mana);
			mainPanel.add(phases, BorderLayout.WEST);
			handSplitter.setRightComponent(handS);
			handSplitter.setLeftComponent(zoneManager.play);
		}
		mainPanel.add(infoPanel, BorderLayout.NORTH);
	}

	/**
	 * @param nickName
	 *          the current player's nickname.
	 * @param opponentNickname
	 *          the opponent player's nickname.
	 * @param inBin
	 *          the input stream the settings will be read from.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void readSettings(String nickName, String opponentNickname,
			InputStream inBin) throws IOException {
		String nickNameInt = nickName;
		if (nickNameInt.equals(opponentNickname)) {
			// both players have the same name, so we change the opponent's one
			nickNameInt = nickNameInt + "[1]";
		}

		// read real name
		String realName = MToolKit.readString(inBin);

		if (realName.length() > 0) {
			// the real name is shared
			this.nickName.setText(nickNameInt + "[" + realName + "]");
		} else {
			this.nickName.setText(nickNameInt + "[-]");
		}

		// read opponent's avatar icon
		init(MToolKit.readImage(inBin));

		// read opponent's avatar picture
		avatarButton.setIcon(MToolKit.readImageIcon(inBin));
		avatarButton.setMinimumSize(new Dimension(1, 180));

		// read string settings
		togglePanel.removeAll();
		for (String setting : SETTINGS) {
			String value = MToolKit.readString(inBin);
			if (setting.length() != 0) {
				// this setting is shared (public and filled)
				JLabel label = new JLabel(UIHelper.getIcon(setting + IdConst.TYPE_PIC));
				label.setToolTipText("<html>" + LanguageManager.getString(setting)
						+ " : " + value);
				label.setPreferredSize(new Dimension(20, 20));
				togglePanel.add(label);
				label = null;
			}
		}

		// read wallpapers
		for (MZone zone : zoneManager.getValidTargetZones()) {
			if (!zone.isShared()) {
				zone.readWallPaperConfiguration(inBin);
			}
		}

		togglePanel.doLayout();
		morePanel.doLayout();
	}

	@Override
	public String getNickName() {
		return nickName.getText();
	}

	/**
	 * The label containing the nick name of this player.
	 */
	private JLabel nickName;

}
