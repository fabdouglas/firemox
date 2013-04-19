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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.clickable.mana.ManaPool;
import net.sf.firemox.network.Synchronizer;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.Account;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class You extends Player {

	private static final String CONFIG_AVATAR_ICON = "images/sample_avatar32.jpg";

	private static final String CONFIG_KEY_AVATAR_ICON = "avatarIconFile";

	private static final String CONFIG_AVATAR = "images/sample_avatar.jpg";

	private static final String CONFIG_KEY_AVATAR = "avatarFile";

	private static final String CONFIG_NICKNAME = "Player";

	private static final String CONFIG_KEY_NICKNAME = "nickName";

	private static final String CONFIG_REALNAME = "";

	private static final String CONFIG_KEY_REALNAME = "realName";

	/**
	 * Creates a new instance of You <br>
	 * 
	 * @param zoneManager
	 *          the zoneManager of this player
	 * @param morePanel
	 *          the panel containing player info.
	 */
	public You(ZoneManager zoneManager, JPanel morePanel) {
		super(0, new ManaPool(false), zoneManager, morePanel);
		init(Toolkit.getDefaultToolkit().createImage(
				Configuration.getString(CONFIG_KEY_AVATAR_ICON, CONFIG_AVATAR_ICON)));
	}

	@Override
	public void updateReversed() {
		// Nothing to do for this player
	}

	@Override
	public void initUI() {
		super.initUI();
		infoPanel.add(this);
		infoPanel.add(lifeLabel);
		infoPanel.add(poisonLabel);
		infoPanel.add(mana);
		mainPanel.add(phases, BorderLayout.WEST);
		mainPanel.add(infoPanel, BorderLayout.SOUTH);

		final JScrollPane handS = (JScrollPane) zoneManager.hand.getParent()
				.getParent();
		handSplitter.setRightComponent(handS);
		handSplitter.setLeftComponent(zoneManager.play);

		// nickname
		nickNameBtn = new JButton(Configuration.getString(CONFIG_KEY_NICKNAME,
				CONFIG_NICKNAME));
		nickNameBtn.setMaximumSize(new Dimension(200, 20));
		nickNameBtn.setBorder(new EtchedBorder());
		nickNameBtn.addActionListener(this);
		nickNameBtn.setActionCommand(CONFIG_KEY_NICKNAME);
		nickNamePanel.add(nickNameBtn);

		infoPanel.add(MagicUIComponents.skipButton);
		infoPanel.add(new JLabel("  "));
		infoPanel.add(MagicUIComponents.waitingLabel);
		infoPanel.add(MagicUIComponents.backgroundBtn);
		avatarButton.setIcon(new ImageIcon(Configuration.getString(
				CONFIG_KEY_AVATAR, CONFIG_AVATAR)));
		toggleButtons = new JButton[SETTINGS.length];
		for (int i = SETTINGS.length; i-- > 0;) {
			toggleButtons[i] = new JButton();
			toggleButtons[i].addActionListener(this);
			toggleButtons[i].setActionCommand(SETTINGS[i]);
			toggleButtons[i].setPreferredSize(new Dimension(20, 20));
			actionPerformed(new ActionEvent(toggleButtons[i], NO_PROMPT, SETTINGS[i]));
			togglePanel.add(toggleButtons[i]);
		}

		actionPerformed(new ActionEvent(nickNameBtn, NO_PROMPT, CONFIG_KEY_NICKNAME));
		// real name
		realNamePanel.setVisible(true);
		realNameBtn = new JButton(Configuration.getString(CONFIG_KEY_REALNAME,
				CONFIG_REALNAME));
		realNameBtn.setBorder(new EtchedBorder());
		realNameBtn.setMaximumSize(new Dimension(200, 20));
		realNameBtn.addActionListener(this);
		realNameBtn.setActionCommand(CONFIG_KEY_REALNAME);
		realNamePanel.add(realNameBtn);
		actionPerformed(new ActionEvent(realNameBtn, NO_PROMPT, CONFIG_KEY_REALNAME));
		// MagicUIComponents.gameSplitPane.setBottomComponent(mainPanel);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source instanceof JButton) {
			JButton account = (JButton) source;
			String name = evt.getActionCommand();
			if (evt.getID() != NO_PROMPT) {
				new Account(name).setVisible(true);
			}
			if (CONFIG_KEY_NICKNAME.equals(name) || CONFIG_KEY_REALNAME.equals(name)) {
				// update the name
				account.setText(Configuration.getString(name));
			} else {
				account.setIcon(UIHelper.getIcon((Configuration.getBoolean(name
						+ "public", false) ? "" : "d")
						+ name + IdConst.TYPE_PIC));
			}
			StringBuilder builder = new StringBuilder();
			builder.append("<html>");
			builder.append(LanguageManager.getString(name));
			builder.append(" : ");
			builder.append(Configuration.getString(name));
			builder.append("<br>");
			builder.append(LanguageManager.getString("ispublic"));
			builder.append(" : ");
			builder.append(Configuration.getBoolean(name + "public", false));
			builder.append("<br>");
			builder.append(MagicUIComponents.HTML_ICON_TIP);
			builder.append(LanguageManager.getString("clicToModify"));
			account.setToolTipText(builder.toString());
		} else {
			super.actionPerformed(evt);
		}
	}

	@Override
	public boolean isYou() {
		return true;
	}

	@Override
	public void setHandedPlayer() {
		super.setHandedPlayer();
		MagicUIComponents.waitingLabel.setForeground(Color.GREEN);
		Synchronizer.setAsHanded();
	}

	/**
	 * Indicates whether your real name is public or not
	 * 
	 * @return true if your real name is public or not
	 */
	private boolean isRealNameIsPublic() {
		return Configuration.getBoolean("realnamepublic", true);
	}

	/**
	 * Senf the setting over the given output stream.
	 * 
	 * @param outBin
	 *          the stream containg the settings of this player.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void sendSettings(OutputStream outBin) throws IOException {
		// write our real name
		MToolKit.writeString(outBin, isRealNameIsPublic() ? realNameBtn.getText()
				: "");

		// write our avatar icon (32x32)
		MToolKit.writeFile(MToolKit.getFile(Configuration.getString(
				CONFIG_KEY_AVATAR_ICON, CONFIG_AVATAR_ICON)), outBin);
		// write our avatar picture
		MToolKit.writeFile(MToolKit.getFile(Configuration.getString(
				CONFIG_KEY_AVATAR, CONFIG_AVATAR)), outBin);

		// write string settings
		for (String setting : SETTINGS) {
			Boolean isPublic = Configuration.getBoolean(setting + "public", false);
			MToolKit.writeString(outBin, isPublic ? Configuration.getString(setting)
					: "");
		}

		// write wallpapers
		for (MZone zone : zoneManager.getValidTargetZones()) {
			if (!zone.isShared()) {
				zone.writeWallPaperConfiguration(outBin);
			}
		}
	}

	@Override
	public String getNickName() {
		return nickNameBtn.getText();
	}

	/**
	 * The toggle buttons representing the editable string settings.
	 */
	private JButton[] toggleButtons;

	/**
	 * The button containing the nick name of this player.
	 */
	private JButton nickNameBtn;

	/**
	 * The button containing the real name of this player.
	 */
	private JButton realNameBtn;

	/**
	 * Private id used to update button graphics
	 */
	private static final int NO_PROMPT = 666;

}
