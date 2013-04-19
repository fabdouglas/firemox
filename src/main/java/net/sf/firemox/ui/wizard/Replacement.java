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
package net.sf.firemox.ui.wizard;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Replacement extends Ok implements MouseListener {

	/**
	 * Creates a new instance of Replacement <br>
	 * 
	 * @param eventName
	 * @param triggeredCards
	 */
	public Replacement(String eventName,
			List<? extends AbstractCard> triggeredCards) {
		super(LanguageManager.getString("wiz_replacement.title"), LanguageManager
				.getString("wiz_replacement.description", eventName),
				"wiz_replacement.gif", null, 600, 400);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		JScrollPane scroll = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		container = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for (int i = 0; i < triggeredCards.size(); i++) {
			container.add(triggeredCards.get(i));
			triggeredCards.get(i).addMouseListener(this);
		}
		isRunning = true;
		replacement = -1;
		scroll.setAutoscrolls(true);
		scroll.setViewportView(container);
		gameParamPanel.add(scroll);
	}

	/**
	 * triggers the ok button
	 * 
	 * @param event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			if (replacement != -1) {
				isRunning = false;
				validAnswer(JOptionPane.OK_OPTION);
			}
		} else {
			super.actionPerformed(event);
		}
	}

	public void mouseClicked(MouseEvent e) {
		TriggeredCard triggered = (TriggeredCard) e.getSource();
		MagicUIComponents.logListing.append(triggered.triggeredAbility.getCard()
				.getController().idPlayer, "Chosen replacement : " + triggered);
		isRunning = false;
		for (int i = container.getComponentCount(); i-- > 0;) {
			if (container.getComponent(i) == triggered) {
				replacement = i;
				break;
			}
		}
		setVisible(false);
		dispose();
	}

	public void mousePressed(MouseEvent e) {
		// Ignore this event
	}

	public void mouseReleased(MouseEvent e) {
		// Ignore this event
	}

	public void mouseEntered(MouseEvent e) {
		// Ignore this event
	}

	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * The chosen replacement ability index.
	 */
	public static int replacement;

	/**
	 * Indicates we are waiting for a player choice for a replacement ability.
	 */
	public static boolean isRunning;

	/**
	 * This is the panel containing the possible replacement abilities
	 */
	private JPanel container;
}
