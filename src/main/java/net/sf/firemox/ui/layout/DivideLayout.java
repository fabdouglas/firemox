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
import java.awt.LayoutManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class DivideLayout implements LayoutManager {

	private Container container;

	/**
	 * Constructs a new <code>DivideLayout</code>.
	 * 
	 * @param container
	 *          the container of this layout.
	 */
	public DivideLayout(Container container) {
		this.container = container;
	}

	public void addLayoutComponent(String name, Component comp) {
		// Ignore this event
	}

	public void removeLayoutComponent(Component comp) {
		// Ignore this event
	}

	public Dimension preferredLayoutSize(Container target) {
		return container.getSize();
	}

	public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			final int height = container.getHeight() / container.getComponentCount();
			final int width = container.getWidth();
			for (int i = container.getComponentCount(); i-- > 0;) {
				final Component c = container.getComponent(i);
				c.setLocation(0, i * height);
				c.setSize(width, height);
			}
		}
	}
}
