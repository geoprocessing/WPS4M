package geoprocessing.model.swmm5;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.javaps.algorithm.AbstractAlgorithm;
import org.n52.javaps.algorithm.ExecutionException;
import org.n52.javaps.algorithm.ProcessInputs;
import org.n52.javaps.algorithm.ProcessOutputs;
import org.n52.javaps.description.TypedComplexOutputDescription;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.description.impl.TypedProcessDescriptionFactory;
import org.n52.javaps.engine.ProcessExecutionContext;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.GroupInputData;
import org.n52.javaps.io.GroupOutputData;
import org.n52.javaps.io.literal.LiteralData;
import org.n52.shetland.ogc.ows.OwsCode;
import org.n52.shetland.ogc.wps.Format;
import org.n52.shetland.ogc.wps.description.ProcessInputDescription;

import com.geoprocessing.model.swmm.JSWMM;
import com.geoprocessing.model.swmm.SWMMExecption;
import com.geoprocessing.model.swmm.StepSimulation;
import com.geoprocessing.model.swmm.object.Link;
import com.geoprocessing.model.swmm.object.ObjectType;

import cn.edu.whu.model.IEnvModel;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;
import geoprocessing.util.TimeConverter;

public class SWMM extends AbstractAlgorithm implements IEnvModel {

	 private  TypedProcessDescription description;

	 private ProcessExecutionContext context;
	 private Calendar startTimeCalendar,endTimeCalendar,currentTimeCalendar;
	 
	 private JSWMM swmm;
	 private StepSimulation stepSimulation = null;
	 
	 private SWMMDescriptionGenerator descriptionGeneator;
	 
	 private String dateFormat = "yyyy-MM-dd HH:mm:ss";
	 
	@Override
	public void execute(ProcessExecutionContext context) throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("excuting");
		this.context = context;
		
		ProcessInputs inputs = context.getInputs();
		//inputs.get(input);
		
		List<Data<?>> inputDatas = inputs.get(new OwsCode(this.descriptionGeneator.input)); 
		
		Data inputData = null;
		long timestep = 300;
		for(OwsCode keyCode : context.getInputs().keySet()) {
			if(keyCode.getValue().equalsIgnoreCase(this.descriptionGeneator.input)) {
				inputData = inputs.get(keyCode).get(0);
			}
			if(keyCode.getValue().equalsIgnoreCase(this.descriptionGeneator.timeStep)) {
				timestep = Math.round((Double)inputs.get(keyCode).get(0).getPayload());
			}
		}
		
		GeneralFileBinding inputFile = (GeneralFileBinding)inputData;
		String inputPath = inputFile.getPayload().getAbsolutePath();
		try {
			swmm = new JSWMM(inputPath);
		} catch (SWMMExecption e) {
			// TODO Auto-generated catch block
			throw new ExecutionException(e.getMessage());
		}
		
		stepSimulation = new StepSimulation(swmm, timestep);
		stepSimulation.initialize();
		
		//stepSimulation.run();
		updateDescription();
	}

	@Override
	public void performStep(ProcessExecutionContext context) throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("perform time step");

		// TODO Auto-generated method stub
		ProcessInputs inputs = context.getInputs();
		
		List<String> raingages = this.swmm.getRainGages();
		Calendar dateTimeCalendar = null;
		Map<String, Double> rainfall = new HashMap<String, Double>();
		
		for(String raingage:raingages) {
			List<Data<?>> preciInputs = inputs.get(new OwsCode(raingage));
			for(Data<?> preciInput:preciInputs) {
				ProcessInputs inputDatas = ((GroupInputData) preciInput).getPayload();
				String value = inputDatas.get((new OwsCode(this.descriptionGeneator.rainfallParam))).get(0).getPayload().toString();
				String time = inputDatas.get((new OwsCode(this.descriptionGeneator.dateTimeParam))).get(0).getPayload().toString();
				rainfall.put(raingage, Double.parseDouble(value));
				dateTimeCalendar = TimeConverter.str2Calendar(time, dateFormat);
			}
		}
		
		LocalDateTime localTime = TimeConverter.toLocalDateTime(dateTimeCalendar);
		this.stepSimulation.step(rainfall,localTime);
		 
		LocalDateTime currentTime = this.swmm.getCurrentDateTime();
		String curTimeString = TimeConverter.local2String(currentTime);
		ProcessOutputs outputs = context.getOutputs();
		outputs.clear();
        
        List<String> conduits = this.swmm.getLinks();
        for(String conduit:conduits) {
        	if(context.getOutputDefinition(conduit).isEmpty())
				continue;
        	
        	int index = this.swmm.findObjectIndex(ObjectType.LINK.getValue(), conduit);
        	Link link = new Link(this.swmm, index);
        	String flow = String.valueOf(link.getFlow());
        	String  depth = String.valueOf(link.getDepth());
        	
        	LiteralData flowData = new LiteralData(flow);
    		LiteralData depthData = new LiteralData(depth);
    		LiteralData timeData = new LiteralData(curTimeString);
        	
        	ProcessOutputs groupOutput = new ProcessOutputs();
        	groupOutput.put(new OwsCode(this.descriptionGeneator.flowParam), flowData);
        	groupOutput.put(new OwsCode(this.descriptionGeneator.depthParm),depthData);
        	groupOutput.put(new OwsCode(this.descriptionGeneator.dateTimeParam),timeData);
        	
        	GroupOutputData output1 = new GroupOutputData(groupOutput);
        	outputs.put(new OwsCode(conduit), output1);
        }
	}

	@Override
	public void finish() throws ExecutionException {
		// TODO Auto-generated method stub
		System.out.println("finising");
		stepSimulation.finish();
		
		String rptOut = swmm.getReport();
		String resultOut = swmm.getOutput();
		GeneralFileBinding rptFile = new GeneralFileBinding(new File(rptOut));
		GeneralFileBinding resultFile = new GeneralFileBinding(new File(resultOut));
		
		context.getOutputs().put(new OwsCode(this.descriptionGeneator.rptOut), rptFile);
		context.getOutputs().put(new OwsCode(this.descriptionGeneator.resultOut), resultFile);
	}

	@Override
	protected TypedProcessDescription createDescription() {
		if(this.descriptionGeneator == null)
			this.descriptionGeneator = new SWMMDescriptionGenerator();
		
		this.description = this.descriptionGeneator.createInitDescription();
		
		return this.description;
	}

	private void updateDescription() {
		this.description = this.descriptionGeneator.updateDescription(this.swmm);
		setDescription(this.description);
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

}
