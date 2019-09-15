package geoprocessing.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.javaps.annotation.Properties;
import org.n52.javaps.description.TypedProcessOutputDescription;
import org.n52.javaps.io.AbstractPropertiesInputOutputHandler;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.EncodingException;
import org.n52.javaps.io.OutputHandler;
import org.n52.shetland.ogc.wps.Format;

import geoprocessing.io.data.binding.complex.GeneralFileBinding;

/**
 * This class generates a inputstream from the local file.
 * TODO return the local file directly
 * @author mingda zhang
 *
 */
@Properties(
        defaultPropertyFileName = "localfile.properties")
public class GeneralFileGenerator extends AbstractPropertiesInputOutputHandler implements OutputHandler {

    public GeneralFileGenerator() {
        super();
        addSupportedBinding(GeneralFileBinding.class);
    }

    public InputStream generate(TypedProcessOutputDescription<?> description,
            Data<?> data,
            Format format) throws IOException, EncodingException {
        if (data instanceof GeneralFileBinding) {
            File g = ((GeneralFileBinding) data).getPayload();

            InputStream is = new FileInputStream(g);

            return is;
        }
        return null;
    }

}
