package com.mcit

import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.types.{TimestampType, _}
import org.apache.spark.sql.{SparkSession, _}

object UberData extends App {

  //case class Uber(dt: String, lat: Double, lon: Double, base: String) extends Serializable
  //var file: String = "/home/deshbandhu/MCIT_BigData/Uberv2/resource/uber.csv"
  var file: String = "resource/uber.csv"
  val schema = StructType(Array(
    StructField("dt", TimestampType, nullable = true),
    StructField("lat", DoubleType, nullable = true),
    StructField("lon", DoubleType, nullable = true),
    StructField("base", StringType, nullable = true)
  ))


  val spark: SparkSession = SparkSession.builder().master("local[*]").appName("uber").getOrCreate()
  val df: DataFrame = spark.read.option("inferSchema", "false").schema(schema).option("header", "false").csv(file)
  df.persist()//To avoid inconsistency in randomSplit()
  val featureCols = Array("lat", "lon")
  val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
  val df2 = assembler.transform(df)
  val Array(trainingData, testData) = df2.randomSplit(Array(0.7, 0.3), 5043)


  val kmeans = new KMeans().setK(10).setFeaturesCol("features").setMaxIter(10)
  val model = kmeans.fit(trainingData)

  println("Final Centers: =====================================")
  model.clusterCenters.foreach(println)

  val categories = model.transform(testData)
  categories.show()
  categories.createOrReplaceTempView("uber")

  categories.select()
 println("==================================111111")

  categories.groupBy("prediction").count().show()

  spark.sql("select prediction, count(prediction) as count from uber group by prediction").show

  spark.sql("SELECT hour(uber.dt) as hr,count(prediction) as ct FROM uber group By hour(uber.dt)").show
 model.write.overwrite().save("output/savemodel")
  val res = spark.sql("select dt, lat, lon, base, prediction as cid FROM uber order by dt")
   res.write.mode(SaveMode.Overwrite).format("json").save("output/uber.json")

}