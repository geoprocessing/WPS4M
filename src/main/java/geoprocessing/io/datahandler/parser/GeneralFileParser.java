package geoprocessing.io.datahandler.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
import org.n52.javaps.annotation.Properties;
import org.n52.javaps.description.TypedProcessInputDescription;
import org.n52.javaps.io.AbstractPropertiesInputOutputHandler;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.DecodingException;
import org.n52.javaps.io.InputHandler;
import org.n52.javaps.settings.SettingsConstants;
import org.n52.shetland.ogc.wps.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import geoprocessing.io.data.binding.complex.GeneralFileBinding;
import geoprocessing.util.ExtensionsProperties;

/**
 * This class saves the online data into local disc, and returns the workable file path (including unzip the files).
 *
 * @author mingda zhang
 *
 */
@Configurable
@Properties(
        defaultPropertyFileName = "localfile.properties")
public class GeneralFileParser extends AbstractPropertiesInputOutputHandler implements InputHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(GeneralFileParser.class);

    private Path basePath;
    
    public GeneralFileParser() {
        super();
        addSupportedBinding(GeneralFileBinding.class);
    }

    @Setting(SettingsConstants.MISC_BASE_DIRECTORY)
    public void setBasePath(File baseDirectory) {
        this.basePath = baseDirectory.toPath();
    }
    
    public Data<?> parse(TypedProcessInputDescription<?> description,
            InputStream input,
            Format format) throws IOException, DecodingException {
    	
    	if(format.isEmpty()) {
    		if(description.isComplex()) {
    			format = description.asComplex().getDefaultFormat();
    		}else {
				format = Format.TEXT_XML;
			}
    	}
    	
    	String uuid = UUID.randomUUID().toString();
    	Path directory = Files.createDirectory(basePath.resolve(uuid));
    	String prefix = description.getId().getValue();
    	
    	Optional<String> mimeType = null;
    	if(!format.isEmpty()) {
    		mimeType = format.getMimeType();
    	}
    	
    	String extension = null;
    	if(mimeType.isPresent()) {
    		extension = ExtensionsProperties.suffix(mimeType.get());
    	}
    	
    	extension = extension == null?"tmp":extension;
    	String fileName = prefix+"."+extension;
    	
    	Path outputFile = directory.resolve(fileName);
    			//Files.createTempFile(directory, null, null);
    	Files.createFile(outputFile);
    	
    	LOGGER.info("save the file into {}", outputFile.toString());
    	Files.copy(input, outputFile, StandardCopyOption.REPLACE_EXISTING);
    	
    	return new GeneralFileBinding(outputFile.toFile());
    	
		/*
		 * try { Geometry g = new WKTReader().read(new InputStreamReader(input));
		 * 
		 * 
		 * 
		 * return new GeneralFileBinding(file);
		 * 
		 * } catch (ParseException e) { LOGGER.error(e.getMessage(), e); } finally { try
		 * { input.close(); } catch (IOException e) { LOGGER.error(e.getMessage(), e); }
		 * }
		 */
    }

}
