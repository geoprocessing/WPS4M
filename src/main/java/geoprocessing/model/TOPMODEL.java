package geoprocessing.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

import org.n52.javaps.description.TypedProcessDescription;
import org.n52.javaps.engine.ProcessExecutionContext;

import geoprocessing.io.data.binding.complex.GeneralFileBinding;
import geoprocessing.util.TimeConverter;

public class TOPMODEL extends AbstractModelWrapper{
	
	 private ProcessExecutionContext context;
	 //private Calendar startTimeCalendar,endTimeCalendar,currentTimeCalendar;
	 
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
	
	@Override
	protected boolean execute() {
		// TODO Auto-generated method stub
		Map<String, Object> inputMap = this.getInitInput();
		
		this.c = Double.parseDouble(inputMap.get(this.recession_inid).toString());
		this.Tmax = Double.parseDouble(inputMap.get(this.tmax_inid).toString());
		this.R = Double.parseDouble(inputMap.get(this.rate_inid).toString());
		this.interception = Double.parseDouble(inputMap.get(this.interception_inid).toString());
		this._watershedArea = Double.parseDouble(inputMap.get(this.waterShed_area_inid).toString());
		
		File topoIndexBinding = (File)inputMap.get(this.topo_index_inid); 
		//String topoIndexPath = topoIndexBinding.getPayload().getAbsolutePath();
		read_topo_input(topoIndexBinding.getAbsolutePath());
		
		String startTimeStr = inputMap.get(this.startTime).toString();
		Date startDate = TimeConverter.str2Date(startTimeStr, dateFormat);
		_simulationStartTime = TimeConverter.date2MJulianDate(startDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		this.setStartTime(calendar);
		this.setCurrentTime(calendar);
		
		String endTimeStr = inputMap.get(this.endTime).toString();
		Date endDate = TimeConverter.str2Date(endTimeStr, dateFormat);
		_simulationStartTime = TimeConverter.date2MJulianDate(endDate);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(endDate);
		this.setEndTime(calendar2);

		
		_timeStep = Double.parseDouble(inputMap.get(this.timeStep).toString())/(60*60*24);
		
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
		return true;
	}
     
	@Override
	protected boolean performStep() {
		Map<String, Object> inputMap = this.getStepInput();
		
		Calendar currentTime = this.getCurrentTime();
		
		Object preciValueMap = inputMap.get(this.precipitation);
		Map<String, Object> groupData = (Map<String, Object>)preciValueMap;
		String preciValue = groupData.get("Value").toString();
		String preciTime = groupData.get("Time").toString();
	
		Calendar preciCalendar = TimeConverter.str2Calendar(preciTime, dateFormat);
		if(!currentTime.equals(preciCalendar)) {
			this.appendErr("The time of "+this.precipitation+" is not equal to the current time");
			return false;
		}
		
		Object etValueMap = inputMap.get(this.evapotranspiration);
		Map<String, Object> etgroupData = (Map<String, Object>)etValueMap;
		String etValue = etgroupData.get("Value").toString();
		String etTime = etgroupData.get("Time").toString();
		
		Calendar etCalendar = TimeConverter.str2Calendar(etTime, dateFormat);
		if(!currentTime.equals(etCalendar)) {
			this.appendErr("The time of "+this.evapotranspiration+" is not equal to the current time");
			return false;
		}
		
        ET_daily = Double.parseDouble(etValue);
        PPT_daily = Double.parseDouble(preciValue);

        //declaring the flow matrices here since they are related with the size of input matrices
        double[] S_d = new double[TI.length];
        double[] over_flow = new double[TI.length]; //Infiltration excess
        double[] reduced_ET = new double[TI.length];//Reduced ET due to dryness

        //calculate the saturation deficit for each TIpoint 
        double[] S = new double[TI.length];
        for (int j = 0; j <TI.length; j++)
        {
            S[j] = S_average + c * (lamda_average - TI[j]);
        }

        //remove the interception effect from PPT matrix, and update the saturation deficit matrix, calculating q_infiltration
        PPT_daily = Math.max(0, (PPT_daily - interception)); // 
        for (int m = 0; m <TI.length; m++)
        {
            S[m] = S[m] - PPT_daily + ET_daily;
        }
        q_infiltration = PPT_daily - ET_daily;
        double[] MM = new double[TI.length];
        if ((PPT_daily - ET_daily) > 0)
        {
            //create a list for S values<0 
            for (int m = 0; m < TI.length; m++)
            {
                if (S[m] < 0) { over_flow[m] = -S[m]; S[m] = 0; }
                else { over_flow[m] = 0; }
                MM[m] = freq[m] * over_flow[m];
            }
        }
        else
        {
            double[] NN = new double[TI.length];
            for (int m = 0; m <TI.length; m++)
            {
                if (S[m] > 5000) { reduced_ET[m] = -5000 + S[m]; S[m] = 5000; } //KK.Add(S[m]);
                else { reduced_ET[m] = 0; }
                NN[m] = freq[m] * reduced_ET[m];
            }

            q_infiltration = q_infiltration + (this.sum(NN) /this.sum(freq));
        }
        q_subsurface = Tmax * (Math.exp(-lamda_average)) * (Math.exp(-S_average / c));
        q_overland = this.sum(MM) / this.sum(freq);

        //calculate the new average deficit using cachement mass balance
        S_average = S_average + q_subsurface + q_overland - q_infiltration;

        //calculating runoff q
        double q = q_overland + q_subsurface;

        //Storing values of DateTimes and surface runoff values
        Calendar curr_time = this.getCurrentTime();
        _DateTimes.add(curr_time);
        q_outputs.add(q);
        q_infltration_outputs.add(q_infiltration);

        //save runoff
        double[] runoff = new double[1];
        runoff[0] = q;// *_watershedArea;

        //-- calculate streamflow using watershed area
        double[] streamflow = new double[1];
        streamflow[0] = q * (_watershedArea) / 86400;

        TopModelData modelData = new TopModelData();
        modelData.setTime(curr_time);
        modelData.setPrecip(PPT_daily);
        modelData.setPet(ET_daily);
        modelData.setRunoff(q);
        modelData.setStreamFlow(streamflow[0]);
        outputValues.put(curr_time, modelData);
        
        Map<String, Object> groupMap = new HashMap<String, Object>();
        groupMap.put("Time", etTime);
        groupMap.put("Value",q+" ");
        
        this.getStepOutput().clear();
        this.getStepOutput().put(this.steprunoff, groupMap);
        
        advanceStep();
		return true;
	}

	@Override
	protected boolean finishModel() {
		try {
			String path = this.getTmpPath().toFile().getAbsolutePath() + File.separator + "Topmodel_output.txt";
			
			File file = new File(path);
			
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String line = "";
			String timeFormat = "yyyy-MM-dd hh:mm:ss";
			bw.write("Daily Runoff....");
			bw.newLine();
			Calendar startCalendar = this.getStartTime();
			Calendar endCalendar = this.getEndTime();
			line = "StartDate: " + TimeConverter.calendar2Str(startCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			line = "EndDate: " + TimeConverter.calendar2Str(endCalendar, timeFormat);
			bw.write(line);
			bw.newLine();
			bw.newLine();
			line = "Time[" + timeFormat + "], Runoff, Streamflow [l/s], PET, Precipitation";
			bw.write(line);
			bw.newLine();

			for (Calendar calendar : outputValues.keySet()) {
				String time = TimeConverter.calendar2Str(calendar, timeFormat);
				TopModelData modelData = outputValues.get(calendar);
				line = time + ", " + modelData.getRunoff() + ", " + modelData.getStreamFlow() + ", "
						+ modelData.getPet() + ", " + modelData.getPrecip();
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			bw.close();

			this.getFinalOutput().clear();
			this.getFinalOutput().put(this.runoff, new GeneralFileBinding(file));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			this.appendErr(e.getMessage());
			return false;
		}
	}
	
	@Override
	protected TypedProcessDescription createDescription() {
		return new TopmodelDescriptionGenerator().createDescription();
	}

	
	private double sum(double[] values) {
		double total = 0;
		for(double val:values) {
			total += val;
		}
		return total;
	}
	
	private void advanceStep() {
		this.getCurrentTime().add(Calendar.SECOND, (int)(_timeStep*60*60*24));
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

}
