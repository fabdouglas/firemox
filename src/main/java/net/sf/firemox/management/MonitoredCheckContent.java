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
package net.sf.firemox.management;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * A monitored content represents partial data representation : source,
 * destination, content length to proceed, content proceeded length.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
public class MonitoredCheckContent implements Runnable {

	/**
	 * The total content length to load.
	 */
	private int contentLength;

	/**
	 * The current content length to load.
	 */
	private int currentLength;

	/**
	 * Notification period.
	 */
	public static final int NOTIFICATION_PERIOD = 300;

	/**
	 * Is the content is completely loaded.
	 */
	private boolean isFinished = false;

	/**
	 * The listener
	 */
	protected Set<MonitorListener> listeners;

	/**
	 * Notification counter
	 */
	protected int notificationCounter = 0;

	/**
	 * The list of remote files.
	 */
	protected List<String> remoteFiles;

	/**
	 * The destination (local) files.
	 */
	protected List<String> localFiles;

	/**
	 * The string delimiting the jar/zip content.
	 */
	public static final String STR_ZIP_PATH = "!/";

	/**
	 * The wrapped content.
	 */
	private Image image;

	/**
	 * Return the current content.
	 * 
	 * @return the current content.
	 */
	public Image getContent() {
		return image;
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param content
	 *          the completed content.
	 */
	public MonitoredCheckContent(Image content) {
		this.image = content;
		this.isFinished = true;
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param localFiles
	 *          the destination (local) files.
	 * @param remoteFiles
	 *          the list of remote files.
	 * @param listener
	 *          the listener to notify.
	 */
	public MonitoredCheckContent(List<String> localFiles,
			List<String> remoteFiles, MonitorListener listener) {
		this.localFiles = localFiles;
		this.remoteFiles = remoteFiles;
		if (listener != null) {
			this.listeners = new HashSet<MonitorListener>();
			this.listeners.add(listener);
		}
		this.image = DatabaseFactory.blankImage;
	}

	/**
	 * Is the whole content is got.
	 * 
	 * @return true if the whole content is got.
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Indicates this content is completely loaded.
	 */
	public void setFinished() {
		this.isFinished = true;
		if (listeners != null) {
			synchronized (listeners) {
				for (MonitorListener listener : listeners) {
					listener.notifyChange();
				}
			}
			// now we wait the acknowledge from the listeners
			try {
				while (true) {
					Thread.sleep(NOTIFICATION_PERIOD);
					synchronized (listeners) {
						if (listeners.isEmpty())
							break;
					}
				}
			} catch (InterruptedException e) {
				// Ignore this error and terminate this thread
			}
			listeners = null;
		}

	}

	/**
	 * Add a listener.
	 * 
	 * @param listener
	 *          the listener to add.
	 */
	public final void addListener(MonitorListener listener) {
		if (listeners != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
		if (isFinished()) {
			listener.notifyChange();
		}
	}

	/**
	 * Paint the progress bar of the content download on the given graphics.
	 * 
	 * @param g
	 *          the used graphics to draw our progress bar.
	 */
	public void paintNotification(Graphics g) {
		g.drawImage(MagicUIComponents.targetTimer.byGifPictures[notificationCounter
				% MagicUIComponents.targetTimer.byGifPictures.length], 15, 15, null);
		g.setColor(Color.WHITE);
		if (contentLength <= 0) {
			g.drawString("? %", 15, 45);
		} else {
			g.drawString(String.valueOf(currentLength * 100 / contentLength) + "%",
					15, 45);
		}
	}

	/**
	 * Unregister the specified lister from the monitored content listeners.
	 * 
	 * @param listener
	 *          the listener acknowledging the finished content.
	 */
	public void acknowledgeFinished(MonitorListener listener) {
		if (listeners != null && !listeners.isEmpty()) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	/**
	 * Start the download.
	 */
	public void start() {
		new Thread(this).start();
	}

	public void run() {
		for (String localFile : localFiles) {
			try {
				if (localFile != null) {
					MonitoredCheckContent tmpContent = Picture.loadImage(localFile, null,
							this);
					if (tmpContent != null && tmpContent.getContent() != null) {
						image = tmpContent.getContent();
						setFinished();
						return;
					}
				}
			} catch (Exception e1) {
				// Nothing to, it was just a test.
			}
		}
		for (int i = 0; i < remoteFiles.size(); i++) {
			String localFile = localFiles.get(i);
			String remoteFile = remoteFiles.get(i);
			try {
				if (localFile != null && remoteFile != null) {
					MonitoredCheckContent tmpContent = Picture.loadImage(localFile,
							new URL(remoteFile), this);
					if (tmpContent != null && tmpContent.getContent() != null) {
						image = tmpContent.getContent();
						setFinished();
						return;
					}
				}
			} catch (Exception e1) {
				// Ignore this error, and test the next pair
				Log.info(e1.getMessage());
			}
		}
	}

	/**
	 * Update the content data progress.
	 * 
	 * @param contentLength
	 *          the total content length to load.
	 * @param currentLength
	 *          the current content length to load.
	 */
	public void updateProgress(int contentLength, int currentLength) {
		this.contentLength = contentLength;
		this.currentLength = currentLength;
		this.notificationCounter++;
		if (listeners != null) {
			synchronized (listeners) {
				for (MonitorListener listener : listeners) {
					listener.notifyChange();
				}
			}
		}
	}
}
