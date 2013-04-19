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
package net.sf.firemox.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

/**
 * Bring to front manager
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.84
 */
public class BringToFrontListener implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		// TODO Bring To Front Listener refresher
	}

	/**
	 * Set the bring to front component. This restore the previous bring to front
	 * component state, then bring this component to front. Bringing to front
	 * simply repaint a component.
	 * 
	 * @param component
	 */
	public void setBringToFrontComponent(JComponent component) {
		// if (!isBringToFront(component)) {
		if (this.component != null) {
			this.component.getParent().repaint();
		}
		this.component = component;
		this.component.getParent().repaint();
		// }
	}

	/**
	 * Return the current bring to front component. If none, retur null.
	 * 
	 * @return the current bring to front component. If none, retur null.
	 */
	public JComponent getBringToFrontComponent() {
		return component;
	}

	/**
	 * Indicates the specified component is the bring to front one.
	 * 
	 * @param component
	 *          the tested component.
	 * @return true if the specified component is the bring to front one.
	 */
	public boolean isBringToFront(JComponent component) {
		return this.component == component;
	}

	/**
	 * The last bring to front component.
	 */
	private JComponent component;

}
