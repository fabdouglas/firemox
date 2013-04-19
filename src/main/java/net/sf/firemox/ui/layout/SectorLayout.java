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
package net.sf.firemox.ui.layout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.zone.ZoneSector;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
public class SectorLayout implements LayoutManager2, java.io.Serializable {
	/**
	 * Constructs a border layout with the horizontal gaps between components. The
	 * horizontal gap is specified by <code>hgap</code>.
	 * 
	 * @see #getHgap()
	 * @see #setHgap(int)
	 * @serial
	 */
	int hgap;

	/**
	 * Constructs a border layout with the vertical gaps between components. The
	 * vertical gap is specified by <code>vgap</code>.
	 * 
	 * @see #getVgap()
	 * @see #setVgap(int)
	 * @serial
	 */
	int vgap;

	private final Map<Object, ZoneSector> sectors;

	private static final List<AbstractCard> EMPTY_LIST = new ArrayList<AbstractCard>(
			0);

	/*
	 * JDK 1.1 serialVersionUID
	 */
	private static final long serialVersionUID = -8658291919501921765L;

	/**
	 * Constructs a new border layout with no gaps between components.
	 * 
	 * @param sectors
	 *          defined sectors.
	 */
	public SectorLayout(List<ZoneSector> sectors) {
		this(5, 5, sectors);
	}

	/**
	 * Constructs a border layout with the specified gaps between components. The
	 * horizontal gap is specified by <code>hgap</code> and the vertical gap is
	 * specified by <code>vgap</code>.
	 * 
	 * @param hgap
	 *          the horizontal gap.
	 * @param vgap
	 *          the vertical gap.
	 * @param sectors
	 *          defined sectors.
	 */
	public SectorLayout(int hgap, int vgap, List<ZoneSector> sectors) {
		this.hgap = hgap;
		this.vgap = vgap;
		this.sectors = new HashMap<Object, ZoneSector>();
		for (ZoneSector zoneSector : sectors) {
			this.sectors.put(zoneSector.getConstraint(), zoneSector);
		}
	}

	/**
	 * Returns the horizontal gap between components.
	 * 
	 * @return the horizontal gap.
	 * @since JDK1.1
	 */
	public int getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between components.
	 * 
	 * @param hgap
	 *          the horizontal gap between components
	 * @since JDK1.1
	 */
	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	/**
	 * Returns the vertical gap between components.
	 * 
	 * @return the vertical gap.
	 * @since JDK1.1
	 */
	public int getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between components.
	 * 
	 * @param vgap
	 *          the vertical gap between components
	 * @since JDK1.1
	 */
	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		//
	}

	public void addLayoutComponent(String name, Component comp) {
		//
	}

	public void removeLayoutComponent(Component comp) {
		//
	}

	/**
	 * Determines the minimum size of the <code>target</code> container using
	 * this layout manager.
	 * <p>
	 * This method is called when a container calls its
	 * <code>getMinimumSize</code> method. Most applications do not call this
	 * method directly.
	 * 
	 * @param target
	 *          the container in which to do the layout.
	 * @return the minimum dimensions needed to lay out the subcomponents of the
	 *         specified container.
	 * @see java.awt.Container
	 * @see net.sf.firemox.ui.layout.SectorLayout#preferredLayoutSize
	 * @see java.awt.Container#getMinimumSize()
	 */
	public Dimension minimumLayoutSize(Container target) {
		return new Dimension(0, 0);
	}

	/**
	 * Determines the preferred size of the <code>target</code> container using
	 * this layout manager, based on the components in the container.
	 * <p>
	 * Most applications do not call this method directly. This method is called
	 * when a container calls its <code>getPreferredSize</code> method.
	 * 
	 * @param target
	 *          the container in which to do the layout.
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container.
	 * @see java.awt.Container
	 * @see net.sf.firemox.ui.layout.SectorLayout#minimumLayoutSize
	 * @see java.awt.Container#getPreferredSize()
	 */
	public Dimension preferredLayoutSize(Container target) {
		return target.getSize();
	}

	/**
	 * Returns the maximum dimensions for this layout given the components in the
	 * specified target container.
	 * 
	 * @param target
	 *          the component which needs to be laid out
	 * @return the maximum dimensions for this layout
	 * @see Container
	 * @see #minimumLayoutSize
	 * @see #preferredLayoutSize
	 */
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns the alignment along the x axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be
	 * a number between 0 and 1 where 0 represents alignment along the origin, 1
	 * is aligned the furthest away from the origin, 0.5 is centered, etc.
	 * 
	 * @param parent
	 * @return the alignment along the x axis.
	 */
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	/**
	 * Returns the alignment along the y axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be
	 * a number between 0 and 1 where 0 represents alignment along the origin, 1
	 * is aligned the furthest away from the origin, 0.5 is centered, etc.
	 * 
	 * @param parent
	 * @return the alignment along the y axis.
	 */
	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	/**
	 * Invalidates the layout, indicating that if the layout manager has cached
	 * information it should be discarded.
	 * 
	 * @param target
	 */
	public void invalidateLayout(Container target) {
		//
	}

	private List<AbstractCard> getCards(Object constraint) {
		ZoneSector sector = sectors.get(constraint);
		if (sector == null) {
			return EMPTY_LIST;
		}
		return sector.getCards();
	}

	/**
	 * Lays out the container argument using this border layout.
	 * <p>
	 * This method actually reshapes the components in the specified container in
	 * order to satisfy the constraints of this <code>SectorLayout</code>
	 * object. The <code>NORTH</code> and <code>SOUTH</code> components, if
	 * any, are placed at the top and bottom of the container, respectively. The
	 * <code>WEST</code> and <code>EAST</code> components are then placed on
	 * the left and right, respectively. Finally, the <code>CENTER</code> object
	 * is placed in any remaining space in the middle.
	 * <p>
	 * Most applications do not call this method directly. This method is called
	 * when a container calls its <code>doLayout</code> method.
	 * 
	 * @param target
	 *          the container in which to do the layout.
	 * @see java.awt.Container
	 * @see java.awt.Container#doLayout()
	 */
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			int height = target.getHeight();
			int width = target.getWidth();

			// West part
			int yw = 0;
			int xw = 0;
			int maxWidth = 0;
			for (AbstractCard card : getCards(BorderLayout.WEST)) {
				if (card instanceof MCard && ((MCard) card).getMUI().isAutoAlign) {
					int cardHeight = card.getHeight();
					if (yw + cardHeight + vgap >= height) {
						yw = 0;
						xw += maxWidth + hgap;
						maxWidth = card.getWidth();
						card.setLocation(xw, yw);
					} else {
						if (card.getWidth() > maxWidth) {
							maxWidth = card.getWidth();
						}
						card.setLocation(xw, yw);
					}
					yw += cardHeight + vgap;
				}
			}
			xw += maxWidth + hgap;

			// East part
			int ye = vgap;
			int xe = width - hgap;
			maxWidth = 0;
			for (AbstractCard card : getCards(BorderLayout.EAST)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					int cardHeight = card.getHeight();
					if (ye + cardHeight + vgap >= height) {
						ye = 0;
						xe -= maxWidth + hgap;
						maxWidth = card.getWidth();
						card.setLocation(xe - card.getWidth(), ye);
					} else {
						if (card.getWidth() > maxWidth) {
							maxWidth = card.getWidth();
						}
						card.setLocation(xe - card.getWidth(), ye);
					}
					ye += cardHeight + vgap;
				}
			}
			xe -= maxWidth + hgap;

			// North part
			int yn = vgap;
			int xn = (xe - xw) / 2;
			maxWidth = 0;
			int maxHeightN = 0;
			int count = 0;
			int requiredSpace = 0;
			int scaledHgap = hgap;
			for (AbstractCard card : getCards(BorderLayout.NORTH)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					count++;
					requiredSpace += card.getWidth();
					if (card.getHeight() > maxHeightN)
						maxHeightN = card.getHeight();
				}
			}
			requiredSpace += count * hgap;
			if (count > 1) {
				if (requiredSpace + xw > xe) {
					scaledHgap = (xe - xw - requiredSpace) / (count - 1);
					xn = xw;
				} else {
					xn = (xe - xw - requiredSpace) / 2;
				}
			}
			for (AbstractCard card : getCards(BorderLayout.NORTH)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					card.setLocation(xn, yn);
					xn += card.getWidth() + scaledHgap;
				}
			}
			yn += maxHeightN + vgap;

			// South part
			int ys = height - vgap;
			int xs = (xe - xw) / 2;
			maxWidth = 0;
			int maxHeightS = 0;
			count = 0;
			requiredSpace = 0;
			scaledHgap = hgap;
			for (AbstractCard card : getCards(BorderLayout.SOUTH)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					count++;
					requiredSpace += card.getWidth();
					if (card.getHeight() > maxHeightS)
						maxHeightS = card.getHeight();
				}
			}
			if (count > 1) {
				if (requiredSpace + xw > xe) {
					scaledHgap = (xe - xw - requiredSpace) / (count - 1);
					xs = xw;
				} else {
					xs = (xe - xw - requiredSpace) / 2;
				}
			}
			for (AbstractCard card : getCards(BorderLayout.SOUTH)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					card.setLocation(xs, ys - card.getHeight());
					xs += card.getWidth() + scaledHgap;
				}
			}
			ys -= maxHeightS + vgap;

			// Center part
			maxWidth = 0;
			int maxHeightC = 0;
			requiredSpace = 0;
			int requiredHeight = 0;
			scaledHgap = hgap;
			int tmpX = 0;
			List<Integer> lineWidths = new ArrayList<Integer>();
			for (AbstractCard card : getCards(BorderLayout.CENTER)) {
				if (((MCard) card).getMUI().isAutoAlign) {
					if (tmpX + card.getWidth() + hgap + xw >= xe) {
						// add new line to the center
						lineWidths.add(tmpX);
						tmpX = 0;
						// update the total lines height
						requiredHeight += maxHeightC + vgap;
						maxHeightC = card.getHeight();
					} else if (card.getHeight() > maxHeightC) {
						// update the line height
						maxHeightC = card.getHeight();
					}
					tmpX += card.getWidth() + hgap;
				}
			}
			lineWidths.add(tmpX);

			if (!lineWidths.isEmpty()) {
				int yc = (ys - yn) / 2;
				if (yn + requiredHeight > ys) {
					// Not enougth space
					yc = yn;
				} else {
					yc = (ys - yn - requiredHeight) / 2;
				}
				int lastLine = 0;
				int lineWidth = lineWidths.get(0);
				int xc = (xe - xw - lineWidth) / 2;
				maxHeightC = 0;
				for (AbstractCard card : getCards(BorderLayout.CENTER)) {
					if (((MCard) card).getMUI().isAutoAlign) {
						if (xc + card.getWidth() + hgap + xw >= xe) {
							lineWidth = lineWidths.get(++lastLine);
							xc = (xe - xw - lineWidth) / 2;
							yc += maxHeightC + vgap;
							maxHeightC = card.getHeight();
						} else {
							if (card.getHeight() > maxHeightC) {
								maxHeightC = card.getHeight();
							}
						}
						card.setLocation(xc, yc);
						xc += card.getWidth() + hgap;
					}
				}
			}
		}
	}

	/**
	 * Returns a string representation of the state of this border layout.
	 * 
	 * @return a string representation of this border layout.
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
	}

}
