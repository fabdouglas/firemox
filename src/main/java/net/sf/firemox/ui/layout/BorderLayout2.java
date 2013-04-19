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

import net.sf.firemox.ui.component.MovableComponent;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class BorderLayout2 extends BorderLayout {

	/**
	 * Constructs a new border layout with no gaps between components.
	 */
	public BorderLayout2() {
		this(0, 0);
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
	 */
	public BorderLayout2(int hgap, int vgap) {
		super(hgap, vgap);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if (!(comp instanceof MovableComponent)) {
			super.removeLayoutComponent(comp);
		}
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (!(comp instanceof MovableComponent)) {
			super.addLayoutComponent(comp, constraints);
		}
	}

}
