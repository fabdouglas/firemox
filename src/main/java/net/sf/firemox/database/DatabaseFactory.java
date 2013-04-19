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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.database.data.StringData;
import net.sf.firemox.database.propertyconfig.Cache;
import net.sf.firemox.database.propertyconfig.PropertyConfig;
import net.sf.firemox.database.propertyconfig.PropertyConfigFactory;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.FileFilterPlus;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import sun.awt.image.FileImageSource;
import sun.awt.image.URLImageSource;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class DatabaseFactory {

	private static final String CACHE_FILE_HEADER = "<?xml version='1.0' encoding='ISO-8859-1'?>";

	/**
	 * Unique instance of a XML database.
	 */
	private static XmlParser.Node config;

	/**
	 * The loaded proxy configurations. The order determine priorities.
	 */
	public static Proxy[] pictureProxies;

	/**
	 * The loaded proxy configurations. The order determine priorities.
	 */
	public static Proxy[] dataProxies;

	/**
	 * A blank blacked-image.
	 */
	public static Image blankImage;

	/**
	 * Available properties of current TBS for databases.
	 */
	public static Map<String, PropertyConfig> propertiesCacheConfig;

	/**
	 * The common back picture of the cards of the selected turn based system. Is
	 * <code>null</code> while this factory has not been initialized.
	 * 
	 * @see #init(InputStream)
	 */
	public static Image backImage;

	/**
	 * The common scaled back picture of the cards of the selected turn based
	 * system. Is <code>null</code> while this factory has not been initialized.
	 */
	public static BufferedImage scaledBackImage;

	/**
	 * The picture representing a damage for the selected turn based system. Is
	 * <code>null</code> while this factory has not been initialized.
	 * 
	 * @see #init(InputStream)
	 */
	public static Image damageImage;

	/**
	 * The picture representing a damage for the selected turn based system. Is
	 * <code>null</code> while this factory has not been initialized.
	 * 
	 * @see #init(InputStream)
	 */
	public static Image damageScaledImage;

	/**
	 * The source file field allowing to access the source file/URL of any
	 * picture.
	 */
	static Field sourceFile;

	static Field sourceUrl;

	/**
	 * All created instances of DatabaseCard.
	 */
	private static Map<Node, DatabaseCard> databaseCardCache = new HashMap<Node, DatabaseCard>();

	/**
	 * Return the DatabaseCard object from the given card name. This object is
	 * build from the cache or the available proxies if cache is empty for this
	 * card. If there is no available proxy or if all all failed, the null value
	 * is returned. In case of success, the cache is updated and further proxy
	 * call would be done.
	 * 
	 * @param pictureName
	 *          the picture name associated to the database object. May be null to
	 *          use the picture associated to the given card instead.
	 * @param cardModel
	 *          the card name (no translated one)
	 * @param constraints
	 *          constraints set {key,value}. May be null.
	 * @return the DatabaseCard object from the given card name.
	 */
	public static DatabaseCard getDatabase(String pictureName,
			CardModel cardModel, Map<String, String> constraints) {

		// first retrieve data from cache
		DatabaseCard cacheData = null;
		try {
			cacheData = getDatabaseFromCache(cardModel, constraints);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (cacheData != null && cacheData.isConsistent()) {
			// ok, this card is already in the cache, return it
			if (pictureName != null && pictureName.length() > 0) {
				// The picture of this card must be redefined
				cacheData = cacheData.clone(pictureName);
			}
			return cacheData;
		}

		// return new DatabaseCardEmpty(cardName);;

		// iterate data proxies according preferences
		for (Proxy dataProxy : dataProxies) {
			if (dataProxy != null) {
				try {
					// get data from this proxy
					final DatabaseCard res = getDatabaseFromProxy(dataProxy, cardModel,
							constraints);
					if (res == null) {
						return cacheData;
					}
					res.setPictureProxies(pictureProxies);

					// Complete the cache with the defined user properties
					if (constraints != null) {
						for (Map.Entry<String, String> key : constraints.entrySet()) {
							res.add(new StringData(new Cache(key.getKey()), key.getValue()));
						}
					}
					updateCache(res);
					return res;
				} catch (Exception e) {
					// this proxy failed, we ignore this error and try the next one
					e.printStackTrace();
					Log.info("Proxy-data failure for " + cardModel + " : "
							+ dataProxy.getName() + ", error=" + e.getMessage());
				}
			}
		}
		// No valid proxy, maybe no available connection
		return null;
	}

	/**
	 * Retrieve data from the cache. If this card is not stored in cache, null
	 * value is returned.
	 * 
	 * @param cardName
	 *          the card name (no translated one)
	 * @param constraints
	 *          constraints set{key,value}. May be null.
	 * @return the DatabaseCard object from the given card name from the cache.
	 */
	private static DatabaseCard getDatabaseFromCache(CardModel cardModel,
			Map<String, String> constraints) throws IOException, SAXException {
		if (config == null) {
			final XmlParser parser = new XmlParser();
			Configuration.loadTemplateTbsFile(IdConst.FILE_DATABASE_SAVED);
			config = parser.parse(MToolKit.getTbsUrl(IdConst.FILE_DATABASE_SAVED)
					.getPath());
		}

		final List<Node> cachedInstances = config.getNodes(cardModel.getKeyName());
		if (cachedInstances != null && !cachedInstances.isEmpty()) {
			final List<Node> validateCards = new ArrayList<Node>();

			// This card is already cached once, keep only the best one
			int bestScore = 0;
			for (Node cachedCard : cachedInstances) {
				// Iterate over instances of this card assuming constraints.
				int score = 0;
				if (constraints != null && !constraints.isEmpty()) {
					for (Map.Entry<String, String> constraintKey : constraints.entrySet()) {
						final String constraintValue = constraintKey.getValue();
						if (constraintValue == null || constraintValue.length() == 0) {
							// Invalid constraint ?!
							throw new InternalError("No value associated to property '"
									+ constraintKey.getKey() + "'");
						}
						// Check the constraint against cached data of this card
						final List<Node> properties = cachedCard.getNodes("property");
						if (properties != null && !properties.isEmpty()) {
							for (Node property : properties) {
								if (property.getAttribute("name").equalsIgnoreCase(
										constraintKey.getKey())
										&& property.getAttribute("value").equalsIgnoreCase(
												constraintValue)) {
									// the requested property has been found and does not match
									score++;
									break;
								}
							}
						}
					}
				} else {
					validateCards.add(cachedCard);
					break;
				}
				if (score > bestScore) {
					bestScore = score;
					validateCards.clear();
					validateCards.add(cachedCard);
				} else if (score == bestScore) {
					validateCards.add(cachedCard);
				}
			}
			if (!validateCards.isEmpty()) {
				// All constraints are valid for these nodes
				Node preferredData = null;
				if (dataProxies == null) {
					return getDatabaseObjectCard(cardModel, cachedInstances.get(0), true);
				}
				for (Proxy dataProxy : dataProxies) {
					for (Node node : validateCards) {
						if (dataProxy.getName().equals(node.getAttribute("proxy"))) {
							preferredData = node;
							break;
						}
					}
				}

				if (preferredData == null) {
					// Do not retry a GET on a preferred proxy even if non ONE matches
					preferredData = validateCards.get(0);
				}
				if (constraints == null) {
					return getDatabaseObjectCard(cardModel, preferredData, true);
				}
				return getDatabaseObjectCard(cardModel, preferredData,
						bestScore >= constraints.size());
			}
			/*
			 * The card has been found in the cache but many constraints made failed
			 * the extraction. We try the proxy 'update' and if failed too, then
			 * return the first instance of cached data.
			 */
			// TODO try proxies first
			// return getDatabaseObjectCard(cardModel, cachedInstances.get(0));
		}
		// This card is not yet cached, no database object created
		return null;
	}

	/**
	 * Retrieve data from the given proxy. If this card is not available throw
	 * this proxy, null value is returned.
	 * 
	 * @param proxy
	 *          the proxy retrieving data
	 * @param cardName
	 *          the card name (no translated one)
	 * @param constraints
	 *          constraints set{key,value}. May be null.
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @return the built database object.
	 */
	private static DatabaseCard getDatabaseFromProxy(Proxy proxy,
			CardModel cardModel, Map<String, String> constraints) throws IOException {
		return proxy.getDatabaseFromStream(cardModel, constraints);
	}

	private static DatabaseCard getDatabaseObjectCard(CardModel cardModel,
			Node preferredData, boolean consistent) {
		DatabaseCard databaseCard = databaseCardCache.get(preferredData);
		if (databaseCard != null) {
			// An existing instance has been found.
			databaseCard.updateCardModel(cardModel);
			return databaseCard;
		}
		cardModel.setLocalName(preferredData.getAttribute("local-name"));
		databaseCard = new DatabaseCard(cardModel, getProxy(preferredData
				.getAttribute("proxy")), pictureProxies);
		databaseCard.setConsistent(consistent);
		// Fill this new database object with the validated cached data
		if (preferredData.aList != null) {
			for (Object node : preferredData.aList) {
				if (node != null && node instanceof Node && ((Node) node).getAttribute("name") != null) {
					// Add this property to database object
					databaseCard.add(propertiesCacheConfig.get(
							((Node) node).getAttribute("name")).parseProperty(
							cardModel.getCardName(), (Node) node));
				}
			}
		}
		// Database object has been successfully filled, we save it in our cache
		databaseCardCache.put(preferredData, databaseCard);
		return databaseCard;
	}

	/**
	 * Return the proxy object giving it's name.<code>null</code> value is
	 * returned if the proxy has not been found or if the <code>proxyName</code>
	 * was <code>null</code>, empty, or no proxy were loaded (occurs in
	 * initComponent() method calls).
	 * 
	 * @param proxyName
	 *          the poxy's name
	 * @return the proxy object if found, <code>null</code> otherwise.
	 */
	public static Proxy getProxy(String proxyName) {
		if (proxyName != null && proxyName.length() > 0 && dataProxies != null) {
			for (Proxy proxy : dataProxies) {
				if (proxy != null && proxyName.equals(proxy.getName())) {
					return proxy;
				}
			}
		}
		return null;
	}

	/**
	 * Initialize property configurations from the dbStream. Existing proxies are
	 * also initialized.
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>properties[] [...]</li>
	 * </ul>
	 * 
	 * @param dbStream
	 *          the MDB file containing rules
	 * @throws IOException
	 *           error during the database configuration.
	 */
	public static void init(InputStream dbStream) throws IOException {
		String[] dataProxyNameOrders = Configuration.getString(
				"database.dataOrder", "").split("\\|");
		String[] pictureProxyNameOrders = Configuration.getString(
				"database.pictureOrder", "").split("\\|");
		int count = dbStream.read();
		propertiesCacheConfig = new HashMap<String, PropertyConfig>(count);
		while (count-- > 0) {
			PropertyConfig propertyConfig = PropertyConfigFactory
					.getPropertyConfig(dbStream);
			propertiesCacheConfig.put(propertyConfig.getName(), propertyConfig);
		}

		// list available proxies
		final File proxiesLocation = MToolKit.getTbsFile(IdConst.PROXIES_LOCATION);
		final File[] lproxies;
		if (proxiesLocation == null) {
			Log.warn("The proxy directory '"
					+ MToolKit.getTbsFile(IdConst.PROXIES_LOCATION, false)
					+ "' does not exist");
			lproxies = new File[0];
		} else {
			lproxies = proxiesLocation.listFiles(new FileFilterPlus("xml"));
		}
		dataProxies = new Proxy[lproxies.length];
		pictureProxies = new Proxy[lproxies.length];

		XmlTools.initHashMaps();

		// validate them and build 'Proxy' instances from XML
		for (int i = lproxies.length; i-- > 0;) {
			try {
				dataProxies[i] = new Proxy(lproxies[i]);
				pictureProxies[i] = dataProxies[i];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// re-order proxies with the defined orders
		int index = 0;
		for (String dataProxyNameOrder : dataProxyNameOrders) {
			for (int j = 0; j < dataProxies.length; j++) {
				if (dataProxyNameOrder.equalsIgnoreCase(dataProxies[j].getXmlName())) {
					final Proxy oldProxy = dataProxies[index];
					dataProxies[index] = dataProxies[j];
					dataProxies[j] = oldProxy;
					index++;
					break;
				}
			}
		}
		index = 0;
		for (String pictureProxyNameOrder : pictureProxyNameOrders) {
			for (int j = 0; j < pictureProxies.length; j++) {
				if (pictureProxyNameOrder.equalsIgnoreCase(pictureProxies[j]
						.getXmlName())) {
					final Proxy oldProxy = pictureProxies[index];
					pictureProxies[index] = pictureProxies[j];
					pictureProxies[j] = oldProxy;
					index++;
					break;
				}
			}
		}

		// Initialize source file accessibility
		try {
			sourceFile = FileImageSource.class.getDeclaredField("imagefile");
			sourceUrl = URLImageSource.class.getDeclaredField("url");
			AccessibleObject.setAccessible(new AccessibleObject[] { sourceFile },
					true);
			AccessibleObject
					.setAccessible(new AccessibleObject[] { sourceUrl }, true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		XmlTools.clean();
	}

	/**
	 * Save the cache into the database XML file.
	 */
	public static void saveCache() {
		if (config != null) {
			try {
				PrintWriter printer = new PrintWriter(Configuration
						.loadTemplateTbsFile(IdConst.FILE_DATABASE_SAVED));
				printer.println(CACHE_FILE_HEADER);
				config.print(printer);
				IOUtils.closeQuietly(printer);
			} catch (IOException e) {
				// Cache could not be saved
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update the cache with the given data.
	 * 
	 * @param databaseCard
	 *          the data of a card to cache in our XML data base.
	 */
	private static void updateCache(DatabaseCard databaseCard) {
		if (config == null) {
			throw new IllegalStateException("Cache should be initialized");
		}
		databaseCard.updateCache(config);
	}

	/**
	 * Prevent this class to be instantiated.
	 */
	private DatabaseFactory() {
		// Nothing to do
	}
}
