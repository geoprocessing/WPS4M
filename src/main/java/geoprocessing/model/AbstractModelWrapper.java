package geoprocessing.model;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
import org.n52.javaps.algorithm.AbstractAlgorithm;
import org.n52.javaps.algorithm.ExecutionException;
import org.n52.javaps.algorithm.ProcessInputs;
import org.n52.javaps.algorithm.ProcessOutputs;
import org.n52.javaps.engine.ProcessExecutionContext;
import org.n52.javaps.io.Data;
import org.n52.javaps.io.GroupInputData;
import org.n52.javaps.io.GroupOutputData;
import org.n52.javaps.io.complex.ComplexData;
import org.n52.javaps.io.literal.LiteralData;
import org.n52.javaps.settings.SettingsConstants;
import org.n52.shetland.ogc.ows.OwsCode;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import com.fasterxml.jackson.annotation.JacksonInject.Value;

import cn.edu.whu.model.IEnvModel;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;

@Configurable
public abstract class AbstractModelWrapper extends AbstractAlgorithm implements IEnvModel {
	private Path basePath;
	private Calendar startTime, endTime, currentTime;
	private StringBuffer errInfo = new StringBuffer();
	private ProcessExecutionContext context;
	//initial arguments
	private Map<String, Object> InitInputMap = new HashMap<String, Object>();
	private Map<String, Object> StepInputMap = new HashMap<String, Object>();
	private Map<String, Object> FinalOutputMap = new HashMap<String, Object>();
	private Map<String, Object> StepOutputMap = new HashMap<String, Object>();
	
	
	@Override
	public void execute(ProcessExecutionContext context) throws ExecutionException {
		this.context = context;
		retrieveInputs(context,InitInputMap);
		if(!this.execute()) {
			throw new ExecutionException(this.errInfo.toString());
		}
		//setOutputs(context,FinalOutputMap);
	}

	protected abstract boolean execute();
	
	private void retrieveInputs(ProcessExecutionContext context, Map<String, Object> inputMap) {
		inputMap.clear();
		ProcessInputs inputs = context.getInputs();
		for(OwsCode identifier:inputs.keySet()) {
			List<Data<?>> value = inputs.get(identifier);
			inputMap.put(identifier.getValue(), parseInput(value));
		}
	}
	
	public Map<String, Object> getInitInput(){
		return this.InitInputMap;
	}
	
	public Map<String, Object> getStepInput(){
		return this.StepInputMap;
	}
	
	//ignore the situation when the size of datas is larger than one
	private Object parseInput(List<Data<?>> datas){
		
		if(datas.size() == 0)
			return null;
		
		Data<?> data = datas.get(0);
		
		if(data instanceof GroupInputData) {
			ProcessInputs inputDatas = ((GroupInputData) data).getPayload();
			Map<String, Object> inputList = new HashMap<String,Object>();
			
			for(OwsCode identifier:inputDatas.keySet()) {
				List<Data<?>> value = inputDatas.get(identifier);
				//Map<String, Object> valueMap = new HashMap<String, Object>();
				inputList.put(identifier.getValue(), parseInput(value));
			}
			return inputList;
		}
		
		return datas.get(0).getPayload();
	}
	
	@Override
	public void performStep(ProcessExecutionContext context) throws ExecutionException {
		retrieveInputs(context,StepInputMap);
		if(!this.performStep()) {
			throw new ExecutionException(this.errInfo.toString());
		}
		setOutputs(context,StepOutputMap);
	}
	
	protected abstract boolean performStep();
	
	@Override
	public void finish() throws ExecutionException {
		if(!this.finishModel()) {
			throw new ExecutionException(this.errInfo.toString());
		}
		setOutputs(this.context, this.FinalOutputMap);
	}
	protected abstract boolean finishModel();
	
	
	private void setOutputs(ProcessExecutionContext context,Map<String, Object> outputMap) {
		context.getOutputs().clear();
		ProcessOutputs outputs = context.getOutputs();
		for(String key:outputMap.keySet()) {
			if(context.getOutputDefinition(key).isEmpty())
				continue;
			
			Object value = outputMap.get(key);
			Data data = getData(value);
			outputs.put(new OwsCode(key), data);
		}
	}
	
	private Data<?> getData(Object obj){

		if(obj instanceof Map) {
			Map<String, Object> outputMaps = (Map<String, Object>)obj;
			ProcessOutputs outputs = new ProcessOutputs();
			for(String key:outputMaps.keySet()) {
				Data data2 = getData(outputMaps.get(key));
				outputs.put(new OwsCode(key), data2);
			}
			return new GroupOutputData(outputs);
		}
		
		if(obj instanceof ComplexData)
			return (ComplexData)obj;
		
		return new LiteralData(obj); 
	}
	
	public Map<String, Object> getStepOutput(){
		return this.StepOutputMap;
	}
	
	public Map<String, Object> getFinalOutput(){
		return this.FinalOutputMap;
	}
	
	protected void setCurrentTime(Calendar time) {
		this.currentTime = time;
	}

	protected void setStartTime(Calendar time) {
		this.startTime = time;
	}

	protected void setEndTime(Calendar time) {
		this.endTime = time;
	}

	@Override
	public Calendar getStartTime() {
		return this.startTime;
	}

	@Override
	public Calendar getEndTime() {
		return endTime;
	}

	@Override
	public Calendar getCurrentTime() {
		return this.currentTime;
	}

	/**
	 * Config the base path using configuration file
	 * 
	 * @param baseDirectory
	 */
	@Setting(SettingsConstants.MISC_BASE_DIRECTORY)
	public void setBasePath(File baseDirectory) {
		this.basePath = baseDirectory.toPath();
	}

	protected Path getBasePath() {
		return this.basePath;
	}

	protected void appendErr(String errInfo) {
		this.errInfo.append(errInfo);
	}
}
