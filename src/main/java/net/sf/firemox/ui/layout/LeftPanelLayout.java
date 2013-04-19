/*
 * Created on Dec 20, 2004 
 * Original filename was LeftPanelLayout.java
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
package net.sf.firemox.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.component.JFlipFlapPanel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class LeftPanelLayout implements LayoutManager {

	/**
	 * Creates a new instance of AttachmentLayout <br>
	 * 
	 * @param flipFlapPanel
	 * @param leftPanel
	 * @param playerPanel
	 * @param previewPanel
	 * @param zonePanel
	 * @param youAdditionalZones
	 * @param opponentAdditionalZones
	 */
	public LeftPanelLayout(JFlipFlapPanel flipFlapPanel, JPanel leftPanel,
			JTabbedPane playerPanel, JTabbedPane previewPanel, JTabbedPane zonePanel,
			JPanel youAdditionalZones, JPanel opponentAdditionalZones) {
		this.flipFlapPanel = flipFlapPanel;
		this.leftPanel = leftPanel;
		this.playerPanel = playerPanel;
		this.previewPanel = previewPanel;
		this.zonePanel = zonePanel;
		this.youAdditionalZones = youAdditionalZones;
		this.opponentAdditionalZones = opponentAdditionalZones;
		this.dimension = new Dimension(IdConst.STD_WIDTH + 13, -1);
		this.dimensionMin = new Dimension(10, -1);
		leftPanel.add(opponentAdditionalZones);
		if (previewPanel != null) {
			leftPanel.add(previewPanel);
		}
		leftPanel.add(playerPanel);
		leftPanel.add(zonePanel);
		leftPanel.add(youAdditionalZones);
	}

	public Dimension preferredLayoutSize(Container target) {
		if (flipFlapPanel.isHidden()) {
			return dimensionMin;
		}
		return dimension;
	}

	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			return preferredLayoutSize(target);
		}
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {

			if (flipFlapPanel.isHidden()) {
				leftPanel.setVisible(false);
			} else {
				leftPanel.setVisible(true);
				int height = leftPanel.getSize().height;
				int lastY = START_Y;
				int width = IdConst.STD_WIDTH + 10;

				opponentAdditionalZones.setLocation(START_X, lastY);
				opponentAdditionalZones.setPreferredSize(new Dimension(width,
						CardFactory.cardHeight + 10));
				opponentAdditionalZones.setSize(opponentAdditionalZones
						.getPreferredSize());

				lastY += opponentAdditionalZones.getPreferredSize().height;

				if (previewPanel != null) {
					previewPanel.setLocation(START_X, lastY);
					previewPanel.setPreferredSize(new Dimension(width,
							IdConst.STD_HEIGHT + 30));
					previewPanel.setSize(previewPanel.getPreferredSize());
					lastY += previewPanel.getPreferredSize().height;
				}

				youAdditionalZones.setLocation(START_X, height - CardFactory.cardHeight
						- 10);
				youAdditionalZones.setPreferredSize(new Dimension(width,
						CardFactory.cardHeight + 10));
				youAdditionalZones.setSize(youAdditionalZones.getPreferredSize());

				zonePanel.setLocation(START_X, youAdditionalZones.getLocation().y
						- CardFactory.cardHeight - 60);
				zonePanel.setPreferredSize(new Dimension(width,
						CardFactory.cardHeight + 60));
				zonePanel.setSize(zonePanel.getPreferredSize());

				playerPanel.setLocation(START_X, lastY);
				playerPanel.setPreferredSize(new Dimension(width, zonePanel
						.getLocation().y
						- lastY - 1));
				playerPanel.setSize(playerPanel.getPreferredSize());
			}
		}
	}

	public void addLayoutComponent(String name, Component comp) {
		// Ignore this event
	}

	public void removeLayoutComponent(Component comp) {
		// Ignore this event
	}

	private static final int START_X = 2;

	private static final int START_Y = 0;

	private JTabbedPane playerPanel;

	private JTabbedPane previewPanel;

	private JTabbedPane zonePanel;

	private JPanel opponentAdditionalZones;

	private JPanel youAdditionalZones;

	private JPanel leftPanel;

	private Dimension dimension;

	private Dimension dimensionMin;

	JFlipFlapPanel flipFlapPanel;
}