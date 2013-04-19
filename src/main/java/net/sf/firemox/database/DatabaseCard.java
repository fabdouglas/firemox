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
package net.sf.firemox.database;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.clickable.target.card.CardModelLazy;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.database.data.TranslatableData;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.management.MonitorListener;
import net.sf.firemox.management.MonitoredCheckContent;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlParser.Node;
import sun.awt.image.FileImageSource;
import sun.awt.image.URLImageSource;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class DatabaseCard {

	private Map<String, TranslatableData> data = new HashMap<String, TranslatableData>(
			5);

	/**
	 * The proxy used to build this object.
	 */
	private final Proxy dataProxy;

	/**
	 * The proxy used to display picture of this object. If is null,
	 * <code>dataProxy</code> will be used.
	 */
	private Proxy[] pictureProxies;

	/**
	 * The currently associated image of this card. Is <code>null</code> while
	 * not loaded.
	 */
	private MonitoredCheckContent image;

	/**
	 * The currently associated scaled image of this card. Is <code>null</code>
	 * while not loaded.
	 */
	private Image scaledImage;

	/**
	 * The card model of this card.
	 */
	private CardModel cardModel;

	/**
	 * The consistency of this database.
	 */
	private boolean consistent;

	/**
	 * Create new instance of DatabaseCard.
	 * 
	 * @param cardModel
	 *          the card model containing card name used as id
	 * @param dataProxy
	 *          the proxy where this data came from. If is <code>null</code>,
	 *          the picture would be get using the default art URL.
	 * @param pictureProxies
	 *          the proxies used to get image. If is <code>null</code>,
	 *          <code>dataProxy</code> would be used.
	 */
	public DatabaseCard(CardModel cardModel, Proxy dataProxy,
			Proxy... pictureProxies) {
		this.cardModel = cardModel;
		this.dataProxy = dataProxy;
		setPictureProxies(pictureProxies);
	}

	/**
	 * Return the card model of this card.
	 * 
	 * @return the card model of this card.
	 */
	public CardModel getCardModel() {
		return cardModel;
	}

	/**
	 * Return the proxy used to build this object.
	 * 
	 * @return the proxy used to build this object.
	 */
	public Proxy getDataProxy() {
		return dataProxy;
	}

	/**
	 * Return the proxy used to display picture of this object. If is null,
	 * <code>dataProxy</code> will be used.
	 * 
	 * @return the proxy used to display picture of this object. If is null,
	 *         <code>dataProxy</code> will be used.
	 */
	public Proxy getPictureProxy() {
		return pictureProxies[0];
	}

	/**
	 * Return the proxies used to display picture of this object. If is null,
	 * <code>dataProxy</code> will be used.
	 * 
	 * @return the proxies used to display picture of this object. If is null,
	 *         <code>dataProxy</code> will be used.
	 */
	public Proxy[] getPictureProxies() {
		return pictureProxies;
	}

	/**
	 * The card name. Is considered as id. Return the English name, not the
	 * localized one.
	 * 
	 * @return the card name. Is considered as id.
	 * @see net.sf.firemox.clickable.target.card.CardModel#getCardName()
	 */
	public String getCardName() {
		return cardModel.getCardName();
	}

	/**
	 * Return the localized card's name.
	 * 
	 * @return the localized card's name.
	 * @see net.sf.firemox.clickable.target.card.CardModel#getLocalName()
	 */
	public String getLocalName() {
		return cardModel.getLocalName();
	}

	/**
	 * Return XML rule designer of the card.
	 * 
	 * @return XML rule designer of the card.
	 * @see net.sf.firemox.clickable.target.card.CardModel#getRulesCredit()
	 */
	public String getRulesCredit() {
		return cardModel.getRulesCredit();
	}

	/**
	 * Add a translatable data to this database.
	 * 
	 * @param data
	 *          translatable data to add.
	 */
	public void add(TranslatableData data) {
		if ("local-name".equalsIgnoreCase(data.getValue())) {
			cardModel.setLocalName(data.getValue());
		}
		this.data.put(data.getPropertyName(), data);
	}

	/**
	 * Return the card's picture as it would be displayed in the board.
	 * 
	 * @param card
	 *          the card requesting it's picture.
	 * @return the card's picture as it would be displayed in the board.
	 */
	public Image getImage(AbstractCard card) {
		if (!card.isVisibleForYou()) {
			return DatabaseFactory.backImage;
		}
		return getImage((MonitorListener) card);
	}

	/**
	 * Return the scaled card's picture as it would be displayed in the board.
	 * 
	 * @param card
	 *          the card requesting it's picture.
	 * @return the scaled card's picture as it would be displayed in the board.
	 */
	public Image getScaledImage(AbstractCard card) {
		if (!card.isVisibleForYou()) {
			return DatabaseFactory.scaledBackImage;
		}
		return getScaledImage((MonitorListener) card);
	}

	/**
	 * Return the card's picture as it would be displayed in the board.
	 * 
	 * @param listener
	 *          the listener to notify when the picture will be completely loaded.
	 * @return the card's picture as it would be displayed in the board.
	 */
	public Image getImage(MonitorListener listener) {
		if (image == null) {
			loadDatabasePicture(null, listener);
		} else if (!image.isFinished()) {
			image.addListener(listener);
		}
		return image.getContent();
	}

	/**
	 * Return the scaled card's picture as it would be displayed in the board.
	 * 
	 * @param listener
	 *          the listener to notify when the picture will be completely loaded.
	 * @return the scaled card's picture as it would be displayed in the board.
	 */
	public Image getScaledImage(MonitorListener listener) {
		if (scaledImage == null) {
			if (image == null) {
				getImage(listener);
			}
			if (!image.isFinished()) {
				return image.getContent();
			}
			scaledImage = Picture.getScaledImage(image.getContent());
		}
		return scaledImage;
	}

	/**
	 * Invalidate the card picture
	 */
	public synchronized void invalidateImage() {
		image = null;
		scaledImage = null;
	}

	/**
	 * Return the stream description used to build the picture by this database
	 * object.
	 * 
	 * @return the stream description used to build the picture by this database
	 *         object.
	 */
	public String getPictureStream() {
		if (image != null && DatabaseFactory.sourceFile != null
				&& image.getContent() != null) {
			final ImageProducer imageProducer = image.getContent().getSource();
			if (imageProducer instanceof FileImageSource) {
				try {
					return DatabaseFactory.sourceFile.get(imageProducer).toString();
				} catch (Exception e) {
					return "unavailable stream";
				}
			} else if (imageProducer instanceof URLImageSource) {
				try {
					return DatabaseFactory.sourceUrl.get(imageProducer).toString();
				} catch (Exception e) {
					return "unavailable stream";
				}
			}
		}
		// TODO internationalized "unavailable stream"
		return "unavailable stream";
	}

	/**
	 * Return the default local picture of this card model. The proxies are not
	 * used to get this image.
	 * 
	 * @return <code>null</code> if there is no local image.A non
	 *         <code>null</code> object otherwise.
	 * @throws MalformedURLException
	 */
	private MonitoredCheckContent getDefaultImage() throws MalformedURLException {
		return Picture.loadImage(MToolKit.getTbsPicture(cardModel.getKeyName()
				+ ".jpg", false), null);

	}

	/**
	 * Load the image considering proxy, properties and the given picture name. If
	 * no proxy is set for this database, the picture is loaded from the
	 * 'tbs/${tbs.name}/images' directory. The picture filename is either the
	 * specified <param>pictureName</param> if not null, either the built from
	 * the card name. The looked for picture type is '.jpg'.<br>
	 * If a proxy is set, then the proxy will build the URL where the picture can
	 * been found first locally, then remotely from the proxy web-site.
	 * 
	 * @param pictureName
	 *          the alternative picture name.
	 * @param listener
	 *          the card requesting this picture.
	 */
	private synchronized void loadDatabasePicture(String pictureName,
			MonitorListener listener) {
		if (image != null) {
			// since the call to this method, the picture has been loaded.
			image.addListener(listener);
			return;
		}
		try {
			if (pictureProxies == null) {
				/*
				 * Since the proxy has not been provided, the picture will be saved in
				 * the share 'tbs/xxx/images' place
				 */
				if (pictureName != null) {
					// a special picture has been specified. It must be local.
					image = Picture.loadImage(MToolKit
							.getTbsPicture(pictureName + ".jpg"), null);
				} else {
					// The default art URL will be used if the picture is not yet local.
					image = Picture.loadImage(MToolKit.getTbsPicture(cardModel
							.getKeyName()
							+ ".jpg", false), new URL(MdbLoader.getArtURL() + "/"
							+ cardModel.getKeyName() + ".jpg"));
				}
			} else {

				/**
				 * The proxy has not been provided, we try to match one. <br>
				 * First with the local one, then with the remote ones.<br>
				 * The picture will be saved in the proxy private location
				 * 'tbs/XXX/images/proxies/${proxy.name}/${picture.URL less
				 * proxy.baseURL}/' place
				 */
				image = getDefaultImage();
				if (image != null) {
					return;
				}

				List<String> localPaths = new ArrayList<String>();
				for (Proxy pictureProxy : pictureProxies) {
					for (String path : pictureProxy.getLocalPictures(cardModel, data)) {
						if (path != null) {
							image = Picture.loadImage(path, null);
							if (image != null) {
								return;
							}
						}
						localPaths.add(path);
					}
				}

				List<String> remotePaths = new ArrayList<String>();
				for (Proxy pictureProxy : pictureProxies) {
					remotePaths.addAll(pictureProxy.getRemotePictures(cardModel, data));
				}

				// even the card.id is not provided or another problem occurred
				if (pictureName != null) {
					// a special picture has been specified. It must be local.
					image = Picture.loadImage(MToolKit
							.getTbsPicture(pictureName + ".jpg"), null);
				} else {
					// The default art URL will be used if the picture is not yet local.
					localPaths.add(MToolKit.getTbsPicture(
							cardModel.getKeyName() + ".jpg", false));
					remotePaths.add(MdbLoader.getArtURL() + "/" + cardModel.getKeyName()
							+ ".jpg");
				}

				if (image == null) {
					image = new MonitoredCheckContent(localPaths, remotePaths, listener);
					image.start();
				}
			}
		} catch (MalformedURLException e) {
			try {
				image = Picture.loadImage(MToolKit.getTbsPicture(cardModel.getKeyName()
						+ ".jpg"), null);
			} catch (MalformedURLException ex) {
				Log.debug("Error during picture load of " + cardModel.getKeyName(), ex);
			}
		}

		if (image == null) {
			// picture load failed, return the default picture.
			image = new MonitoredCheckContent(DatabaseFactory.backImage);
		}

	}

	/**
	 * Create a node representing this data inside the given node.
	 * 
	 * @param inNode
	 *          the node where data would be added to.
	 */
	void updateCache(Node inNode) {

		// build attributes
		XmlParser.Attribute[] attributes = new XmlParser.Attribute[2];
		attributes[0] = new XmlParser.Attribute("local-name", getLocalName());
		attributes[1] = new XmlParser.Attribute("proxy", dataProxy.getName());

		// build node
		Node node = new XmlParser.Node(inNode, cardModel.getKeyName(), null);
		node.aAttrs = attributes;

		// add properties
		for (TranslatableData dataIt : data.values()) {
			XmlParser.Attribute[] values = new XmlParser.Attribute[2];
			values[0] = new XmlParser.Attribute("name", dataIt.getPropertyName());
			values[1] = new XmlParser.Attribute("value", dataIt.getValue());
			Node property = new XmlParser.Node(node, "property", null);
			property.aAttrs = values;
			node.add(0, property);
		}
		inNode.aList.add(0, node);
	}

	/**
	 * Create a new instance of DatabaseCard with a picture different form the
	 * standard one (relative to card name). No proxy would be used for this new
	 * instance.<br>
	 * The given picture is immediately loaded.
	 * 
	 * @param pictureName
	 *          the picture to use with this DatabaseCard
	 * @return a clone of this instance, without proxy and with the specified
	 *         picture loaded immediately.
	 */
	public DatabaseCard clone(String pictureName) {
		final DatabaseCard clone = new DatabaseCard(cardModel, null);
		clone.loadDatabasePicture(pictureName, null);
		return clone;
	}

	@Override
	public DatabaseCard clone() {
		return clone(cardModel);
	}

	/**
	 * Return a clone of this object from the given card model.
	 * 
	 * @param cardModel
	 * @return a clone of this object from the given card model.
	 */
	public DatabaseCard clone(CardModel cardModel) {
		final DatabaseCard clone = new DatabaseCard(cardModel, dataProxy,
				pictureProxies);
		return clone;
	}

	/**
	 * Return the translated data associated to the named property.
	 * 
	 * @param property
	 *          the property name.
	 * @return the translated data associated to the named property.
	 */
	public String getProperty(String property) {
		final TranslatableData data = this.data.get(property);
		if (data != null) {
			return data.getTranslatedValue(dataProxy);
		}
		return "-";
	}

	/**
	 * Set the proxy used to display picture of this object. If is null,
	 * <code>dataProxy</code> will be used.
	 * 
	 * @param pictureProxies
	 *          the new proxies used for picture. Order corresponds to preference.
	 */
	public void setPictureProxies(Proxy... pictureProxies) {
		if (this.pictureProxies != pictureProxies) {
			if (pictureProxies == null) {
				this.pictureProxies = new Proxy[] { dataProxy };
			} else {
				this.pictureProxies = pictureProxies;
			}
			image = null;
		}
	}

	@Override
	public String toString() {
		return cardModel.toString();
	}

	/**
	 * Set the consistency of this database.
	 * 
	 * @param consistent
	 *          the new consistency of this database.
	 */
	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}

	/**
	 * Return the new score of this database.
	 * 
	 * @return true if this database is consistent.
	 */
	public boolean isConsistent() {
		return consistent;
	}

	/**
	 * This method update and paint on the given card, the progress bar of task of
	 * the image attached to this model.
	 * 
	 * @param card
	 *          the listener to manage.
	 * @param g
	 *          the graphics used to paint the progress bar.
	 */
	public void updatePaintNotification(MCard card, Graphics g) {
		if (image == null || !image.isFinished()) {
			// The displayed picture is not completely load, we display a progress bar
			if (image != null && card.isVisibleForYou()) {
				image.paintNotification(g);
			}
		} else if (image.isFinished()) {
			image.acknowledgeFinished(card);
		}
	}

	/**
	 * This method update and paint on the given listener, the progress bar of
	 * task of the image attached to this model.
	 * 
	 * @param listener
	 *          the listener to manage.
	 * @param g
	 *          the graphics used to paint the progress bar.
	 */
	public void updatePaintNotification(MonitorListener listener, Graphics g) {
		if (image == null || !image.isFinished()) {
			// The displayed picture is not completely load, we display a progress bar
			if (image != null) {
				image.paintNotification(g);
			}
		} else if (image.isFinished()) {
			image.acknowledgeFinished(listener);
		}
	}

	/**
	 * Reset the buffered data.
	 */
	public void updateMUI() {
		scaledImage = null;
	}

	/**
	 * Update the card model if needed.
	 * 
	 * @param cardModel
	 *          the new card model.
	 */
	public void updateCardModel(CardModel cardModel) {
		if (cardModel != null && this.cardModel instanceof CardModelLazy) {
			this.cardModel = cardModel;
		}
	}

}
