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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.layout.LeftPanelLayout;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class JFlipFlapPanel extends JPanel implements MouseListener {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param playerTabbedPanel
	 * @param previewTabbedPanel
	 * @param tabbedPane
	 * @param youAdditionalZones
	 * @param opponentAdditionalZones
	 */
	public JFlipFlapPanel(JTabbedPane playerTabbedPanel,
			JTabbedPane previewTabbedPanel, JTabbedPane tabbedPane,
			JPanel youAdditionalZones, JPanel opponentAdditionalZones) {
		super(new BorderLayout(0, 0));
		setOpaque(false);

		componentContainer = new JPanel(null);
		componentContainer.setOpaque(false);
		componentContainer.setLayout(new LeftPanelLayout(this, componentContainer,
				playerTabbedPanel, previewTabbedPanel, tabbedPane, youAdditionalZones,
				opponentAdditionalZones));
		taskBar = new JPanel(null);
		taskBar.setBackground(Color.ORANGE);
		taskBar.setBorder(new EtchedBorder(Color.RED, Color.ORANGE));
		taskBar.setPreferredSize(new Dimension(10, 1));
		taskBar.setToolTipText(LanguageManager.getString("clikToHide.tooltip"));
		add(componentContainer, BorderLayout.CENTER);
		add(taskBar, BorderLayout.EAST);
		taskBar.addMouseListener(this);
		componentContainer.doLayout();
		doLayout();
	}

	public void mouseClicked(MouseEvent e) {
		hidePanel();
	}

	public void mousePressed(MouseEvent e) {
		// Ignore this event
	}

	public void mouseReleased(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * Return true if this panel is hidden.
	 * 
	 * @return true if this panel is hidden.
	 */
	public boolean isHidden() {
		return hidden;
	}

	private void hidePanel() {
		if (!hidden && !hiddingPanel) {
			hidden = true;
			hiddingPanel = true;
			taskBar.removeMouseListener(this);
			componentContainer.doLayout();
			doLayout();
			taskBar.addMouseListener(this);
			hiddingPanel = false;
		}
	}

	private void showPanel() {
		if (hidden && !hiddingPanel) {
			hidden = false;
			hiddingPanel = true;
			taskBar.removeMouseListener(this);
			componentContainer.doLayout();
			doLayout();
			taskBar.addMouseListener(this);
			hiddingPanel = false;
		}
	}

	public void mouseEntered(MouseEvent e) {
		showPanel();
	}

	public void mouseExited(MouseEvent e) {
		/*
		 * if (e.getX() > 9 || e.getY() < 0) hidePanel();
		 */
	}

	/**
	 * Is this panel is hidden.
	 */
	private boolean hidden;

	private boolean hiddingPanel;

	private JPanel componentContainer;

	private JPanel taskBar;
}
