package com.mcit

import java.io.FileReader
import java.time.Duration
import java.util.{Collections, Properties}

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import modelClass.UberRecordJSON
import scala.collection.JavaConversions._

import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkKafkaConsumerProducer extends App with Serializable {

  //import org.apache.spark.streaming.kafka.producer._
  // schema for uber data
  case class Uber(dt: String, lat: Double, lon: Double, base: String) extends Serializable
  case class Center(cid: Integer, clat: Double, clon: Double) extends Serializable
  val schema = StructType(Array(
  StructField("dt", TimestampType, true),
  StructField("lat", DoubleType, true),
  StructField("lon", DoubleType, true),
  StructField("base", StringType, true)
  ))

  def parseUber(str: String): Uber = {
  val p = str.split(",")
  Uber(p(0), p(1).toDouble, p(2).toDouble, p(3))
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
  var ac = new Array[Center](20)
  var index: Int = 0
  model.clusterCenters.foreach(x => {
  ac(index) = Center(index, x(0), x(1));
  index += 1;
})
  val cc: RDD[Center] = spark.sparkContext.parallelize(ac)
  val ccdf = cc.toDF()


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
      val value = record.value()
      println(s"Consumed record with key= $key and value= $value")
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