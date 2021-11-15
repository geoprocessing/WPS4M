package geoprocessing.algorithm.selfdescription;

import java.util.List;

import org.n52.javaps.algorithm.ExecutionException;
import org.n52.javaps.algorithm.ProcessInputs;
import org.n52.javaps.engine.ProcessExecutionContext;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.literal.LiteralData;
import org.n52.shetland.ogc.ows.OwsCode;
import org.n52.shetland.ogc.wps.JobId;

import geoprocessing.io.data.binding.complex.GeneralFileBinding;

public class SelfTestProcess2 extends SelfDescriptionAlgorithm {

	public SelfTestProcess2() {
		this.identifier = "SelfDescriptionTest";
	}
    private String input = "input";
    private String output = "result";
    private String output2 ="result2";
	
	@Override
	public void execute(ProcessExecutionContext context) throws ExecutionException {
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
		JobId jobId = context.getJobId();
		 
	}


}
