package com.mcit

import java.io.FileReader
import java.time.Duration
import java.util.{Collections, Properties}

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{TimestampType, _}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConversions._

object SparkKafkaConsumerProducer extends App with Serializable {

  //import org.apache.spark.streaming.kafka.producer._
  // schema for uber data
  case class Center(cid:Option[Integer], clat:Option[Double], clon:Option[Double]) //extends Serializable
  val schema = StructType(Array(
  StructField("dt", TimestampType, true),
  StructField("lat", DoubleType, true),
  StructField("lon", DoubleType, true),
  StructField("base", StringType, true)
  ))
  //case class Uber(dt: Option[String]=Some(""), lat: Option[Double]=Some(0.0D), lon: Option[Double]=Some(0.0D), base: Option[String]=Some("")) extends Serializable
  case class Uber(dt: Option[String], lat:Option[Double], lon: Option[Double], base:Option[String])
  def parseUber(str: String): Uber = {
   val regex= """"((?:[^"\\]|\\[\\"ntbrf])+)"""".r
   val str1 = regex.findFirstMatchIn(str).get.group(1)
    println("=======split=="+str1)
  val p = str1.split(",")
    println(p(0)+" "+p(1)+" "+p(2)+" "+p(3))
    Uber(Option(p(0).toString), Option(p(1).toDouble), Option(p(2).toDouble), Option(p(3).toString))
  //Uber(Some(p(0)), Some(p(1).toDouble), Some(p(2).toDouble), Some(p(3)))
}

  // If using spark-submit script, the master should not be set
  val sparkConf = new SparkConf().setAppName("UberStream")
  val spark = SparkSession.builder().master("local[*]").appName("ClusterUber").getOrCreate()
  val ssc = new StreamingContext(spark.sparkContext, Seconds("2".toInt))


  import spark.implicits._

  // load model for getting clusters
  val model = KMeansModel.load("resource/model")
  // print out cluster centers
  model.clusterCenters.foreach(println)
  // create a dataframe with cluster centers to join with stream
  var ac = new Array[Center](10)
  var index: Integer = 0
  model.clusterCenters.foreach(x => {
  ac(index) = Center(Option(index), Option(x(0)), Option(x(1)))
    println("=========1=======success============"+index+" = "+x(0)+" = "+x(1))
  index += 1
})
  val cc: RDD[Center] = spark.sparkContext.parallelize(ac)
  cc.foreach(x=>"=====2.Success==="+x)
  val ccdf = cc.toDF()
ccdf.show()

  val configFileName ="resource/ConfluentKafkaCloud.config" //args(0)
  val topicName = "uber-test1"
  val props = buildProperties(configFileName)
  val consumer = new KafkaConsumer[String, JsonNode](props)
  val MAPPER = new ObjectMapper
  consumer.subscribe(Collections.singletonList(topicName))

  while(true) {
    println("Polling")
    val records = consumer.poll(Duration.ofSeconds(1))
        for (record <- records) {
      val key = record.key()
      val value = record.value().toSeq
println("===================1111111111========================"+value)
      val rdd = spark.sparkContext.parallelize(value)
      val df = rdd.map(x=> parseUber(x.toString)).toDF()
      //val df = spark.read.json(Seq(value).toDS).toDF()

      df.printSchema()
      df.show()

      println(s"Consumed record with key= $key and value= $value")
      //=========================================================
      // Display the top 20 rows of DataFrame
      println("uber data")
      df.show()

      // get features to pass to model
      val featureCols = Array("lat", "lon")
      val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
      val df2 = assembler.transform(df)

      // get cluster categories from  model
      val categories = model.transform(df2)
        categories.show
      categories.createOrReplaceTempView("uber")

      println("=====1.===========MyDDDDDDDDDDdddddd==================================================")
      // select values to join with cluster centers
      // convert results to JSON string to send to topic

      val clust = categories.select($"dt", $"lat", $"lon", $"base", $"prediction".alias("cid")).orderBy($"dt")
          println("===2.=============MyDDDDDDDDDDdddddd==================================================")
         clust.show()
          ccdf.show()
          val res = clust.join(ccdf, Seq("cid")).orderBy($"dt")
          println("=====3.===========MyDDDDDDDDDDdddddd==================================================")

          res.show
          println("=====4.===========MyDDDDDDDDDDdddddd==================================================")


      val tRDD: org.apache.spark.sql.DataFrame = res

      Producer.sendData(tRDD)
     // val temp: RDD[String] = tRDD1.rdd
    //  temp.sendToKafka[StringSerializer](topicp, producerConf)

      println("sending messages")
      tRDD.take(2).foreach(println)

      //==========================================================
    }
  }
  consumer.close()


  def buildProperties(configFileName: String): Properties = {
    val properties = new Properties()
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonDeserializer")
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "sparkApplication")

    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "maprdemo:9092")
    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1000)
    properties.load(new FileReader(configFileName))
    properties
  }

}