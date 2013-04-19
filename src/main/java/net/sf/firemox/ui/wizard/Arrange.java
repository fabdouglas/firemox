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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.VirtualCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Arrange extends Ok implements MouseMotionListener, MouseListener {

	/**
	 * Creates a new instance of Replacement <br>
	 * 
	 * @param destinationZone
	 *          the destination zone.
	 * @param movingCards
	 *          the moving cards you can arrange
	 * @param order
	 *          the integer array that will contain the chosen order.
	 * @param owner
	 */
	public Arrange(int destinationZone, List<MCard> movingCards, int[] order,
			Player owner) {
		super(LanguageManager.getString("wiz_arrange.title"), LanguageManager
				.getString("wiz_arrange.description", owner.zoneManager
						.getContainer(destinationZone)), "wiz_arrange.png", null, 600, 230);
		final JScrollPane scroll = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		container = new DropCardListener();
		this.movingCards = movingCards;
		this.order = order;
		this.owner = owner;
		for (int i = 0; i < movingCards.size(); i++) {
			final MCard card = movingCards.get(i);
			order[i] = i;
			container.add(card);
			card.getComponent(0).addMouseListener(this);
			card.getComponent(0).addMouseMotionListener(this);
			card.addComponentListener(container);
			card.setVisibility(Visibility.PUBLIC);
			card.tap(false);
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		scroll.setAutoscrolls(true);
		scroll.setViewportView(container);
		gameParamPanel.add(scroll);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancelBtn) {
			// remove mouse listeners
			movingCards.clear();
			for (int i = 0; i < container.getComponentCount(); i++) {
				final MCard card = (MCard) container.getComponent(i);
				card.getComponent(0).removeMouseListener(this);
				card.getComponent(0).removeMouseMotionListener(this);
				card.restoreVisibility();
				movingCards.add(card);
			}
		}
		super.actionPerformed(event);
	}

	public void mouseClicked(MouseEvent e) {
		// Ignore this event
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof VirtualCard) {
			srcPoint = ((VirtualCard) e.getSource()).card.getLocation();
			dX = e.getPoint().getX();
			dY = e.getPoint().getY();
		}
	}

	public void mouseReleased(MouseEvent e) {
		srcPoint = null;
		if (e.getSource() instanceof VirtualCard) {
			// now we move the card within the component list.
			int srcIndex = getIndexOf(e.getSource());
			if (srcIndex != container.separatorIndex
					&& srcIndex + 1 != container.separatorIndex) {
				if (container.separatorIndex == container.getComponentCount()) {
					container.remove(((Component) e.getSource()).getParent());
					container.add(((Component) e.getSource()).getParent());
				} else if (container.separatorIndex < srcIndex) {
					container.remove(((Component) e.getSource()).getParent());
					container.add(((Component) e.getSource()).getParent(),
							container.separatorIndex);
				} else if (container.separatorIndex > srcIndex) {
					container.remove(((Component) e.getSource()).getParent());
					container.add(((Component) e.getSource()).getParent(),
							container.separatorIndex - 1);
				}
				int idComp = order[srcIndex];
				if (srcIndex <= container.separatorIndex) {
					if (container.separatorIndex == container.getComponentCount()) {
						for (int i = srcIndex; i < container.separatorIndex - 2; i++) {
							order[i] = order[i + 1];
						}
						order[container.separatorIndex - 1] = idComp;
					} else {
						for (int i = srcIndex; i < container.separatorIndex - 1; i++) {
							order[i] = order[i + 1];
						}
						order[container.separatorIndex] = idComp;
					}
				} else { // if (srcIndex > container.separatorIndex) {
					for (int i = srcIndex; i > container.separatorIndex; i--) {
						order[i] = order[i - 1];
					}
					order[container.separatorIndex] = idComp;
				}
			}
			container.doLayout();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// Ignore this event
	}

	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	public void mouseDragged(MouseEvent e) {
		if (srcPoint != null) {
			srcPoint.translate((int) (e.getPoint().getX() - dX), (int) (e.getPoint()
					.getY() - dY));
			((VirtualCard) e.getSource()).card.setLocation((int) (srcPoint.getX()),
					(int) (srcPoint.getY()));
		}
	}

	/**
	 * Return the index of specified component within this container.
	 * 
	 * @param comp
	 *          the component to locate.
	 * @return the index of specified component within this container.
	 */
	private int getIndexOf(Object comp) {
		for (int i = container.getComponentCount(); i-- > 0;) {
			if (((MCard) container.getComponent(i)).getComponent(0) == comp) {
				return i;
			}
		}
		throw new InternalError("Could not find the component '" + comp + "'");
	}

	public void mouseMoved(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * This is the panel containing the components and is listening drag&drop to
	 * make some graphic stufs.
	 */
	private DropCardListener container;

	/**
	 * List of component to sort. This list is sorted each time a component has
	 * moved.
	 */
	private List<MCard> movingCards;

	/**
	 * The associated component id to the given list of cards. When a card is
	 * moved from I to J indexes, we slice all values of <code>order</code> from
	 * I to J-1 indexes and we put to J the value of <code>order[I]</code>
	 * before the move.
	 */
	public int[] order;

	/**
	 * The point of cursor when the drag has began.
	 */
	private Point srcPoint;

	/**
	 * The distance between the left-top corner and thedragging point.
	 * 
	 * @see #srcPoint
	 */
	private double dX;

	/**
	 * The distance between the left-top corner and thedragging point.
	 * 
	 * @see #srcPoint
	 */
	private double dY;

	/**
	 * The player arranging the cards
	 */
	public Player owner;
}
