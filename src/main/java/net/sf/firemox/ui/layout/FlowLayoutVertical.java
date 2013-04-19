/*
 * Created on Oct 24, 2004 
 * Original filename was FlowLayoutVertical.java
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
public class FlowLayoutVertical implements LayoutManager {

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 * 
	 * @param preferredHeight
	 *          the preferred height of this layout.
	 */
	public FlowLayoutVertical(int preferredHeight) {
		this(5, 5, preferredHeight);
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 */
	public FlowLayoutVertical() {
		this(5, 5, 0);
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
	 * @param preferredHeight
	 *          the preferred height of this layout.
	 */
	public FlowLayoutVertical(int hgap, int vgap, int preferredHeight) {
		this.hgap = hgap;
		this.vgap = vgap;
		// this.preferredHeight = preferredHeight;
	}

	public void addLayoutComponent(String name, Component comp) {
		// Ignore this event
	}

	public void removeLayoutComponent(Component comp) {
		// Ignore this event
	}

	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			// Dimension dim = new Dimension(hgap * 2, preferredHeight);
			Dimension dim = new Dimension(hgap * 2, target.getSize().height);
			int nmembers = target.getComponentCount();
			int height = 0;
			int maxheight = target.getHeight() - vgap * 2;
			int tmpMaxWidth = 0;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					height += d.height;
					if (height > maxheight) {
						height = d.height;
						dim.width += tmpMaxWidth + vgap;
						tmpMaxWidth = 0;
					}
					tmpMaxWidth = Math.max(tmpMaxWidth, d.width);
					height += vgap;
				}
			}
			dim.width += tmpMaxWidth;
			return dim;
		}
	}

	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			Dimension dim = new Dimension(insets.left + insets.right + hgap * 2,
					insets.top + insets.bottom + vgap * 2);
			int nmembers = target.getComponentCount();
			int tmpMaxHeight = 0;
			int tmpMaxwidth = 0;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getMinimumSize();
					tmpMaxHeight = Math.max(tmpMaxHeight, d.height);
					tmpMaxwidth += d.width + hgap;
				}
			}
			dim.height += tmpMaxHeight;
			dim.width += tmpMaxwidth;
			return dim;
		}
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			int maxheight = target.getHeight()
					- (insets.top + insets.bottom + vgap * 2);
			int nmembers = target.getComponentCount();
			int x = 0;
			int y = 0;
			int roww = 0;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					m.setSize(d.width, d.height);

					if (y + d.height <= maxheight || y == 0) {
						m.setLocation(x, y);
						y += d.height + vgap;
						roww = Math.max(roww, d.width);
					} else {
						x += hgap + roww;
						m.setLocation(x, 0);
						y = d.height + vgap;
						roww = d.width;
					}
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
	 */
	protected int hgap;

	/**
	 * The flow layout manager allows a seperation of components with gaps. The
	 * vertical gap will specify the space between rows and between the the rows
	 * and the borders of the <code>Container</code>.
	 * 
	 * @serial
	 */
	protected int vgap;

}
