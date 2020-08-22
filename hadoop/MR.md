```
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
public class MyWordCountApp
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 2) System.out.println("nothing");
        Job job = Job.getInstance();
        Configuration conf = job.getConfiguration();
        FileSystem fs = FileSystem.get(conf);
        fs.delete(new Path(args[1]), true);
        job.setJarByClass(MyWordCountApp.class);
        job.setJobName("word count");
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.getConfiguration().set("mapreduce.input.keyvaluelinerecordreader.key.value.separator",">");
        job.setMapperClass(MyWordCountMapper.class);
        job.setReducerClass(MyWordCountReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setNumReduceTasks(0);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
 
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
 
import java.io.IOException;
 
public class MyWordCountMapper extends Mapper<Text, Text, Text, IntWritable>
{
    @Override
    protected void map(Text key, Text value, Mapper<Text, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException
    {
        String[] arr = value.toString().split(" ");
        for (String w : arr)
        {
            context.write(new Text(w),new IntWritable(1));
        }
    }
}
 
 
 
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
 
import java.io.IOException;
 
public class MyWordCountReduce extends Reducer<Text, IntWritable, Text, IntWritable>
{
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException
    {
        int count = 0;
        for (IntWritable c : values)
        {
            count=count +c.get();
        }
    }
}
```
