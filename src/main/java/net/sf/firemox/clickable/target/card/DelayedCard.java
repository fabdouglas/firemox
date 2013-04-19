/*
 * Created on Nov 11, 2004 
 * Original filename was DelayedCard.java
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
package net.sf.firemox.clickable.target.card;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JLabel;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.TriggeredAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.modifier.Unregisterable;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 * @since 0.86 A second target can be saved
 */
public class DelayedCard extends JLabel implements MouseListener,
		Unregisterable {

	/**
	 * Creates a new instance of DelayedCard <br>
	 * 
	 * @param ability
	 *          The main ability of this delayed card. This ability is registered
	 *          to the listeners until one of the 'until' abilities unregister it.
	 * @param abilities
	 *          This is the list linked abilities corresponding to the 'until'
	 *          part of this delayed card. Each one of these abilities is supposed
	 *          to unregister from the listeners the main ability.
	 * @param registers
	 *          the registers of this card. These registers must have been fixed
	 *          previously.
	 * @param saved
	 *          the saved target.
	 * @param saved2
	 *          the second saved target.
	 */
	public DelayedCard(TriggeredAbility ability, List<Ability> abilities,
			int[] registers, Target saved, Target saved2) {
		super();
		this.ability = ability;
		this.abilities = abilities;
		ability.setDelayedCard(this);
		setPreferredSize(new Dimension(TriggeredCard.cardWidth,
				TriggeredCard.cardHeight));
		setSize(new Dimension(TriggeredCard.cardWidth, TriggeredCard.cardHeight));
		this.registers = registers;
		this.saved = saved;
		this.saved2 = saved2;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.drawImage(ability.getCard().scaledImage(), null, null);

		g2D.setColor(Color.BLACK);
		g2D.drawRect(0, 0, CardFactory.cardWidth - 1, CardFactory.cardHeight - 1);
		g2D.dispose();
	}

	/**
	 * Remove from the event manager this listener.
	 */
	public void removeFromManager() {
		for (Ability ability : abilities) {
			ability.removeFromManager();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// Nothing to do
	}

	public void mouseExited(MouseEvent e) {
		// Nothing to do
	}

	public MCard getCard() {
		return ability.getCard();
	}

	/**
	 * The main ability of this delayed card. This ability is registered to the
	 * listeners until one of the 'until' abilities unregister it.
	 */
	private TriggeredAbility ability;

	/**
	 * This is the list linked abilities corresponding to the 'until' part of this
	 * delayed card. Each one of these abilities is supposed to unregister from
	 * the listeners the main ability.
	 */
	private List<Ability> abilities;

	/**
	 * The saved target when this delayed card has been created
	 */
	public Target saved;

	/**
	 * The second saved target when this delayed card has been created
	 */
	public Target saved2;

	/**
	 * the registers of this card. These registers must have been fixed
	 * previously.
	 */
	public int[] registers;
}
