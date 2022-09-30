package geoprocessing.io.stream;

import geoprocessing.util.TimeConverter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Collection extends ArrayList<Observation>{
    private String name;

    public Collection(String name){
        this.name = name;
    }

    public void sort(){
        Collections.sort(this, new Comparator<Observation>() {
            @Override
            public int compare(Observation o1, Observation o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
    }
    public Collection getLastest(int num){
        this.sort();
        if(this.size()<=num)
            return this;

        Collection collection = new Collection(this.name);
        for(int i=this.size()-num-1;i<this.size();i++){
            collection.add(this.get(i));
        }
        collection.sort();
        return collection;
    }

    public void print(){
        for(Observation observation :this){
            System.out.println(observation);
        }
    }

    public Calendar getLastestTime(){
        this.sort();
        return this.get(this.size()-1).getTime();
    }
    public double getNeareastValue(Calendar time){
        int i =0;
        for(Observation observation:this){
            if(time.after(observation.getTime())){
                return observation.getValue();
            }
            break;
        }
        return 0;

      /*  double smallValue = this.get(i).getValue();
        double bigValue = this.get(i+1).getValue();

        return (smallValue+bigValue)/2;*/
    }

    public void clearBefore(Calendar time){
        int num = checkOrder(time);
        this.removeRange(0,num);
    }

    public int checkOrder(Calendar time){
        int i = 0;
        for(Observation observation:this){

            if(observation.getTime().before(time)){
                i++;
                continue;
            }

            break;
        }
        return i;
    }
    public Calendar lastestTime(){
        if(this.size()==0)
            return null;

        this.sort();
        return this.get(this.size()-1).getTime();
    }
    public static void main(String[] args){
        String timeFormat = "yyyy-MM-dd HH:mm:ss";
        Collection collection = new Collection("test collection");

        Observation observation1 = new Observation(TimeConverter.str2Calendar("2021-11-15 13:12:22",timeFormat),1);
        collection.add(observation1);

        Observation observation2 = new Observation(TimeConverter.str2Calendar("2021-11-15 13:10:22",timeFormat),2);
        collection.add(observation2);

        Observation observation3 = new Observation(TimeConverter.str2Calendar("2021-11-15 13:12:29",timeFormat),3);
        collection.add(observation3);

        collection.sort();
        Calendar tgt = TimeConverter.str2Calendar("2021-11-15 13:11:29",timeFormat);
        System.out.println(collection.getNeareastValue(tgt));

        System.out.println("the supposed order is "+collection.checkOrder(tgt));

        collection.removeIf(observation -> observation.getTime().before(tgt));
        collection.print();

    }
}
