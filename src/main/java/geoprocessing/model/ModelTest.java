package geoprocessing.model;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.n52.javaps.algorithm.AbstractAlgorithm;
import org.n52.javaps.algorithm.ExecutionException;
import org.n52.javaps.algorithm.ProcessInputs;
import org.n52.javaps.description.TypedComplexOutputDescription;
import org.n52.javaps.description.TypedLiteralOutputDescription;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.description.impl.TypedProcessDescriptionFactory;
import org.n52.javaps.engine.ProcessExecutionContext;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.literal.LiteralData;
import org.n52.javaps.io.literal.xsd.LiteralAnySimpleType;
import org.n52.shetland.ogc.ows.OwsCode;
import org.n52.shetland.ogc.wps.Format;
import org.n52.shetland.ogc.wps.description.LiteralDataDomain;
import org.n52.shetland.ogc.wps.description.ProcessInputDescription;
import org.n52.shetland.ogc.wps.description.impl.LiteralDataDomainImpl;

import cn.edu.whu.model.IEnvModel;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;

public class ModelTest extends AbstractAlgorithm implements IEnvModel {

	 private  TypedProcessDescription description;

	 private  TypedProcessDescriptionFactory descriptionFactory;
	 private String input = "input";
	 private String output = "result";
	 private String output2 ="result2";
	 private String identifier = "ModelTest";
	    
	@Override
	public void execute(ProcessExecutionContext context) throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("excuting");
		
		ProcessInputs inputs = context.getInputs();
		//inputs.get(input);
		
		List<Data<?>> inputDatas = inputs.get(new OwsCode(this.input)); 
		
		Data inputData = null;
		for(OwsCode keyCode : context.getInputs().keySet()) {
			if(keyCode.getValue().equalsIgnoreCase(this.input)) {
				inputData = inputs.get(keyCode).get(0);
			}
		}
		
		
		GeneralFileBinding inputFile = (GeneralFileBinding)inputData;
		
		context.getOutputs().put(new OwsCode(this.output),new LiteralData(inputFile.getPayload().getAbsolutePath()));
		context.getOutputs().put(new OwsCode(this.output2), inputFile);
		
		System.out.println(inputs);
	}

	@Override
	public void performStep(ProcessExecutionContext context) throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("perform time step");
	}

	@Override
	public void finish() throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("finising");
		
		
		
	}

	@Override
	protected TypedProcessDescription createDescription() {
		if(this.descriptionFactory ==null)
			this.descriptionFactory = new TypedProcessDescriptionFactory();
		if(this.description != null )
			return this.description;
		
		LiteralDataDomain literalDataDomain = new LiteralDataDomainImpl.Builder().withDefaultValue("out").withDataType("string").build();
		
		Format defaultFormat = new Format("application/wkt"); 
		TypedLiteralOutputDescription ouputDesc = this.descriptionFactory.literalOutput().withIdentifier(this.output).withTitle("result").withDefaultLiteralDataDomain(literalDataDomain).withType(new LiteralAnySimpleType()).build();
		ProcessInputDescription inputDesc = this.descriptionFactory.complexInput().withIdentifier(this.input).withDefaultFormat(defaultFormat).withSupportedFormat(defaultFormat).withType(GeneralFileBinding.class).build();
		TypedComplexOutputDescription outputDec2 = this.descriptionFactory.complexOutput().withIdentifier(this.output2).withTitle("output 2").withDefaultFormat(defaultFormat).withSupportedFormat(defaultFormat).withType(GeneralFileBinding.class).build();
		
		this.description = this.descriptionFactory.process().withIdentifier(identifier).withInput(inputDesc).withOutput(ouputDesc).withOutput(outputDec2).withVersion("1.0.0").build();
		
		return this.description;
	}

	@Override
	public Calendar getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Calendar getEndTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Calendar getCurrentTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> performStep(Map<String, Map<String, Object>> inputs) {
		return null;
	}
}
