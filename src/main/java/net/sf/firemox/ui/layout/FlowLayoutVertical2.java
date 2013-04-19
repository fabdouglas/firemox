/*
 * Created on Oct 24, 2004 
 * Original filename was FlowLayoutVertical2.java
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
public class FlowLayoutVertical2 extends FlowLayoutVertical implements
		LayoutManager {

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 */
	public FlowLayoutVertical2() {
		this(5, 5, 0);
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and a
	 * default 5-unit horizontal and vertical gap.
	 * 
	 * @param preferredHeight
	 *          the preferred height of this layout.
	 */
	public FlowLayoutVertical2(int preferredHeight) {
		this(5, 5, preferredHeight);
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
	public FlowLayoutVertical2(int hgap, int vgap, int preferredHeight) {
		super(hgap, vgap, preferredHeight);
	}

	/**
	 * Lays out the container. This method lets each <i>visible </i> component
	 * take its preferred size by reshaping the components in the target container
	 * in order to satisfy the alignment of this <code>FlowLayout</code> object.
	 * 
	 * @param target
	 *          the specified component being laid out
	 * @see Container
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Insets insets = target.getInsets();
			int maxwidth = target.getWidth()
					- (insets.left + insets.right + hgap * 2);
			int maxheight = target.getHeight()
					- (insets.top + insets.bottom + vgap * 2);
			int nmembers = target.getComponentCount();
			int x = maxwidth;
			int y = maxheight;
			int roww = 0;

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					m.setSize(d.width, d.height);

					if (y - d.height >= 0) {
						m.setLocation(x, y);
						y -= d.height - hgap;
						roww = Math.max(roww, d.width);
					} else {
						x -= hgap + roww;
						m.setLocation(x, maxheight);
						y = maxheight - d.height - vgap;
						roww = d.width;
					}
				}
			}
		}
	}
}
