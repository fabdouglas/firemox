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
package net.sf.firemox.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import javax.swing.JPanel;

import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.stack.StackContext;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class TargetGlassPane extends JPanel {

	/**
	 * Composite for all paints of this component.
	 */
	private final AlphaComposite composite;

	/**
	 * The active StackElement component. May be <code>null</code>.
	 */
	private StackContext stackContext;

	/**
	 * Create a new instance of this class.
	 */
	public TargetGlassPane() {
		setOpaque(false);
		setVisible(true);
		// targetPicture = Toolkit.getDefaultToolkit().getImage(
		// IdConst.IMAGES_DIR + "target.gif");
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	}

	/**
	 * @param stackContext
	 */
	public void setStackContext(StackContext stackContext) {
		this.stackContext = stackContext;
		MagicUIComponents.magicForm.setGlassPane(this);
		setVisible(true);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		final StackContext stackContext = this.stackContext;
		if (stackContext != null) {
			final MCard source = stackContext.getSourceCard();
			final Point pSource;
			if (source == null)
				return;
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setComposite(composite);
			try {
				pSource = source.getLocationOnScreen();
				if (stackContext.getTargetedList() != null) {
					for (int i = 0; i < stackContext.getTargetedList().size(); i++) {
						final GeneralPath path = new GeneralPath();
						final Point targetI = stackContext.getTargetedList().get(i)
								.getLocationOnScreen();
						float p1x = pSource.x + 15; // P1.x
						float p1y = pSource.y + 15; // P1.y
						float p2x = targetI.x + 15; // P2.x
						float p2y = targetI.y + 15; // P20y
						// Control point of the curve
						float cx = Math.abs(p2x - p1x) / 2 - 10;
						float cy = p2y - 30;
						float arrSize = 20; // Size of the arrow segments
						float width = 10;

						float adjSize = (float) (arrSize / Math.sqrt(2));
						float ex = p2x - cx;
						float ey = p2y - cy;
						float absE = (float) Math.sqrt(ex * ex + ey * ey);
						ex /= absE;
						ey /= absE;

						// Creating quad arrow
						path.moveTo(p1x, p1y);
						path.quadTo(cx, cy, p2x, p2y);
						path.lineTo(p2x + (-ex + ey) * adjSize, p2y + (-ex - ey) * adjSize);
						path.lineTo(p2x + ex * adjSize, p2y + ey * adjSize * 2.0f);
						path.lineTo(p2x - ex * adjSize - (ey + ex) * adjSize, p2y + ey
								* adjSize + (ex - ey) * adjSize);
						path.lineTo(p2x - ex * adjSize, p2y + ey * adjSize);
						path.quadTo(cx + width, cy + 2 * width, p1x + width, p1y + width);
						path.closePath();

						// TODO Should we draw a "target" picture on targeted component?
						// g2d.drawImage(targetPicture, targetI.x+5, targetI.y+15, null);
						g2d.setColor(TargetFactory.TARGET_COLOR);
						g2d.fill(path);
						g2d.setColor(Color.BLACK);
						g2d.draw(path);
						pSource.translate(0, 15);
					}
				}
			} catch (IllegalComponentStateException e) {
				// ignore this error, the source has moved since the target update.
			} finally {
				g2d.dispose();
			}
		}
	}

}
