package geoprocessing.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

/**
 * unzip the file and returns the workable file contained. application/x-zipped-shp --> shp
 * @author mingda zhang
 *
 */
public class ExtensionsProperties {
	private static final String SUFFIX_PROPERTIES_FILE = "/extensions.properties";
	private static final Logger LOG = LoggerFactory.getLogger(ExtensionsProperties.class);
	private static Properties suffixProperties;

	public static String suffix(String mimetype) {
		if (suffixProperties == null)
			suffixProperties = loadProperties(SUFFIX_PROPERTIES_FILE);

		return suffixProperties.getProperty(mimetype);
	}

	private static Properties loadProperties(String path) {
		URL url = Resources.getResource(path);
		ByteSource source = Resources.asByteSource(url);
		Properties properties = new Properties();
		try (InputStream in = source.openStream()) {
			LOG.info("Loading suffix properties from {}", url);
			properties.load(in);
		} catch (IOException e) {
			throw new Error("Could not load properties", e);
		}
		return properties;
	}
}
