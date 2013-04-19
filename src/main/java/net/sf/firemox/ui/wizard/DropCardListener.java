/*
 * Created on Jan 9, 2005
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
 */
package net.sf.firemox.ui.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class DropCardListener extends JPanel implements ComponentListener {

	/**
	 * Creates a new instance of DropCardListener
	 */
	public DropCardListener() {
		super(new FlowLayout(FlowLayout.LEFT, 10, 5));
	}

	@Override
	public void paint(Graphics g) {
		// paint components first
		super.paint(g);
		int index = 0;
		for (int i = 0; i < getComponentCount(); i++) {
			if (getComponent(i) == movingComponent) {
				index = i;
				break;
			}
		}
		g.setColor(Color.GREEN);
		g.draw3DRect(index * (CardFactory.cardWidth + 10) + 10, 0,
				CardFactory.cardWidth, getSize().height, true);

		if (separatorIndex != index && separatorIndex != index + 1) {
			g.setColor(Color.RED);
			// draw an insertion point
			g.draw3DRect(separatorIndex * (10 + CardFactory.cardWidth), 0, 5,
					getSize().height, true);
		}
	}

	public void componentMoved(ComponentEvent e) {
		Component comp = e.getComponent();
		if (comp instanceof MCard) {
			movingComponent = comp;
			int x = comp.getLocation().x;
			if (x <= 10) {
				// out of bounds (min)
				separatorIndex = 0;
			} else if (x >= 10 + getComponentCount() * (CardFactory.cardWidth + 10)) {
				// out of bounds (max)
				separatorIndex = getComponentCount();
			} else {
				separatorIndex = (x - 10) / (CardFactory.cardWidth + 10);
			}
			repaint();
		}
	}

	public void componentResized(ComponentEvent e) {
		// Ignore this event
	}

	public void componentShown(ComponentEvent e) {
		// Ignore this event
	}

	public void componentHidden(ComponentEvent e) {
		// Ignore this event
	}

	/**
	 * The moving component
	 */
	private Component movingComponent;

	/**
	 * Location of rectangle representing insertion index of the moving card.
	 */
	int separatorIndex;

}
