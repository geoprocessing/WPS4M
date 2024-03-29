package geoprocessing.model;

import cn.edu.whu.model.IOType;
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

public class TopmodelMqttDescriptionGenerator {
	 public final String identifier = "TOPMODELMqtt",
			 rate_inid = "Rate", //"subsurface recharge rate [L/T]", 
    		 recession_inid = "Recession",//recession_abstr="recession parameter",
    		 tmax_inid="Tmax", //tmax_abstr="Average effective transmissivity of the soil when the profile is just saturated",
    		 interception_inid = "Interception", //interception_abstr="intial interciption of the watershed",
    		 waterShed_area_inid= "WatershedArea",//waterShed_area_abstr="the area of the watershed",
    		 topo_index_inid = "TopoIndex", //topo_index_abstr = "Average effective transmissivity of the soil when the profile is just saturated",
    		 startTime = "StartTime", endTime = "EndTime",timeStep="TimeStep",
    		 precipitation="Precipitation",evapotranspiration ="Evapotranspiration", steprunoff="StepRunoff",runoff="Runoff",mqttoutput="MqttOutput";
    	
	 private TypedProcessDescriptionFactory descriptionFactory;
	 private TypedProcessDescriptionImpl.Builder processBuilder;
	 LiteralDataDomain literalDataDomain = new LiteralDataDomainImpl.Builder().build();
	 
	 public TypedProcessDescription createDescription() {
			if(this.descriptionFactory ==null)
				this.descriptionFactory = new TypedProcessDescriptionFactory();

			this.processBuilder = this.descriptionFactory.process();
			
			
			Format defaultFormat = new Format("text/plain"); 
			ProcessInputDescription topIndexinput = this.descriptionFactory.complexInput().withIdentifier(this.topo_index_inid).withDefaultFormat(defaultFormat).withSupportedFormat(defaultFormat).withType(GeneralFileBinding.class).build();
			TypedComplexOutputDescription finalOut = this.descriptionFactory.complexOutput().withIdentifier(this.runoff).withTitle("watershed runoff").withDefaultFormat(defaultFormat).withSupportedFormat(defaultFormat).withType(GeneralFileBinding.class).build();
			
			this.processBuilder = this.processBuilder.withIdentifier(identifier).withVersion("1.0.0").withInput(topIndexinput).withOutput(finalOut);
			try {
				OwsMetadata commonIO = new OwsMetadata(new URI(IOType.METAHREF_COMMON),IOType.TITLE);
				OwsMetadata mqttio = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);
				this.processBuilder = this.processBuilder.withInput(createLiteralInput(this.recession_inid,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.rate_inid,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.tmax_inid,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.interception_inid,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.waterShed_area_inid,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.timeStep,commonIO,new LiteralDoubleType()))
														 .withInput(createLiteralInput(this.startTime,commonIO,new LiteralStringType()))
														 .withInput(createLiteralInput(this.endTime,commonIO,new LiteralStringType()))
						                                 .withInput(createLiteralInput(this.mqttoutput,mqttio,new LiteralStringType(),0));
				
				this.processBuilder = this.processBuilder.withInput(createRuntimeGroupInput(precipitation)).withInput(createRuntimeGroupInput(evapotranspiration));
				this.processBuilder = this.processBuilder.withOutput(createGroupOutput(steprunoff));
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
			return this.descriptionFactory.groupInput().withMinimalOccurence(0).withIdentifier(identifier).withMetadata(runTimeIO).
					withInput(createLiteralInput("Value", quantityIO,new LiteralDoubleType(),0)).
					withInput(createLiteralInput("Time", timeIO,new LiteralStringType(),0)).
					withInput(createMQTTGroupInput("MQTTInput")).
					build();
		}

		private ProcessInputDescription createMQTTGroupInput(String identifier) throws URISyntaxException {
			OwsMetadata mqttuser = new OwsMetadata(new URI(IOType.METAHREF_MQTT_USER),IOType.TITLE);
			OwsMetadata mqtttopic = new OwsMetadata(new URI(IOType.METAHREF_MQTT_TOPIC),IOType.TITLE);
			OwsMetadata mqtturl = new OwsMetadata(new URI(IOType.METAHREF_MQTT_URL),IOType.TITLE);
			OwsMetadata mqttpsd = new OwsMetadata(new URI(IOType.METAHREF_MQTT_PSD),IOType.TITLE);
			OwsMetadata mqttIO = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);
			return this.descriptionFactory.groupInput().withMinimalOccurence(0).withIdentifier(identifier).withMetadata(mqttIO).
					withInput(createLiteralInput("Topic", mqtttopic,new LiteralStringType(),0)).
					withInput(createLiteralInput("URL", mqtturl,new LiteralStringType(),0)).
					withInput(createLiteralInput("Username", mqttuser,new LiteralStringType(),0)).
					withInput(createLiteralInput("Password", mqttpsd,new LiteralStringType(),0)).
					build();
		}
		
		private ProcessOutputDescription createGroupOutput(String identifier) throws URISyntaxException {
			OwsMetadata runTimeIO = new OwsMetadata(new URI(IOType.METAHREF_RUNTIME),IOType.TITLE); 
			OwsMetadata timeIO = new OwsMetadata(new URI(IOType.METAHREF_TIME),IOType.TITLE); 
			OwsMetadata quantityIO = new OwsMetadata(new URI(IOType.METAHREF_QUANTITY),IOType.TITLE);
			OwsMetadata mqttIo = new OwsMetadata(new URI(IOType.METAHREF_MQTT),IOType.TITLE);

			return this.descriptionFactory.groupOutput().withIdentifier(identifier).withMetadata(runTimeIO).withOutput(createLiteralOutput("Value",quantityIO,new LiteralStringType())).withOutput(createLiteralOutput("Time", timeIO,new LiteralStringType())).withOutput(createLiteralOutput(this.mqttoutput, mqttIo,new LiteralStringType())).build();
		}
		
		private ProcessInputDescription createLiteralInput(String identifier,OwsMetadata commonIO,LiteralType<?> dataType) throws URISyntaxException {
			return this.descriptionFactory.literalInput().withIdentifier(identifier).withMetadata(commonIO).withDefaultLiteralDataDomain(literalDataDomain).withType(dataType).build();
		}
		
		private ProcessInputDescription createLiteralInput(String identifier,OwsMetadata commonIO,LiteralType<?> dataType,int minioccur) throws URISyntaxException {
			return this.descriptionFactory.literalInput().withIdentifier(identifier).withMetadata(commonIO).withDefaultLiteralDataDomain(literalDataDomain).withMinimalOccurence(minioccur).withType(dataType).build();
		}
		
		private ProcessOutputDescription createLiteralOutput(String identifier,OwsMetadata commonIO,LiteralType<?> dataType) throws URISyntaxException {
			return this.descriptionFactory.literalOutput().withIdentifier(identifier).withMetadata(commonIO).withDefaultLiteralDataDomain(literalDataDomain).withType(dataType).build();
		}
		
}
