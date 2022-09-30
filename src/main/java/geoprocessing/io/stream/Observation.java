package geoprocessing.io.stream;

import geoprocessing.util.TimeConverter;

import java.util.Calendar;

public class Observation {
    private Calendar time;
    private double value;
    private String timeFormat = "yyyy-MM-dd HH:mm:ss";
    public Observation(Calendar time, double value){
        this.time = time;
        this.value = value;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Observation{" +
                "time=" + TimeConverter.calendar2Str(this.getTime(),timeFormat) +
                ", value=" + value +
                '}';
    }
}
