/*
 * Created on Nov 16, 2004 
 * Original filename was FlowLayout2Center.java
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
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * A flow layout arranges components in a directional flow, much like lines of
 * text in a paragraph. The flow direction is determined by the container's
 * <code>componentOrientation</code> property and may be one of two values:
 * <ul>
 * <li><code>ComponentOrientation.LEFT_TO_RIGHT</code>
 * <li><code>ComponentOrientation.RIGHT_TO_LEFT</code>
 * </ul>
 * Flow layouts are typically used to arrange buttons in a panel. It arranges
 * buttons horizontally until no more buttons fit on the same line.
 * <p>
 * A flow layout lets each component assume its natural (preferred) size.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class FlowLayout2Center implements LayoutManager {

	/**
	 * Constructs a new <code>FlowLayout2Center</code> with a centered alignment
	 * and a default 5-unit horizontal and vertical gap.
	 */
	public FlowLayout2Center() {
		this(5, 5);
	}

	/**
	 * Creates a new flow layout manager with the indicated horizontal and
	 * vertical gaps.
	 * <p>
	 * 
	 * @param hgap
	 *          the horizontal gap between components and between the components
	 *          and the borders of the <code>Container</code>
	 * @param vgap
	 *          the vertical gap between components and between the components and
	 *          the borders of the <code>Container</code>
	 */
	public FlowLayout2Center(int hgap, int vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
	}

	/**
	 * Gets the horizontal gap between components and between the components and
	 * the borders of the <code>Container</code>
	 * 
	 * @return the horizontal gap between components and between the components
	 *         and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#setHgap
	 * @since JDK1.1
	 */
	public int getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between components and between the components and
	 * the borders of the <code>Container</code>.
	 * 
	 * @param hgap
	 *          the horizontal gap between components and between the components
	 *          and the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#getHgap
	 * @since JDK1.1
	 */
	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	/**
	 * Gets the vertical gap between components and between the components and the
	 * borders of the <code>Container</code>.
	 * 
	 * @return the vertical gap between components and between the components and
	 *         the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#setVgap
	 * @since JDK1.1
	 */
	public int getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between components and between the components and the
	 * borders of the <code>Container</code>.
	 * 
	 * @param vgap
	 *          the vertical gap between components and between the components and
	 *          the borders of the <code>Container</code>
	 * @see java.awt.FlowLayout#getVgap
	 * @since JDK1.1
	 */
	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	public void addLayoutComponent(String name, Component comp) {
		// Ignore this event
	}

	public void removeLayoutComponent(Component comp) {
		// Ignore this event
	}

	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();
			boolean firstVisibleComponent = true;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					dim.height = Math.max(dim.height, d.height);
					if (firstVisibleComponent) {
						firstVisibleComponent = false;
					} else {
						dim.width += hgap;
					}
					dim.width += d.width;
				}
			}
			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right + hgap * 2;
			dim.height += insets.top + insets.bottom + vgap * 2;
			return dim;
		}
	}

	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getMinimumSize();
					dim.height = Math.max(dim.height, d.height);
					if (i > 0) {
						dim.width += hgap;
					}
					dim.width += d.width;
				}
			}
			Insets insets = target.getInsets();
			dim.width += insets.left + insets.right + hgap * 2;
			dim.height += insets.top + insets.bottom + vgap * 2;
			return dim;
		}
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			int maxwidth = target.getWidth()
					- (insets.left + insets.right + hgap * 2);
			int nmembers = target.getComponentCount();
			int x = 0;
			int y = target.getHeight() - insets.bottom - vgap;
			int rowh = 0;
			int start = 0;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					m.setSize(d.width, d.height);

					if (x == 0 || x + d.width <= maxwidth) {
						if (x > 0) {
							x += hgap;
						}
						x += d.width;
						rowh = Math.max(rowh, d.height);
					} else {
						moveComponents(target, insets.left + hgap, y, maxwidth - x, rowh,
								start, i);
						x = d.width;
						y -= vgap + rowh;
						rowh = d.height;
						start = i;
					}
				}
			}
			moveComponents(target, insets.left + hgap, y, maxwidth - x, rowh, start,
					nmembers);
		}
	}

	/**
	 * Centers the elements in the specified row, if there is any slack.
	 * 
	 * @param target
	 *          the component which needs to be moved
	 * @param x
	 *          the x coordinate
	 * @param y
	 *          the y coordinate
	 * @param width
	 *          the width dimensions
	 * @param height
	 *          the height dimensions
	 * @param rowStart
	 *          the beginning of the row
	 * @param rowEnd
	 *          the the ending of the row
	 */
	private void moveComponents(Container target, int x, int y, int width,
			int height, int rowStart, int rowEnd) {
		int xRec = x;
		synchronized (target.getTreeLock()) {
			xRec += width / 2;
			for (int i = rowStart; i < rowEnd; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					m.setLocation(target.getWidth() - xRec - m.getWidth(), y - height
							+ (height - m.getHeight()) / 2);
					xRec += m.getWidth() + hgap;
				}
			}
		}
	}

	/**
	 * The flow layout manager allows a seperation of components with gaps. The
	 * horizontal gap will specify the space between components and between the
	 * components and the borders of the <code>Container</code>.
	 * 
	 * @serial
	 * @see #getHgap()
	 * @see #setHgap(int)
	 */
	protected int hgap;

	/**
	 * The flow layout manager allows a seperation of components with gaps. The
	 * vertical gap will specify the space between rows and between the the rows
	 * and the borders of the <code>Container</code>.
	 * 
	 * @serial
	 * @see #getHgap()
	 * @see #setHgap(int)
	 */
	protected int vgap;

}
