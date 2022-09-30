package geoprocessing.model.swmm5;

import cn.edu.whu.model.IEnvModel;
import com.geoprocessing.model.swmm.JSWMM;
import com.geoprocessing.model.swmm.SWMMExecption;
import com.geoprocessing.model.swmm.StepSimulation;
import com.geoprocessing.model.swmm.object.Link;
import com.geoprocessing.model.swmm.object.Node;
import com.geoprocessing.model.swmm.object.ObjectType;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;
import geoprocessing.model.AbstractModelWrapper;
import geoprocessing.util.TimeConverter;
import org.n52.javaps.algorithm.AbstractAlgorithm;
import org.n52.javaps.algorithm.ExecutionException;
import org.n52.javaps.algorithm.ProcessInputs;
import org.n52.javaps.algorithm.ProcessOutputs;
import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.engine.ProcessExecutionContext;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.GroupInputData;
import org.n52.javaps.io.GroupOutputData;
import org.n52.javaps.io.literal.LiteralData;
import org.n52.shetland.ogc.ows.OwsCode;

import java.io.File;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SWMMMqtt extends AbstractModelWrapper {

	 private  TypedProcessDescription description;

	 private ProcessExecutionContext context;
	 private Calendar startTimeCalendar,endTimeCalendar,currentTimeCalendar;
	 
	 private JSWMM swmm;
	 private StepSimulation stepSimulation = null;
	 
	 private SWMMMqttDescriptionGenerator descriptionGeneator;
	 
	 private String dateFormat = "yyyy-MM-dd HH:mm:ss";

	 /*
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
	}
	*/


	@Override
	protected boolean execute() {
		Map<String, Object> inputMap = this.getInitInput();
		File inputFile = (File) inputMap.get(this.descriptionGeneator.input);
		Double timestep = (Double) inputMap.get(this.descriptionGeneator.timeStep);

		try {
			swmm = new JSWMM(inputFile.getAbsolutePath());
		} catch (SWMMExecption e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stepSimulation = new StepSimulation(swmm, timestep.intValue());
		stepSimulation.initialize();
		return true;
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
	protected boolean performStep() {
		List<String> raingages = this.swmm.getRainGages();
		Calendar dateTimeCalendar = null;
		Map<String, Double> rainfall = new HashMap<String, Double>();

		for(String raingage:raingages) {
			Map<String, Object> raingageVaule =(Map<String, Object> ) this.getStepInput().get(raingage);
			Double value = (Double)raingageVaule.get("Value");
			dateTimeCalendar =(Calendar) raingageVaule.get("Time");
			rainfall.put(raingage, value);
		}

		LocalDateTime localTime = TimeConverter.toLocalDateTime(dateTimeCalendar);
		this.stepSimulation.step(rainfall,localTime);

		LocalDateTime currentTime = this.swmm.getCurrentDateTime();
		String curTimeString = TimeConverter.local2String(currentTime);

		this.getStepOutput().clear();

		List<String> conduits = this.swmm.getLinks();
		for(String conduit:conduits) {

			int index = this.swmm.findObjectIndex(ObjectType.LINK.getValue(), conduit);
			Link link = new Link(this.swmm, index);
			String flow = String.valueOf(link.getFlow());
			String  depth = String.valueOf(link.getDepth());

			Map<String,Object> flowValueMap = new HashMap<>();
			flowValueMap.put("Value",flow);
			flowValueMap.put("Time",dateTimeCalendar);
			this.getStepOutput().put(conduit+"_flow",flowValueMap);


			Map<String,Object> depthValueMap = new HashMap<>();
			depthValueMap.put("Value",depth);
			depthValueMap.put("Time",dateTimeCalendar);
			this.getStepOutput().put(conduit+"_depth",depthValueMap);
		}

		List<String> nodes = this.swmm.getNodes();
		for(String nodeStr:nodes) {

			int index = this.swmm.findObjectIndex(ObjectType.NODE.getValue(), nodeStr);
			Node node = new Node(this.swmm, index);
			String flow = String.valueOf(node.getFlow());
			String  depth = String.valueOf(node.getDepth());

			Map<String,Object> flowValueMap = new HashMap<>();
			flowValueMap.put("Value",flow);
			flowValueMap.put("Time",dateTimeCalendar);
			this.getStepOutput().put(nodeStr+"_flow",flowValueMap);


			Map<String,Object> depthValueMap = new HashMap<>();
			depthValueMap.put("Value",depth);
			depthValueMap.put("Time",dateTimeCalendar);
			this.getStepOutput().put(nodeStr+"_depth",depthValueMap);
		}
		return true;
	}


	@Override
	protected boolean finishModel() {
		System.out.println("finising");
		stepSimulation.finish();

		String rptOut = swmm.getReport();
		String resultOut = swmm.getOutput();
		GeneralFileBinding rptFile = new GeneralFileBinding(new File(rptOut));
		GeneralFileBinding resultFile = new GeneralFileBinding(new File(resultOut));

		context.getOutputs().put(new OwsCode(this.descriptionGeneator.rptOut), rptFile);
		context.getOutputs().put(new OwsCode(this.descriptionGeneator.resultOut), resultFile);
		return true;
	}


	@Override
	protected TypedProcessDescription createDescription() {
		if(this.descriptionGeneator == null)
			this.descriptionGeneator = new SWMMMqttDescriptionGenerator();

		this.description = this.descriptionGeneator.createInitDescription();
		
		return this.description;
	}

	
	@Override
	public Calendar getStartTime() {
		// TODO Auto-generated method stub
		LocalDateTime localDateTime = this.swmm.getStartTime();
		return TimeConverter.localToCalendar(localDateTime);
	}

	@Override
	public Calendar getEndTime() {
		LocalDateTime localDateTime = this.swmm.getEndTime();
		return TimeConverter.localToCalendar(localDateTime);
	}

	@Override
	public Calendar getCurrentTime() {
		LocalDateTime localDateTime = this.swmm.getCurrentDateTime();
		return TimeConverter.localToCalendar(localDateTime);
	}

}
