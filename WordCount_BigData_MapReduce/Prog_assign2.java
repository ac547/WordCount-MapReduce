import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.*;

public class CloudProjectPart2
        {


public static void main(String[] args) throws Exception
        {
        //Creating an object of Configuration class, which loads the configuration parameters
        Configuration conf = new Configuration();
        //Creating the object of Job class and passing the confobject  and Job name as arguments. The Job class allows the user  to configure the job, submit it and control its execution.
        Job job = new Job(conf, "map_words_states");
        //Setting the jar by finding where a given class came from
        job.setJarByClass(CloudProjectPart2.class);
        //Setting the key class for job output data
        job.setOutputKeyClass(Text.class);
        //Setting the value class for job output data
        job.setOutputValueClass(Text.class);
        //Setting the mapper for the job
        job.setMapperClass(NewWordMapper.class);
        //Setting the reducer for the job
        job.setReducerClass(NewWordReducer.class);
        //Setting the Input Format for the job
        job.setInputFormatClass(TextInputFormat.class);
        //Setting the Output Format for the job
        job.setOutputFormatClass(TextOutputFormat.class);
        //Adding a path which will act as a input for MR job. args[0]  means it will use the first argument written on terminal  as input path
        FileInputFormat.addInputPath(job, new Path(args[0]));
        String intermediate_path="intermediate.txt";
        FileSystem fs = FileSystem.get(conf);
        fs.delete(new Path(intermediate_path), true);
        //Setting the path to a directory where MR job will dump the  output. args[1] means it will use the second argument written on terminal as output path
        FileOutputFormat.setOutputPath(job,new Path(intermediate_path));
        //Submitting the job to the cluster and waiting for its completion
        job.waitForCompletion(true);

        //Creating an object of Configuration class, which loads the configuration parameters

        //Creating the object of Job class and passing the confobject  and Job name as arguments. The Job class allows the user  to configure the job, submit it and control its execution.
        Job job1 = new Job(conf, "map_words_states");
        //Setting the jar by finding where a given class came from
        job1.setJarByClass(CloudProjectPart2.class);
        //Setting the key class for job output data
        job1.setOutputKeyClass(Text.class);
        //Setting the value class for job output data
        job1.setOutputValueClass(Text.class);
        //Setting the mapper for the job
        job1.setMapperClass(FinalMapper.class);
        //Setting the reducer for the job
        job1.setReducerClass(FinalReducer.class);
        //Setting the Input Format for the job
        job1.setInputFormatClass(TextInputFormat.class);
        //Setting the Output Format for the job
        job1.setOutputFormatClass(TextOutputFormat.class);
        //Adding a path which will act as a input for MR job. args[0]  means it will use the first argument written on terminal  as input path
        FileInputFormat.addInputPath(job1, new Path(intermediate_path));
//        fs.delete(new Path(args[1]), true);

        //Setting the path to a directory where MR job will dump the  output. args[1] means it will use the second argument written on terminal as output path
        FileOutputFormat.setOutputPath(job1,new Path(args[1]));

        //Submitting the job to the cluster and waiting for its completion
        job1.waitForCompletion(true);
        }
        }



 class FinalMapper extends Mapper<LongWritable, Text, Text, Text>
{
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
                String file_line = value.toString().replaceAll("\\s+", " ");
                String[] states_items_split=file_line.split(" ");
                String[] line_items = states_items_split[1].split("#");
                int max=-1;
                Integer count=0;
                TreeMap<Integer,TreeMap<String,Integer>> map=new TreeMap<Integer, TreeMap<String, Integer>>();
                for(String item_with_count:line_items){
                        count+=1;
                        String[] item_count=item_with_count.split("repeated");
                        Integer no_times=Integer.parseInt(item_count[1]);
                        if (map.containsKey(no_times)){
                                map.get(no_times).put(item_count[0],no_times);
                                System.out.println(states_items_split[0]);
                        }else {
                                TreeMap<String, Integer> map1=new TreeMap<String, Integer>();
                                map1.put(item_count[0],no_times);
                                map.put(no_times,map1);
                        }
                }
                String seq="";

                for(Integer i:map.keySet()){
                        int count1=0;
                        ArrayList<String> ans=new ArrayList<String>();

                        for (String s:map.get(i).keySet()){
                                ans.add(s);
                        }
                        Collections.sort(ans);
                        Collections.reverse(ans);

                        for (String a:ans){
                                seq=String.valueOf(count)+"."+a+";"+seq;
                                count-=1;
                        }
                }
                context.write(new Text(seq),new Text(states_items_split[0]));
        }}


class FinalReducer extends Reducer<Text, Text, Text, Text >
{   @Override
public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
{
        int count = 0;
        String states_name="";
        for (Text val : values)
        {
                states_name+=val.toString()+";";
        }
        context.write(key, new Text(states_name));
}

}
class ImpWords {
        int freq;
        String item_name;
        public ImpWords(String name,int freq){
                this.freq=freq;
                this.item_name=name;
        }
}

 class NewWordMapper extends Mapper<LongWritable, Text, Text, Text> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        private String[] impwords = new String[]{"education", "politics", "sports", "agriculture"};

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//        String line = value.toString();
                String line = value.toString().toLowerCase().replaceAll("[_|$#<>\\^=\\[\\]\\*/\\\\,;,.\\-:()?!\"']", " ");

                StringTokenizer tokenizer = new StringTokenizer(line);
                String filepath = ((FileSplit) context.getInputSplit()).getPath().toString();
                String country_name = filepath.split("/")[filepath.split("/").length - 1];
                while (tokenizer.hasMoreTokens()) {
                        String file_line = tokenizer.nextToken().toLowerCase();
//            System.out.println(file_line);
//            file_line=file_line.replaceAll("educational","");

                        String file_line_temp = file_line;

                        for (String item : impwords) {
                                if (item.equals(file_line)) {
                                        context.write(new Text(country_name), new Text(item + "repeated1"));
                                }
//                String[] get_items_part=file_line_temp.split(item);
//                if (get_items_part.length>1){
//                    context.write(new Text(country_name),new Text(item+"repeated"+String.valueOf(get_items_part.length-1)));
//                }
//                file_line_temp=file_line;
                        }


                }
        }
}
class NewWordReducer extends Reducer <Text, Text, Text, Text >
{
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
        {
                int count = 0;
                Map<String ,Integer> map=new HashMap<>();
                for (Text val : values)
                {
                        String[] line=val.toString().split("repeated");
                        Integer no_times=Integer.parseInt(line[1]);
                        String items=line[0];
                        if (map.containsKey(items)){
                                map.put(items,map.get(items)+no_times);

                        }else{
                                map.put(items,1);
                        }
                }
                String output_string="";
                for(String item:map.keySet()){
                        output_string+=item+"repeated"+String.valueOf(map.get(item))+"#";
                }
                context.write(key, new Text(output_string));
        }

}
 class PriorityQueue {

        private ImpWords[] heap;
        private  int current_heap_size;
        private  int heap_size;

        public PriorityQueue(int capacity){
                this.heap_size=capacity+1;
                heap=new ImpWords[heap_size];
                this.current_heap_size=0;
        }

        public void add(ImpWords node){

                heap[++current_heap_size]=node;
                int position=current_heap_size;
                while(position!=1&&node.freq<heap[position/2].freq){
                        heap[position]=heap[position/2];
                        position/=2;
                }
                heap[position]=node;
        }
        public int size()
        {
                return current_heap_size;
        }

        public boolean isEmpty()
        {
                return current_heap_size == 0;
        }

        public ImpWords remove(){
                int parent, child;
                ImpWords item, temp;
                if (isEmpty() )
                {
                        System.out.println("Heap is empty");
                        return null;
                }

                item=heap[1];
                temp=heap[current_heap_size--];

                parent=1;
                child=2;
                while (child<=current_heap_size){
                        if (child < current_heap_size && heap[child].freq > heap[child + 1].freq)
                                child++;
                        if (temp.freq <= heap[child].freq)
                                break;

                        heap[parent] = heap[child];
                        parent = child;
                        child *= 2;
                }
                heap[parent]=temp;
                return  item;
        }

}