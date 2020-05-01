package geoprocessing.model.swmm5;

import java.net.URI;
import java.net.URISyntaxException;

import org.n52.javaps.description.TypedComplexOutputDescription;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.description.impl.TypedProcessDescriptionFactory;
import org.n52.javaps.description.impl.TypedProcessDescriptionImpl;
import org.n52.javaps.io.literal.LiteralType;
import org.n52.javaps.io.literal.xsd.LiteralDoubleType;
import org.n52.javaps.io.literal.xsd.LiteralStringType;
import org.n52.shetland.ogc.ows.OwsMetadata;
import org.n52.shetland.ogc.wps.Format;
import org.n52.shetland.ogc.wps.description.LiteralDataDomain;
import org.n52.shetland.ogc.wps.description.ProcessInputDescription;
import org.n52.shetland.ogc.wps.description.ProcessOutputDescription;
import org.n52.shetland.ogc.wps.description.impl.LiteralDataDomainImpl;

import com.geoprocessing.model.swmm.JSWMM;

import cn.edu.whu.model.IOType;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;

public class SWMMDescriptionGenerator {
	public final String identifier = "SWMM5";

	public final String input = "INPInput",timeStep="TimeStep", rptOut = "Report", resultOut = "Result", dateTimeParam = "datetime",
			rainfallParam = "rainfall", flowParam = "flow", depthParm = "depth";

	private JSWMM swmm;
	private TypedProcessDescriptionFactory descriptionFactory;
	private TypedProcessDescriptionImpl.Builder processBuilder;
	LiteralDataDomain literalDataDomain = new LiteralDataDomainImpl.Builder().build();

	public TypedProcessDescription createInitDescription() {
		if (this.descriptionFactory == null)
			this.descriptionFactory = new TypedProcessDescriptionFactory();

		this.processBuilder = this.descriptionFactory.process();

		Format inpFormat = new Format("text/inp");
		Format rptFormat = new Format("text/rpt");
		Format binaryFormat = new Format("application/binary");
		ProcessInputDescription inpinput = this.descriptionFactory.complexInput().withIdentifier(this.input)
				.withDefaultFormat(inpFormat).withSupportedFormat(inpFormat).withType(GeneralFileBinding.class).build();
		TypedComplexOutputDescription rptOut = this.descriptionFactory.complexOutput().withIdentifier(this.rptOut)
				.withTitle("report output").withDefaultFormat(rptFormat).withSupportedFormat(rptFormat)
				.withType(GeneralFileBinding.class).build();
		TypedComplexOutputDescription resultOut = this.descriptionFactory.complexOutput().withIdentifier(this.resultOut)
				.withTitle("result output").withDefaultFormat(binaryFormat).withSupportedFormat(binaryFormat)
				.withType(GeneralFileBinding.class).build();

		this.processBuilder = this.processBuilder.withIdentifier(identifier).withVersion("1.0.0").withInput(inpinput)
				.withOutput(rptOut).withOutput(resultOut);
		try {
			OwsMetadata commonIO = new OwsMetadata(new URI(IOType.METAHREF_COMMON), IOType.TITLE);
			this.processBuilder = this.processBuilder
					.withInput(createLiteralInput(this.timeStep, commonIO, new LiteralDoubleType()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return this.processBuilder.build();
	}

	public TypedProcessDescription updateDescription(JSWMM swmm) {
		this.swmm = swmm;
		try {
			for (String raingage : this.swmm.getRainGages()) 
				this.processBuilder.withInput(createGroupInput(raingage));
			
			for (String conduit : this.swmm.getLinks()) 
				this.processBuilder.withOutput(createGroupOutput(conduit));
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.processBuilder.build();
	}

	// raingage
	private ProcessInputDescription createGroupInput(String identifier) throws URISyntaxException {
		OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME), IOType.TITLE);
		OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME), IOType.TITLE);
		OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY), IOType.TITLE);

		return this.descriptionFactory.groupInput().withMinimalOccurence(0).withIdentifier(identifier)
				.withMetadata(runTimeIO)
				.withInput(createLiteralInput(this.rainfallParam, quantityIO, new LiteralDoubleType(), 0))
				.withInput(createLiteralInput(this.dateTimeParam, timeIO, new LiteralStringType(), 0)).build();
	}

	// conduits
	private ProcessOutputDescription createGroupOutput(String identifier) throws URISyntaxException {
		OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME), IOType.TITLE);
		OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME), IOType.TITLE);
		OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY), IOType.TITLE);

		return this.descriptionFactory.groupOutput().withIdentifier(identifier).withMetadata(runTimeIO)
				.withOutput(createLiteralOutput(this.dateTimeParam, quantityIO, new LiteralStringType()))
				.withOutput(createLiteralOutput(this.flowParam, quantityIO, new LiteralStringType()))
				.withOutput(createLiteralOutput(this.depthParm, timeIO, new LiteralStringType())).build();
	}

	private ProcessInputDescription createLiteralInput(String identifier, OwsMetadata commonIO, LiteralType<?> dataType)
			throws URISyntaxException {
		return this.descriptionFactory.literalInput().withIdentifier(identifier).withMetadata(commonIO)
				.withDefaultLiteralDataDomain(literalDataDomain).withType(dataType).build();
	}

	private ProcessInputDescription createLiteralInput(String identifier, OwsMetadata commonIO, LiteralType<?> dataType,
			int minioccur) throws URISyntaxException {
		return this.descriptionFactory.literalInput().withIdentifier(identifier).withMetadata(commonIO)
				.withDefaultLiteralDataDomain(literalDataDomain).withMinimalOccurence(minioccur).withType(dataType)
				.build();
	}

	private ProcessOutputDescription createLiteralOutput(String identifier, OwsMetadata commonIO,
			LiteralType<?> dataType) throws URISyntaxException {
		return this.descriptionFactory.literalOutput().withIdentifier(identifier).withMetadata(commonIO)
				.withDefaultLiteralDataDomain(literalDataDomain).withType(dataType).build();
	}

}
