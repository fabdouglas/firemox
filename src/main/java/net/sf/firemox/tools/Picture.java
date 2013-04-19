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
package net.sf.firemox.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JComponent;

import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.management.MonitoredCheckContent;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import sun.awt.image.ToolkitImage;

/**
 * A JComponent displaying an image. The picture is sized to suit to the
 * component size.<br>
 * <ul>
 * TODO Add context menu to this component to:
 * <li>display the picture with right size</li>
 * <li>obtain more information about illustrator</li>
 * </ul>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author brius for http proxy configuration
 * @since 0.83 Empty file are deleted to force file to be downloaded.
 */
public class Picture extends JComponent {

	/**
	 * The string delimiting the jar/zip content.
	 */
	public static final String STR_ZIP_PATH = "!/";

	/**
	 * Creates a new instance of Picture <br>
	 * The displayed picture will be the back picture of current TBS game. If no
	 * TBS game is loaded, no picture would be drawn. The [preferred]size of this
	 * component will be set to the given dimensions.
	 * 
	 * @param width
	 *          the fixed width of this picture
	 * @param height
	 *          the fixed height of this picture
	 */
	public Picture(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		setImage(DatabaseFactory.backImage, null);
	}

	/**
	 * Set the image and card's name to display. Repaint method is called
	 * immediately.
	 * 
	 * @param cardImage
	 *          the new card's image to display.
	 * @param cardName
	 *          the new card's name to use as tooltip.
	 */
	public void setImage(Image cardImage, String cardName) {
		if (cardImage != this.cardImage || cardName != this.cardName) {
			this.cardImage = cardImage;
			this.cardName = cardName;
			if (cardName == null) {
				setToolTipText("??");
			} else {
				setToolTipText(cardName);
			}
			repaint();
		}
	}

	/**
	 * Download a file from the specified URL to the specified local file.
	 * 
	 * @param localFile
	 *          is the new card's picture to try first
	 * @param remoteFile
	 *          is the URL where this picture will be downloaded in case of the
	 *          specified card name has not been found locally.
	 * @since 0.83 Empty file are deleted to force file to be downloaded.
	 */
	public static void download(String localFile, URL remoteFile) {
		download(localFile, remoteFile, null);
	}

	/**
	 * Download a file from the specified URL to the specified local file.
	 * 
	 * @param localFile
	 *          is the new card's picture to try first
	 * @param remoteFile
	 *          is the URL where this picture will be downloaded in case of the
	 *          specified card name has not been found locally.
	 * @param listener
	 *          the component waiting for this picture.
	 * @since 0.83 Empty file are deleted to force file to be downloaded.
	 */
	public static synchronized void download(String localFile, URL remoteFile,
			MonitoredCheckContent listener) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		File toDownload = new File(localFile);
		if (toDownload.exists() && toDownload.length() == 0
				&& toDownload.canWrite()) {
			toDownload.delete();
		}
		if (!toDownload.exists()
				|| (toDownload.length() == 0 && toDownload.canWrite())) {
			// the file has to be downloaded
			try {
				if ("file".equals(remoteFile.getProtocol())) {
					File localRemoteFile = MToolKit.getFile(remoteFile.toString()
							.substring(7).replaceAll("%20", " "), false);
					int contentLength = (int) localRemoteFile.length();
					Log.info("Copying from " + localRemoteFile.getAbsolutePath());
					LoaderConsole.beginTask(LanguageManager.getString("downloading")
							+ " " + localRemoteFile.getAbsolutePath() + "("
							+ FileUtils.byteCountToDisplaySize(contentLength) + ")");

					// Copy file
					in = new BufferedInputStream(new FileInputStream(localRemoteFile));
					byte[] buf = new byte[2048];
					int currentLength = 0;
					boolean succeed = false;
					for (int bufferLen = in.read(buf); bufferLen >= 0; bufferLen = in
							.read(buf)) {
						if (!succeed) {
							toDownload.getParentFile().mkdirs();
							out = new BufferedOutputStream(new FileOutputStream(localFile));
							succeed = true;
						}
						currentLength += bufferLen;
						if (out != null) {
							out.write(buf, 0, bufferLen);
						}
						if (listener != null) {
							listener.updateProgress(contentLength, currentLength);
						}
					}

					// Step 3: close streams
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
					in = null;
					out = null;
					return;
				}

				// Testing mode?
				if (!MagicUIComponents.isUILoaded()) {
					return;
				}

				// Step 1: open streams
				final URLConnection connection = MToolKit.getHttpConnection(remoteFile);
				int contentLength = connection.getContentLength();
				in = new BufferedInputStream(connection.getInputStream());
				Log.info("Download from " + remoteFile + "("
						+ FileUtils.byteCountToDisplaySize(contentLength) + ")");
				LoaderConsole.beginTask(LanguageManager.getString("downloading") + " "
						+ remoteFile + "("
						+ FileUtils.byteCountToDisplaySize(contentLength) + ")");

				// Step 2: read and write until done
				byte[] buf = new byte[2048];
				int currentLength = 0;
				boolean succeed = false;
				for (int bufferLen = in.read(buf); bufferLen >= 0; bufferLen = in
						.read(buf)) {
					if (!succeed) {
						toDownload.getParentFile().mkdirs();
						out = new BufferedOutputStream(new FileOutputStream(localFile));
						succeed = true;
					}
					currentLength += bufferLen;
					if (out != null) {
						out.write(buf, 0, bufferLen);
					}
					if (listener != null) {
						listener.updateProgress(contentLength, currentLength);
					}
				}

				// Step 3: close streams
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
				in = null;
				out = null;
				return;
			} catch (IOException e1) {
				if (MToolKit.getFile(localFile) != null) {
					MToolKit.getFile(localFile).delete();
				}
				if (remoteFile.getFile().equals(remoteFile.getFile().toLowerCase())) {
					Log.fatal("could not load picture " + localFile + " from URL "
							+ remoteFile + ", " + e1.getMessage());
				}
				String tmpRemote = remoteFile.toString().toLowerCase();
				try {
					download(localFile, new URL(tmpRemote), listener);
				} catch (MalformedURLException e) {
					Log.fatal("could not load picture " + localFile + " from URL "
							+ tmpRemote + ", " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Load the file picture and return the Image object.
	 * 
	 * @param localFile
	 *          is the new card's picture to try first
	 * @return the Image object representing this fileName picture
	 * @throws MalformedURLException
	 */
	public static Image loadImage(String localFile) throws MalformedURLException {
		if (localFile == null) {
			return null;
		}
		MonitoredCheckContent res = loadImage(localFile, null);
		if (res != null) {
			return res.getContent();
		}
		return null;
	}

	/**
	 * Load the file picture and return the Image object. If the specified
	 * filename did not exist, the specified URL is used to download it.
	 * 
	 * @param localFile
	 *          is the new card's picture to try first
	 * @param remoteFile
	 *          is the URL where this picture will be downloaded in case of the
	 *          specified card name has not been found locally.
	 * @return the Image object representing this fileName picture
	 * @throws MalformedURLException
	 */
	public static MonitoredCheckContent loadImage(String localFile, URL remoteFile)
			throws MalformedURLException {
		return loadImage(localFile, remoteFile, null);
	}

	/**
	 * Load the file picture and return the Image object. If the specified
	 * filename did not exist, the specified URL is used to download it.
	 * 
	 * @param localFile
	 *          is the new card's picture to try first
	 * @param remoteFile
	 *          is the URL where this picture will be downloaded in case of the
	 *          specified card name has not been found locally.
	 * @param listener
	 *          the component waiting for this picture.
	 * @return the Image object representing this fileName picture
	 * @throws MalformedURLException
	 * @since 0.83 Empty file are deleted to force file to be downloaded.
	 * @since 0.90 zipped files are managed.
	 */
	public static MonitoredCheckContent loadImage(String localFile,
			URL remoteFile, MonitoredCheckContent listener)
			throws MalformedURLException {
		File toDownload = null;
		try {
			// First determines the file is within a zip file
			if (localFile.contains(STR_ZIP_PATH)) {
				final String zipName = localFile.substring(0, localFile
						.indexOf(STR_ZIP_PATH));
				toDownload = MToolKit.getFile(zipName);
				if (toDownload == null) {
					// The zip file does not exist, we get it first
					toDownload = MToolKit.getFile(zipName, false);
					if (toDownload.exists() && toDownload.isFile()
							&& toDownload.length() == 0) {
						// delete previous null sized zip file
						toDownload.delete();
					}

					if (!toDownload.exists()) {
						// the file does not exist
						if (remoteFile == null) {
							return null;
						}
						download(zipName, new URL(remoteFile.toString().substring(0,
								remoteFile.toString().indexOf(STR_ZIP_PATH))), listener);
					}
				}
				return new MonitoredCheckContent(MToolKit.getLocalPicture("jar:"
						+ MToolKit.getFile(zipName, false).toURI().toURL()
						+ localFile.substring(localFile.indexOf(STR_ZIP_PATH))));
			}

			toDownload = MToolKit.getFile(localFile);
			if (toDownload == null)
				toDownload = MToolKit.getFile(localFile, false);
			if (toDownload.exists() && toDownload.isFile()
					&& toDownload.length() == 0) {
				// delete previous null sized picture file
				toDownload.delete();
			}

			if (!toDownload.exists()) {
				// the file does not exist
				if (remoteFile == null) {
					return null;
				}
				download(localFile, remoteFile, listener);
			}
			// load picture from it's local location
			return new MonitoredCheckContent(MToolKit.getLocalPicture(localFile));
		} catch (InterruptedException e) {
			// IGNORED
			return null;
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		if (cardImage != null) {
			g.drawImage(cardImage, 0, 0, getWidth(), getHeight(), this);
		}
	}

	/**
	 * Return a scaled picture of the given one suiting to card dimension.
	 * 
	 * @param image
	 *          the original card picture.
	 * @return a scaled picture of the given one suiting to card dimension.
	 */
	public static BufferedImage getScaledImage(Image image) {
		BufferedImage buffImage = ((ToolkitImage) image).getBufferedImage();
		return ToolKit
				.getScaledInstance(buffImage, CardFactory.cardWidth,
						CardFactory.cardHeight, IdConst.BORDER_WIDTH,
						getBorderColor(buffImage));
	}

	/**
	 * Return the border color of this card. If the picture of this card is not
	 * <code>null</code> the returned color corresponds to the pixel placed on
	 * the topmost leftmost pixel.
	 * 
	 * @return the border color of this card.
	 */
	private static Color getBorderColor(BufferedImage image) {
		Color borderColor;
		// The border color is not yet cached
		if (CardFactory.borderColor != null) {
			// manual border
			borderColor = CardFactory.borderColor;
		} else {
			// auto border
			if (image != null) {
				borderColor = new Color(image.getRGB(0, 0));
				if (borderColor.getRed() > 175 && borderColor.getGreen() > 175
						&& borderColor.getBlue() > 175) {
					borderColor = Color.WHITE.darker();
				} else {
					borderColor = Color.BLACK.brighter();
				}
			} else {
				borderColor = CardFactory.borderColor;
			}
		}
		return borderColor;
	}

	/**
	 * card's image
	 */
	private Image cardImage;

	/**
	 * card's name
	 */
	private String cardName;

}