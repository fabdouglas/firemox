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
 * 
 */
package net.sf.firemox.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class ToolKit {

	/**
	 * Create a new instance of this class.
	 */
	private ToolKit() {
		super();
	}

	/**
	 * Force the escape key to call the same action as pressing the Cancel button.
	 * This does not always work. See class comment.
	 * 
	 * @param dialog
	 * @param cancelButton
	 */
	public static void addCancelByEscapeKey(JDialog dialog,
			final JButton cancelButton) {
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap inputMap = dialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);

		AbstractAction cancelAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				cancelButton.doClick();
			}
		};
		dialog.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}

	/**
	 * Convenience method that returns a scaled instance of the provided
	 * {@code BufferedImage}.
	 * 
	 * @param img
	 *          the original image to be scaled
	 * @param targetWidth
	 *          the desired width of the scaled instance, in pixels
	 * @param targetHeight
	 *          the desired height of the scaled instance, in pixels
	 * @param borderWidth
	 *          the border width.
	 * @param borderColor
	 *          the border color.
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	public static BufferedImage getScaledInstance(BufferedImage img,
			int targetWidth, int targetHeight, int borderWidth, Color borderColor) {
		final int type = BufferedImage.TYPE_INT_ARGB;
		int w, h;
		/*
		 * Use multi-step technique: start with original size, then scale down in
		 * multiple passes with drawImage() until the target size is reached
		 */
		w = img.getWidth();
		h = img.getHeight();
		BufferedImage tmpBorder = new BufferedImage(w + borderWidth * 2, h
				+ borderWidth * 2, type);
		Graphics2D g2Border = tmpBorder.createGraphics();
		g2Border.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2Border.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2Border.setColor(borderColor);
		g2Border.fillRoundRect(0, 0, w + borderWidth * 2, h + borderWidth * 2,
				borderWidth * 2, borderWidth * 2);
		g2Border.drawImage(img, borderWidth, borderWidth, w, h, null);

		BufferedImage ret = tmpBorder;
		do {
			if (w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	private static final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";

}
