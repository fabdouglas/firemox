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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.database.data.TranslatableData;
import net.sf.firemox.database.propertyconfig.PropertyConfig;
import net.sf.firemox.database.propertyconfig.PropertyProxyConfig;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.xml.XmlParser;
import net.sf.firemox.xml.XmlParser.Node;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 * @since 0.90
 */
public class Proxy {

	/**
	 * Private declaration of this proxy.
	 */
	private final Map<String, Map<String, String>> aliases;

	/**
	 * The stream base of stream URLs.
	 */
	private String streamBaseUrl;

	/**
	 * The URL where data would be build from.
	 */
	private List<UrlTokenizer> streams;

	/**
	 * The language of this proxy.
	 */
	private String language;

	/**
	 * The encoding of stream retrieved from the proxy URL.
	 */
	private String encoding;

	/**
	 * The proxy's name as displayed in the GUI menus
	 */
	private String name;

	/**
	 * The proxy web site providing the data. Is only here for information
	 * purpose.
	 */
	private String home;

	/**
	 * The pictures configuration of this stream.
	 */
	public List<PictureConfiguration> pictures = new ArrayList<PictureConfiguration>();

	/**
	 * The properties managed by this proxy.
	 */
	private List<PropertyConfig> properties;

	/**
	 * The XML name of this proxy. The name corresponds to the XML file definition
	 * of this proxy
	 */
	private String xmlName;

	/**
	 * The proxy's name as displayed in the GUI menus
	 * 
	 * @return The proxy's name as displayed in the GUI menus
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Return the XML name of this proxy. The name corresponds to the XML file
	 * definition of this proxy
	 * 
	 * @return the XML name of this proxy.
	 */
	public String getXmlName() {
		return xmlName;
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param xmlFile
	 *          the definition file of this proxy.
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @throws SAXException
	 *           If some XML parse error occurs
	 */
	public Proxy(File xmlFile) throws IOException, SAXException {
		final XmlParser parser = new XmlParser();
		final Node config = parser.parse(new FileInputStream(xmlFile));
		xmlName = StringUtils.removeEnd(xmlFile.getName().toLowerCase(), ".xml");
		name = config.getAttribute("name");
		encoding = config.getAttribute("encoding");
		language = config.getAttribute("language");
		home = config.getAttribute("home");

		final List<?> pictures = config.get("pictures").getNodes("picture");
		for (int i = 0; i < pictures.size(); i++) {
			final Node pictureStream = (Node) pictures.get(i);
			this.pictures.add(new PictureConfiguration(
					new UrlTokenizer(pictureStream), pictureStream.getAttribute("base")));
		}

		// read private aliases
		final Node aliases = config.get("alias");
		this.aliases = new HashMap<String, Map<String, String>>();
		if (aliases != null) {
			List<Node> nodes = aliases.getNodes("alias");
			for (int i = nodes.size(); i-- > 0;) {
				final Node alias = nodes.get(i);
				Map<String, String> nameSpace = this.aliases.get(alias
						.getAttribute("property"));
				if (nameSpace == null) {
					nameSpace = new HashMap<String, String>();
					this.aliases.put(alias.getAttribute("property"), nameSpace);
				}
				nameSpace.put(alias.getAttribute("local-value").toLowerCase(), alias
						.getAttribute("ref"));
			}
		}

		// read streams configurations
		Node dataConfig = config.get("data");
		Node streamConfig = dataConfig.get("streams");
		if (streamConfig != null) {
			streamBaseUrl = streamConfig.getAttribute("base");
			final List<Node> streamsNode = streamConfig.getNodes("stream");
			streams = new ArrayList<UrlTokenizer>(streamsNode.size());
			for (int i = 0; i < streamsNode.size(); i++) {
				streams.add(new UrlTokenizer(streamsNode.get(i)));
			}
			if (streams.isEmpty()) {
				throw new RuntimeException(
						"At least one stream configuration must be defined");
			}

			// read properties configurations
			final List<Node> nodes = dataConfig.get("properties")
					.getNodes("property");
			properties = new ArrayList<PropertyConfig>(nodes.size());
			for (int i = 0; i < nodes.size(); i++) {
				properties.add(new PropertyProxyConfig(nodes.get(i)));
			}
		} else {
			streams = new ArrayList<UrlTokenizer>(0);
			properties = new ArrayList<PropertyConfig>(0);
		}
	}

	/**
	 * @param cardModel
	 *          the card model.
	 * @param constraints
	 *          the constraints.
	 * @return the string read from one of the streams of this proxy.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	private String getStringFromStream(CardModel cardModel,
			Map<String, String> constraints) throws IOException {

		// Determine the best stream configuration
		int highestScore = -1;
		UrlTokenizer stream = null;
		for (int i = 0; i < streams.size(); i++) {
			int score = streams.get(i).getUrlScore(constraints);
			if (score > highestScore) {
				highestScore = score;
				stream = streams.get(i);
			}
		}

		// No stream available for this proxy + card
		if (stream == null) {
			return null;
		}

		// read stream from the built URL
		final URL mainPage = new URL(streamBaseUrl
				+ stream.getUrl(cardModel, constraints, this));
		final StringBuilder res = new StringBuilder(2000);
		final InputStream proxyStream;
		try {
			proxyStream = MToolKit.getHttpConnection(mainPage).getInputStream();
		} catch (Throwable e) {
			// Error during the IP get
			throw new IOException(LanguageManager.getString("error.stream.null"));
		}
		if (proxyStream == null) {
			// Error during the IP get
			throw new IOException(LanguageManager.getString("error.stream.null"));
		}
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				proxyStream, encoding));
		String line = null;
		while ((line = br.readLine()) != null) {
			res.append(StringUtils.trim(line));
		}
		return res.toString();
	}

	/**
	 * Create a new DatabaseCard from the given CardModel. The best stream is
	 * determined depending the given constraints. The associated picture is also
	 * will be downloaded and loaded only during the first display of this
	 * picture.
	 * 
	 * @param cardModel
	 *          the object containing the card name.
	 * @param constraints
	 *          set of constraints
	 * @return a new DatabaseCard from the given CardModel.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public DatabaseCard getDatabaseFromStream(CardModel cardModel,
			Map<String, String> constraints) throws IOException {
		final DatabaseCard databaseCard = new DatabaseCard(cardModel, this,
				DatabaseFactory.pictureProxies);

		// Find the best stream
		final String stream = getStringFromStream(cardModel, constraints);

		// Add parsed properties managed by this proxy
		PropertyProxyConfig.values.clear();
		PropertyProxyConfig.values.put("%last-offset", new IntValue(0));
		for (PropertyConfig property : properties) {
			final TranslatableData data = property.parseProperty(cardModel
					.getCardName(), stream, this);
			if (data != null) {
				databaseCard.add(data);
			}
		}

		return databaseCard;
	}

	/**
	 * Return the encoding of stream retrieved from the proxy URL.<br>
	 * Unreferenced method, but called with reflection.
	 * 
	 * @return the encoding of stream retrieved from the proxy URL.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Return the proxy web site providing the data. Is only here for information
	 * purpose.<br>
	 * Unreferenced method, but called with reflection.
	 * 
	 * @return the proxy web site providing the data.
	 */
	public String getHome() {
		return home;
	}

	/**
	 * Return the language of this proxy.<br>
	 * Unreferenced method, but called with reflection.
	 * 
	 * @return the language of this proxy.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Return the referenced value of this proxy. If this proxy do not define the
	 * named alias, return the given <param>localValue</param>.
	 * 
	 * @param nameSpace
	 *          the name space (property)
	 * @param localValue
	 *          the private-proxy value
	 * @return the referenced value of this proxy.
	 */
	public String getGlobalValueFromLocal(String nameSpace, String localValue) {
		if (localValue.length() > 1000) {
			// This property may contains some invalid data
			return "Too long text (" + localValue.length() + ")";
		}

		final Map<String, String> alias = aliases.get(nameSpace);
		if (alias != null && alias.containsKey(localValue.toLowerCase())) {
			return alias.get(localValue.toLowerCase());
		}
		return localValue;
	}

	/**
	 * Return the referenced value of this proxy. If this proxy do not define the
	 * named alias, return the given <param>localValue</param>.
	 * 
	 * @param nameSpace
	 * @param localValue
	 * @return the referenced value of this proxy.
	 */
	public String getLocalValueFromGlobal(String nameSpace, String localValue) {
		final Map<String, String> alias = aliases.get(nameSpace);
		if (alias != null && alias.containsValue(localValue.toLowerCase())) {
			for (Map.Entry<String, String> entry : alias.entrySet()) {
				if (entry.getValue().equals(localValue.toLowerCase())) {
					return entry.getKey();
				}
			}
		}
		return localValue;
	}

	/**
	 * Return a list of remote picture paths considering proxy, properties and the
	 * given card. The returned order corresponds to the priority.
	 * 
	 * @param cardModel
	 *          the card model.
	 * @param data
	 *          the translated data.
	 * @return a list of remote picture paths.
	 */
	public List<String> getRemotePictures(CardModel cardModel,
			Map<String, TranslatableData> data) {
		List<String> res = new ArrayList<String>(pictures.size());
		for (PictureConfiguration p : pictures) {
			try {
				final String localUrl = p.getPictureUrl().getUrl(cardModel, data, this);
				res.add(p.getProxyBaseUrl() + "/" + localUrl);
			} catch (Exception e) {
				// Ignore this error, we'll not add this URL
				res.add(null);
			}
		}
		return res;
	}

	/**
	 * Return a list of local picture paths considering proxy, properties and the
	 * given card. The returned order corresponds to the priority.
	 * 
	 * @param cardModel
	 *          the card model.
	 * @param data
	 *          the translated data.
	 * @return a list of local picture paths.
	 */
	public List<String> getLocalPictures(CardModel cardModel,
			Map<String, TranslatableData> data) {
		List<String> res = new ArrayList<String>(pictures.size());
		for (PictureConfiguration p : pictures) {
			try {
				final String localUrl = p.getPictureUrl().getUrl(cardModel, data, this);
				res.add(MToolKit.getTbsPicture(IdConst.PROXIES_LOCATION + "/"
						+ getValidPath(getXmlName()) + "/" + localUrl, false));
			} catch (Exception e) {
				// Ignore this error, we'll not add this URL
				res.add(null);
			}

		}
		return res;
	}

	/**
	 * Simple method returning a suitable path for Windows, Linux,...
	 * 
	 * @param path
	 *          any non null string
	 * @return a suitable path for Windows, Linux,...
	 */
	private String getValidPath(String path) {
		return path.replace('/', '_').replace(':', '_').replace('*', '_').replace(
				'\\', '_').replace('%', '_').replace('"', '_').replace('\'', '_')
				.replace('&', '_').replace('$', '_').replace('~', '_');
	}

}
