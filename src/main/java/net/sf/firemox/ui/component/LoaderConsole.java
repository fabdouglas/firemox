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
package net.sf.firemox.ui.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.network.NetworkActor;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author Fabrice Daugan
 * @since 0.53
 * @since 0.86 JDialog icon updated
 * @since 'cancel' button is defined as default and cancel button, and window
 *        closing is handled.
 */
public class LoaderConsole extends JDialog {

	private static LoaderConsole instance;

	/**
	 * Creates a new instance of MLoader <br>
	 */
	private LoaderConsole() {
		super(MagicUIComponents.magicForm, false);
		loadingText = new JTextArea();
		loadingText.setEditable(false);
		loadingText.setLineWrap(true);
		loadingText.setWrapStyleWord(true);
		loadingText.setBorder(new EtchedBorder());
		JButton cancelLoadBtn = new JButton(LanguageManager.getString("cancel"));
		JScrollPane sPane = new JScrollPane(loadingText,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(cancelLoadBtn, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(cancelLoadBtn);
		loadingPorgressbar = new JProgressBar(0, 100);
		getContentPane().add(loadingPorgressbar, BorderLayout.NORTH);
		getContentPane().add(sPane, BorderLayout.CENTER);

		cancelLoadBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				NetworkActor.cancelling = true;
				setVisible(false);
			}
		});
		ToolKit.addCancelByEscapeKey(this, cancelLoadBtn);
	}

	/**
	 * hide the current panel and show the loader form
	 */
	public static void launchLoader() {
		instance = new LoaderConsole();
		instance.loadingText.setText("");
		instance.setLocation(
				(instance.getToolkit().getScreenSize().width - 450) / 2, (instance
						.getToolkit().getScreenSize().height - 220) / 2);
		instance.setSize(450, 220);
		loadingPorgressbar.setValue(0);
		instance.setVisible(true);
	}

	/**
	 * hide the current panel and show the loader form
	 */
	public static void endTask() {
		if (instance != null) {
			instance.setVisible(false);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		try {
			if (visible) {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "newlan.gif"));
			} else {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "mp.gif"));
				instance = null;
			}
		} catch (Exception e) {
			// IGNORING
		}
		super.setVisible(visible);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			NetworkActor.cancelling = true;
		}
	}

	/**
	 * Begin a new task.
	 * 
	 * @param taskName
	 *          the task name.
	 */
	public static void beginTask(String taskName) {
		if (instance != null) {
			instance.loadingText.append(taskName + "\n");
		}
	}

	/**
	 * Begin a new task.
	 * 
	 * @param taskName
	 *          the task name.
	 * @param percent
	 *          the percent of task.
	 */
	public static void beginTask(String taskName, int percent) {
		if (instance != null) {
			beginTask(taskName);
			setTaskPercent(percent);
		}
	}

	/**
	 * Update the percent of current task.
	 * 
	 * @param percent
	 *          the percent of task.
	 */
	public static void setTaskPercent(int percent) {
		loadingPorgressbar.setValue(percent);
	}

	/**
	 * The progressbar
	 */
	private static JProgressBar loadingPorgressbar;

	/**
	 * The current displayed text
	 */
	private JTextArea loadingText;

}