package geoprocessing.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
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
import org.n52.javaps.settings.SettingsConstants;
import org.n52.shetland.ogc.ows.OwsCode;

import cn.edu.whu.model.IEnvModel;
import geoprocessing.io.data.binding.complex.GeneralFileBinding;
import geoprocessing.util.TimeConverter;

@Configurable
public class TOPMODEL2 extends AbstractAlgorithm implements IEnvModel{
	
	 private ProcessExecutionContext context;
	
	 private Calendar startTimeCalendar,endTimeCalendar,currentTimeCalendar;
	 
	 private Path basePath;
	 
	 protected double _simulationStartTime;
	 protected double _simulationEndTime;
	 protected double _currentTime;
	 protected double _timeStep;
	 
	 
	 double R;//subsurface Recharge rate [L/T]
     double c; //recession parameter (m)
     double Tmax; //Average effective transmissivity of the soil when the profile is just saturated
     double interception;//intial interciption of the watershed

     double[] TI;//topographic index
     double[] freq;//topographic index frequency

     double lamda_average;//average lamda
     double PPT_daily;
     double ET_daily;
     double q_overland;
     double q_subsurface;
     double q_infiltration;
     double S_average; //average saturation deficit
     double _watershedArea = 0; //area of the watershed. used to convert runoff into streamflow
     String topo_index_file;
     
     Map<Date, Double> Precip = new HashMap<Date, Double>();
     Map<Date, Double> ET = new HashMap<Date, Double>();
     Map<Calendar, TopModelData> outputValues = new LinkedHashMap<Calendar, TopModelData>();
     
    
     String[] _input_elementset;
     String[] _output_elementset;
     String[] _output_quantity;
     String[] _input_quantity;
     List<Calendar> _DateTimes = new ArrayList<Calendar>();
     List<Double> q_outputs = new ArrayList<Double>();
     List<Double> q_infltration_outputs = new ArrayList<Double>();
     
     private final String  recession_inid = "Recession",
    		 tmax_inid="Tmax",  rate_inid = "Rate",
    		 interception_inid = "Interception", 
    		 waterShed_area_inid= "WatershedArea",
    		 topo_index_inid = "TopoIndex", 
    		 startTime = "StartTime", endTime = "EndTime",timeStep="TimeStep",precipitation="Precipitation",evapotranspiration ="Evapotranspiration", steprunoff="StepRunoff",runoff="Runoff";
    	
     private String dateFormat = "yyyy-MM-dd HH:mm:ss";
	/**
	 * initialize
	 */
	@Override
	public void execute(ProcessExecutionContext context) throws ExecutionException {
		this.context = context;
		ProcessInputs inputs = context.getInputs();
		
		this.c = toDouble(inputs.get(new OwsCode(this.recession_inid)));
		this.Tmax = toDouble(inputs.get(new OwsCode(this.tmax_inid)));
		this.R = toDouble(inputs.get(new OwsCode(this.rate_inid)));
		this.interception = toDouble(inputs.get(new OwsCode(this.interception_inid)));
		this._watershedArea = toDouble(inputs.get(new OwsCode(this.waterShed_area_inid)));
		
		GeneralFileBinding topoIndexBinding = (GeneralFileBinding)inputs.get(new OwsCode(this.topo_index_inid)).get(0); 
		String topoIndexPath = topoIndexBinding.getPayload().getAbsolutePath();
		read_topo_input(topoIndexPath);
		
		String startTimeStr = inputs.get(new OwsCode(this.startTime)).get(0).getPayload().toString();
		Date startDate = TimeConverter.str2Date(startTimeStr, dateFormat);
		_simulationStartTime = TimeConverter.date2MJulianDate(startDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		this.setStartTime(calendar);
		
		String endTimeStr = inputs.get(new OwsCode(this.endTime)).get(0).getPayload().toString();
		Date endDate = TimeConverter.str2Date(endTimeStr, dateFormat);
		_simulationStartTime = TimeConverter.date2MJulianDate(endDate);
		calendar = Calendar.getInstance();
		calendar.setTime(endDate);
		this.setEndTime(calendar);
		this.setCurrentTime(calendar);
		
		_timeStep = toDouble(inputs.get(new OwsCode(this.timeStep)))/(60*60*24);
		
		double[] TI_freq = new double[TI.length];

        for (int i = 0; i <TI.length; i++)
        {
            TI_freq[i] = TI[i] * freq[i];
        }

        //lamda_average = TI_freq.Sum() / freq.Sum();
        lamda_average = this.sum(TI_freq)/ this.sum(freq);

        //catchement average saturation deficit(S_bar)
        double S_bar = -c * ((Math.log(R / Tmax)) + lamda_average);
        S_average = S_bar;
        
        _currentTime = _simulationStartTime;
	}

	
	@Override
	public void performStep(ProcessExecutionContext context) throws ExecutionException {
		// TODO Auto-generated method stub
		ProcessInputs inputs = context.getInputs();
		List<Data<?>> preciInputs = inputs.get(new OwsCode(this.precipitation));
		
		//TypedProcessInputDescription input = description.getInput(this.precipitation);
		
		String time="default";
		String value = "";
		for(Data<?> preciInput:preciInputs) {
			ProcessInputs inputDatas = ((GroupInputData) preciInput).getPayload();
			value = inputDatas.get((new OwsCode("Value"))).get(0).getPayload().toString();
			time = inputDatas.get((new OwsCode("Time"))).get(0).getPayload().toString();
		}
		
		LiteralData valueData = new LiteralData(value);
		LiteralData timeData = new LiteralData(time);
		//ProcessInputs 
		
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.put(new OwsCode("Value"), valueData);
		outputs.put(new OwsCode("Time"),timeData);
		GroupOutputData output = new GroupOutputData(outputs);
		// groupData = new GroupInputData(payload);
		
		context.getOutputs().clear();
		context.getOutputs().put(new OwsCode("StepRunoff"), output);
	}

	@Override
	public void finish() throws ExecutionException {
		//do save operation
		//Path jobPath = FilePathUtil.Instance().getJobPath(this.context.getJobId());
		
		if(this.basePath!=null) {
			Path tmpPath = this.basePath.resolve(this.context.getJobId().getValue());
		}
		File outFile = new File("E:/workspace/test.txt");
		GeneralFileBinding fileBinding = new GeneralFileBinding(outFile);
		
		this.context.getOutputs().clear();
		this.context.getOutputs().put(new OwsCode("Runoff"), fileBinding);
	}

	@Override
	public Map<String, Object> performStep(Map<String, Map<String, Object>> inputs) {
		return null;
	}


	@Override
	protected TypedProcessDescription createDescription() {
		return new TopmodelDescriptionGenerator().createDescription();
	}

	
	private double toDouble(List<Data<?>> datas){
		
		if(datas.size() == 0)
			return 0;
		
		Data data = datas.get(0);
		if(!(data instanceof LiteralData))
			return 0;
		
		LiteralData literalData = (LiteralData)data;
		return Double.parseDouble(literalData.getPayload().toString());
	}
	
	private double sum(double[] values) {
		double total = 0;
		for(double val:values) {
			total += val;
		}
		return total;
	}
	
	private void advanceStep() {
		this._currentTime += _timeStep;
		this.currentTimeCalendar = TimeConverter.modifiedJulian2Gregorian(this._currentTime);
	}
	
	/**
	 * Save the result of topmodel simulation
	 * @author Mingda Zhang
	 *
	 */
	public class TopModelData {
		private double precip, pet, runoff, streamFlow;
		private Calendar time;

		
		public double getPrecip() {
			return precip;
		}

		public void setPrecip(double precip) {
			this.precip = precip;
		}

		public double getPet() {
			return pet;
		}

		public void setPet(double pet) {
			this.pet = pet;
		}

		public double getRunoff() {
			return runoff;
		}

		public void setRunoff(double runoff) {
			this.runoff = runoff;
		}

		public double getStreamFlow() {
			return streamFlow;
		}

		public void setStreamFlow(double streamFlow) {
			this.streamFlow = streamFlow;
		}

		public Calendar getTime() {
			return time;
		}

		public void setTime(Calendar time) {
			this.time = time;
		}

	}
	
	/**
	 * Reads an input file containing topographic index to produce topographic index and topographic frequency arrays
	 * ti:output topographic index array
	 * freq:output topographic frequency array
	 */
    public boolean read_topo_input(String topographicIndex)
    {
        //---- begin reading the values stored in the topo file
      //  StreamReader sr = new StreamReader(topographicIndex);
        InputStream is=null;
		try {
			is = new FileInputStream(new File(topographicIndex));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		try {
			BufferedReader sr = new BufferedReader(new InputStreamReader(is));
	        //-- read header info
	        String line = null;
	        for (int i=0; i<=4; i++){
	        	line = sr.readLine();
	        }

	        //-- save the cellsize
	        double cellsize = Double.parseDouble(line.split(" ")[line.split(" ").length - 1]);
	        line = sr.readLine();

	        //-- save the nodata value
	        String nodata = line.split(" ")[line.split(" ").length-1];
	        line = sr.readLine();

	        //-- store all values != nodata in a list
	        List<Double> topoList = new ArrayList<Double>();
	        int lineNum = 0;
	        while (line!=null && !line.isEmpty())
	        {
	            lineNum += 1;
	            String[] vals = line.trim().split(" ");
	            for (int i = 0; i < vals.length; i++){
	            	 if (!vals[i].equals(nodata)){
	            		 //add by zmd,是否需要指定小数点位数，因为下边会有指数对比，判断是否相等
	                 	topoList.add(Double.parseDouble(vals[i])); 
	                 	//modified by zmd, why
	                 	//_watershedArea += cellsize;
	                 }
	            }
	            line = sr.readLine();
	        }

	        //---- calculate frequency of each topographic index
	        //-- consolidate topo list into unique values 
	        Map<Double, Double> d = new HashMap<Double, Double>();
	        for(double t:topoList){
	        	if (d.containsKey(t)) {
					d.put(t, d.get(t)+1.0);
				}else{
					d.put(t, 1.0);
				}
	        }
//	        Dictionary<double, double> d = new Dictionary<double, double>();
	        /*foreach (double t in topoList)
	            if (d.ContainsKey(t))
	                d[t] += 1.0;
	            else
	                d.Add(t, 1.0);*/

	        //-- calculate topo frequency, then return both topographic index and topo frequency arrays
	        double total = (double)topoList.size();
	        this.TI = new double[d.size()];
	        this.freq = new double[d.size()];
	        int index = 0;
	        for(double key:d.keySet()){
	        	this.TI[index] =Math.round((Double)key*10000)*0.0001d;
	        	this.freq[index] =Math.round( d.get(key)/total*Math.pow(10, 10))*Math.pow(0.1, 10);
	        	index++;
	        }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		
        return true;
    }

    public void setCurrentTime(Calendar time) {
    	this.currentTimeCalendar = time;
    }
    
    public void setStartTime(Calendar time) {
    	this.startTimeCalendar = time;
    }
    
    public void setEndTime(Calendar time) {
    	this.endTimeCalendar = time;
    }
	@Override
	public Calendar getStartTime() {
		return this.startTimeCalendar;
	}


	@Override
	public Calendar getEndTime() {
		return endTimeCalendar;
	}


	@Override
	public Calendar getCurrentTime() {
		return this.currentTimeCalendar;
	}

	 @Setting(SettingsConstants.MISC_BASE_DIRECTORY)
	 public void setBasePath(File baseDirectory) {
	       this.basePath = baseDirectory.toPath();
	  }
}
