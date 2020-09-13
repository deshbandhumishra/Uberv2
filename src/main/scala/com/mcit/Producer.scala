package com.mcit
import java.io.FileReader
import java.util.{Collections, Properties}

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import modelClass.UberRecordJSON
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.errors.TopicExistsException

import scala.util.Try

object Producer {
  val configFileName = "resource/ConfluentKafkaCloud.config"
  val topicName = "uber-test1"
  val MAPPER = new ObjectMapper
  val props = buildProperties(configFileName)
  createTopic(topicName, 1, 3, props)
  val producer = new KafkaProducer[String, JsonNode](props)

  val callback = new Callback {
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      Option(exception) match {
        case Some(err) => println(s"Failed to produce: $err")
        case None => println(s"Produced record at $metadata")
      }
    }
  }

def sendData( data:org.apache.spark.sql.DataFrame):Unit = {
  println("=====================================================================Sending")

  val mydata = data.select("dt", "lat", "lon", "base").toJavaRDD
  for (x <- mydata) {
    val uberRecord = new UberRecordJSON(Option(x.get(0).toString), Option(x.get(1)), Option(x.get(2)), Option(x.get(3).toString))
    val key: String = "uber-key"
    val value: JsonNode = MAPPER.valueToTree(uberRecord)
    val record = new ProducerRecord[String, JsonNode](topicName, key, value)
   producer.send(record, callback)
  }
}


  producer.flush()
  //producer.close()
  println("Wrote ten records to " + topicName)

  def buildProperties(configFileName: String): Properties = {
    val properties: Properties = new Properties
    properties.load(new FileReader(configFileName))
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonSerializer")
    properties
  }

//You can delete this function after running atleast once to create the topic.
  def createTopic(topic: String, partitions: Int, replication: Int, cloudConfig: Properties): Unit = {
    val newTopic = new NewTopic(topic, partitions, replication.toShort)
    val adminClient = AdminClient.create(cloudConfig)
    Try (adminClient.createTopics(Collections.singletonList(newTopic)).all.get).recover {
      case e :Exception =>
        // Ignore if TopicExistsException, which may be valid if topic exists
        if (!e.getCause.isInstanceOf[TopicExistsException]) throw new RuntimeException(e)
    }
    adminClient.close()
  }
}


