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

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import net.sf.firemox.action.BackgroundMessaging;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.Wizard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class MessageButton extends JButton implements ActionListener {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param enabledPicture1
	 * @param enabledPicture2
	 */
	public MessageButton(ImageIcon enabledPicture1, ImageIcon enabledPicture2) {
		this.enabledPicture1 = enabledPicture1;
		this.enabledPicture2 = enabledPicture2;
		addActionListener(this);
	}

	/**
	 * Show the button enabling to restore the background wizard.
	 * 
	 * @param context
	 *          the context of wizard put in background.
	 * @param ability
	 *          the ability of wizard put in background.
	 * @param action
	 *          the action of wizard put in background.
	 * @param wizard
	 *          the wizard put in background.
	 */
	public void startButton(ContextEventListener context, Ability ability,
			BackgroundMessaging action, Wizard wizard) {
		this.context = context;
		this.ability = ability;
		this.action = action;
		this.wizard = wizard;
		state = 0;
		setVisible(true);
		setToolTipText(LanguageManager.getString("wiz_recallmsgTTenabled"));
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * Hidde the button put in background.
	 */
	public void stopButton() {
		state = 0;
		setVisible(false);
		setToolTipText(LanguageManager.getString("wiz_recallmsgTTdisabled"));
	}

	public void actionPerformed(ActionEvent e) {
		// display hidden message box and disable this button
		stopButton();
		action.replayAction(context, ability, wizard);
	}

	/**
	 * Display the next picture of this button.
	 */
	public void nextPicture() {
		state = ++state % 2;
		if (isVisible()) {
			if (state == 0) {
				setIcon(enabledPicture1);
			} else {
				setIcon(enabledPicture2);
			}
		}
	}

	private ImageIcon enabledPicture1;

	private ImageIcon enabledPicture2;

	private int state;

	private ContextEventListener context;

	private Ability ability;

	private BackgroundMessaging action;

	private Wizard wizard;
}
