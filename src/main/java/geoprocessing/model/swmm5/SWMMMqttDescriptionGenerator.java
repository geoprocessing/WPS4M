package geoprocessing.model.swmm5;

import cn.edu.whu.model.IOType;
import com.geoprocessing.model.swmm.JSWMM;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;
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

import java.net.URI;
import java.net.URISyntaxException;

public class SWMMMqttDescriptionGenerator {
	public final String identifier = "SWMMMqtt";

	public final String input = "INPInput",timeStep="TimeStepInSecond",startTime="StartTime",endTime="EndTime", rptOut = "Report", resultOut = "Result", dateTimeParam = "datetime",
			rainfallParam = "rainfall", flowParam = "flow", depthParm = "depth",mqttoutput="MqttOutput";

	public final String RainGauge = "RainGauge", Junctions ="Junctions", Subcatchments ="Subcatchments",Conduits="Conduits";


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

		this.processBuilder = this.processBuilder.withIdentifier(identifier).withVersion("1.0.0").withInput(inpinput);


		try {
			OwsMetadata commonIO = new OwsMetadata(new URI(IOType.METAHREF_COMMON), IOType.TITLE);
			OwsMetadata mqttio = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);
			//the start time, end time and time step. The time step refers to the
			this.processBuilder = this.processBuilder
					.withInput(createLiteralInput(this.timeStep, commonIO, new LiteralDoubleType()))
					.withInput(createLiteralInput(this.startTime,commonIO,new LiteralStringType()))
					.withInput(createLiteralInput(this.endTime,commonIO,new LiteralStringType()))
					.withInput(createLiteralInput(this.mqttoutput,mqttio,new LiteralStringType(),0));;

			this.processBuilder = this.processBuilder.withInput(createRuntimeGroupInput(RainGauge));

			this.processBuilder = this.processBuilder.withOutput(createGroupOutput(Junctions))
					                                 .withOutput(createGroupOutput(Subcatchments));

			this.processBuilder = this.processBuilder.withOutput(rptOut).withOutput(resultOut);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return this.processBuilder.build();
	}


	private ProcessInputDescription createRuntimeGroupInput(String identifier) throws URISyntaxException {
		OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME),IOType.TITLE);
		OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME),IOType.TITLE);
		OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY),IOType.TITLE);
		//OwsMetadata mqttIO = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);
		return this.descriptionFactory.groupInput().withMinimalOccurence(0).withMaximalOccurence(5).withIdentifier(identifier).withMetadata(runTimeIO).
				withInput(createLiteralInput("Identifier", timeIO,new LiteralStringType(),0,5)).
				withInput(createLiteralInput("Value", quantityIO,new LiteralDoubleType(),0,5)).
				withInput(createLiteralInput("Time", timeIO,new LiteralStringType(),0,5)).
				withInput(createMQTTGroupInput("MQTTInput")).
				build();
	}

	private ProcessInputDescription createMQTTGroupInput(String identifier) throws URISyntaxException {
		OwsMetadata mqttuser = new OwsMetadata(new URI(IOType.METAHREF_MQTT_USER),IOType.TITLE);
		OwsMetadata mqtttopic = new OwsMetadata(new URI(IOType.METAHREF_MQTT_TOPIC),IOType.TITLE);
		OwsMetadata mqtturl = new OwsMetadata(new URI(IOType.METAHREF_MQTT_URL),IOType.TITLE);
		OwsMetadata mqttpsd = new OwsMetadata(new URI(IOType.METAHREF_MQTT_PSD),IOType.TITLE);
		OwsMetadata mqttIO = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);
		return this.descriptionFactory.groupInput().withMinimalOccurence(0).withMaximalOccurence(5).withIdentifier(identifier).withMetadata(mqttIO).
				withInput(createLiteralInput("Topic", mqtttopic,new LiteralStringType(),0,5)).
				withInput(createLiteralInput("URL", mqtturl,new LiteralStringType(),0,5)).
				withInput(createLiteralInput("Username", mqttuser,new LiteralStringType(),0,5)).
				withInput(createLiteralInput("Password", mqttpsd,new LiteralStringType(),0,5)).
				build();
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
	/*
	private ProcessOutputDescription createGroupOutput(String identifier) throws URISyntaxException {
		OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME), IOType.TITLE);
		OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME), IOType.TITLE);
		OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY), IOType.TITLE);

		return this.descriptionFactory.groupOutput().withIdentifier(identifier).withMetadata(runTimeIO)
				.withOutput(createLiteralOutput(this.dateTimeParam, quantityIO, new LiteralStringType()))
				.withOutput(createLiteralOutput(this.flowParam, quantityIO, new LiteralStringType()))
				.withOutput(createLiteralOutput(this.depthParm, timeIO, new LiteralStringType())).build();
	}
	 */

	private ProcessOutputDescription createGroupOutput(String identifier) throws URISyntaxException {
		OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME),IOType.TITLE);
		OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME),IOType.TITLE);
		OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY),IOType.TITLE);
		OwsMetadata mqttIo = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);

		return this.descriptionFactory.groupOutput().withIdentifier(identifier).withMetadata(runTimeIO).withOutput(createLiteralOutput("Value",quantityIO,new LiteralStringType())).withOutput(createLiteralOutput("Time", timeIO,new LiteralStringType())).withOutput(createLiteralOutput(this.mqttoutput, mqttIo,new LiteralStringType())).build();
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

	private ProcessInputDescription createLiteralInput(String identifier, OwsMetadata commonIO, LiteralType<?> dataType,
													   int minioccur, int maxuoccur) throws URISyntaxException {
		return this.descriptionFactory.literalInput().withIdentifier(identifier).withMetadata(commonIO)
				.withDefaultLiteralDataDomain(literalDataDomain).withMinimalOccurence(minioccur).withMaximalOccurence(maxuoccur).withType(dataType)
				.build();
	}

	private ProcessOutputDescription createLiteralOutput(String identifier, OwsMetadata commonIO,
			LiteralType<?> dataType) throws URISyntaxException {
		return this.descriptionFactory.literalOutput().withIdentifier(identifier).withMetadata(commonIO)
				.withDefaultLiteralDataDomain(literalDataDomain).withType(dataType).build();
	}

}
