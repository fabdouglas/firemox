/*
 * Created on Dec 20, 2004
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
 */
package net.sf.firemox.ui.component;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class SplashScreen extends JWindow {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param filename
	 *          the picture filename.
	 * @param parent
	 *          the splash screen's parent.
	 * @param waitTime
	 *          the maximum time before the screen is hidden.
	 */
	public SplashScreen(String filename, Frame parent, int waitTime) {
		super(parent);
		getContentPane().setLayout(null);
		toFront();
		final JLabel l = new JLabel(new ImageIcon(filename));
		final Dimension labelSize = l.getPreferredSize();
		l.setLocation(0, 0);
		l.setSize(labelSize);
		setSize(labelSize);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - labelSize.width / 2, screenSize.height
				/ 2 - labelSize.height / 2);

		final JLabel mp = new JLabel(IdConst.PROJECT_DISPLAY_NAME);
		mp.setLocation(30, 305);
		mp.setSize(new Dimension(300, 30));

		final JLabel version = new JLabel(IdConst.VERSION);
		version.setLocation(235, 418);
		version.setSize(new Dimension(300, 30));

		final JTextArea disclaimer = new JTextArea();
		disclaimer.setEditable(false);
		disclaimer.setLineWrap(true);
		disclaimer.setWrapStyleWord(true);
		disclaimer.setAutoscrolls(true);
		disclaimer.setFont(MToolKit.defaultFont);
		disclaimer.setTabSize(2);

		// Then try and read it locally
		Reader inGPL = null;
		try {
			inGPL = new BufferedReader(new InputStreamReader(MToolKit
					.getResourceAsStream(IdConst.FILE_LICENSE)));
			disclaimer.read(inGPL, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inGPL);
		}

		final JScrollPane disclaimerSPanel = new JScrollPane();
		disclaimerSPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		disclaimerSPanel.setViewportView(disclaimer);
		disclaimerSPanel.setLocation(27, 340);
		disclaimerSPanel.setPreferredSize(new Dimension(283, 80));
		disclaimerSPanel.setSize(disclaimerSPanel.getPreferredSize());

		getContentPane().add(disclaimerSPanel);
		getContentPane().add(version);
		getContentPane().add(mp);
		getContentPane().add(l);

		final int pause = waitTime;
		final Runnable waitRunner = new Runnable() {
			public void run() {
				try {
					Thread.sleep(pause);
					while (!bKilled) {
						Thread.sleep(200);
					}
				} catch (InterruptedException e) {
					// Ignore this error
				}
				setVisible(false);
				dispose();
				MagicUIComponents.magicForm.toFront();
			}
		};

		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				setVisible(false);
				dispose();
				if (MagicUIComponents.magicForm != null)
					MagicUIComponents.magicForm.toFront();
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					setVisible(false);
					dispose();
					MagicUIComponents.magicForm.toFront();
				}
			}
		});
		setVisible(true);
		start(waitRunner);
	}

	private void start(Runnable runnable) {
		new Thread(runnable, "SplashThread").start();
	}

	/**
	 * Close this splash screen.
	 */
	public void close() {
		bKilled = true;
	}

	boolean bKilled = false;

}
