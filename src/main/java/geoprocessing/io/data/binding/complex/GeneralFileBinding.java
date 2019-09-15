package geoprocessing.io.data.binding.complex;

import java.io.File;

import org.n52.javaps.io.complex.ComplexData;

/**
 * This class wraps a local file.
 * @author mingda zhang
 *
 */
public class GeneralFileBinding implements ComplexData<File> {

    /**
     *
     */
    private static final long serialVersionUID = 3415522592135759594L;
    private File geom;

    public GeneralFileBinding(File geom){
        this.geom = geom;
    }

    public File getPayload() {
        return this.geom;
    }

    public Class<?> getSupportedClass() {
        return File.class;
    }

    public void dispose() {

    }

}
