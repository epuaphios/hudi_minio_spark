package org.streaming

import org.apache.hudi.DataSourceWriteOptions
import org.apache.hudi.config.HoodieWriteConfig
import org.apache.hudi.hive.MultiPartKeysValueExtractor
import org.apache.log4j._
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.{col, from_json, regexp_extract}
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession, functions}

object streaming {

  val hudiOptions = Map[String,String](
    DataSourceWriteOptions.RECORDKEY_FIELD_OPT_KEY -> "",
    DataSourceWriteOptions.PARTITIONPATH_FIELD_OPT_KEY -> "",
    HoodieWriteConfig.TABLE_NAME -> "",
    DataSourceWriteOptions.OPERATION_OPT_KEY ->
    DataSourceWriteOptions.UPSERT_OPERATION_OPT_VAL,
    DataSourceWriteOptions.PRECOMBINE_FIELD_OPT_KEY -> "",
    //    DataSourceWriteOptions.HIVE_STYLE_PARTITIONING.key()-> "dogum_yili",
    DataSourceWriteOptions.METASTORE_URIS.key() -> "thrift://0.0.0.0:9083",
    DataSourceWriteOptions.HIVE_SYNC_MODE.key()-> "hms",
    DataSourceWriteOptions.HIVE_DATABASE.key() -> "hive",
    DataSourceWriteOptions.HIVE_TABLE.key() -> "",
    DataSourceWriteOptions.HIVE_SYNC_ENABLED.key()-> "true",
    DataSourceWriteOptions.HIVE_PARTITION_FIELDS.key() -> "",
    DataSourceWriteOptions.HIVE_PARTITION_EXTRACTOR_CLASS_OPT_KEY -> classOf[MultiPartKeysValueExtractor].getName
  )

  val s3accessKey = ""
  val s3secretKey = ""
  val hudiTablePath = "s3a://hudi/hastakimlik22/"
  val s3endPointLoc: String = "http://0.0.0.0"
  val hivemetastore: String = "thrift://0.0.0.0:9083"


  def main(args: Array[String]) = {
//    Logger.getLogger("org").setLevel(Level.INFO)
//    val s3accessKey = "t8JW7DmAsRMSWrH2"
//    val s3secretKey = "heBYbecZBmz8bOslW37hUOpyQbo7MgoY"
//    val connectionTimeOut = "600000"

    val config = new SparkConf()
    config.set("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionExtension")
    config.set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
    config.set("spark.hadoop.fs.s3a.endpoint", s3endPointLoc)
    config.set("spark.hadoop.fs.s3a.access.key", s3accessKey)
    config.set("spark.hadoop.fs.s3a.secret.key", s3secretKey)
    config.set("spark.memory.fraction", "0.4")
    config.set("spark.memory.storageFraction", "0.4")
    config.set("spark.kubernetes.memoryOverheadFactor","0.2")
    config.set("spark.executor.memoryOverhead", "20g")
    config.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    //config.set("spark.streaming.kafka.consumer.cache.maxCapacity", "250")
    config.set("spark.kryoserializer.buffer.max", "32m")
//    config.set("spark.executor.memory","16g")
//    config.set("spark.kubernetes.executor.limit.cores","4")
    config.set("spark.executor.extraJavaOptions", "-verbose:gc -XX:+PrintGCDetails -XX:+UseStringDeduplication -XX:+UseCompressedOops")
    config.set("spark.driver.extraJavaOptions", "-XX:+UseG1GC")
   // config.set("spark.kubernetes.memoryOverheadFactor","0.5")
   // config.set("spark.task.maxFailures","16")
    //config.set("spark.max.fetch.failures.per.stage","16")
    config.set("spark.hadoop.fs.s3a.fast.upload", "true")
    config.set("spark.hadoop.fs.s3a.connection.maximum", "5000")
    config.set("spark.hadoop.fs.s3a.attempts.maximum", "200")
    config.set("spark.hadoop.fs.s3a.connection.timeout", "300000")
    config.set("spark.hadoop.fs.s3a.threads.max", "400")
    //config.set("spark.rpc.io.serverThreads","64")
    config.set("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
    //config.set("hive.metastore.uris", hivemetastore)
    //config.set("spark.sql.shuffle.partitions", "330")
    //config.set("spark.default.parallelism", "330")
    //config.set("spark.hadoop.fs.s3a.connection.timeout", connectionTimeOut)
    //config.set("spark.databricks.delta.retentionDurationCheck.enabled", "false")
    config.set("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")



    val spark = SparkSession.builder().config(config)
       //.master("local[*]")
       .appName("SparkByExamples").getOrCreate()





    val df = spark
      .readStream
      .format("kafka")
      //.option("kafka.bootstrap.servers", "0.0.0.0:31111")
      .option("kafka.bootstrap.servers", "test-cluster-kafka-bootstrap.kafka-strimzi:9092")
      .option("subscribe", "enabiz-mutation_repeat")
      .option("group.id", "group")
      .option("kafka.session.timeout.ms", "60000")
      .option("startingOffsets", "earliest")
      .option("maxOffsetsPerTrigger", "16500000")
      .load()

    import spark.implicits._

    val r = """^Struct\{event=(.{0,64}),key=(.{0,64}),cas=(\d{0,64}),content=(\{{0,1}.*\}{0,1})\}$"""


    val df2 = df.select(
      $"value",
      regexp_extract($"value", r, 1).as("event"),
      regexp_extract($"value", r, 2).as("key"),
      regexp_extract($"value", r, 3).as("cas"),
      from_json(regexp_extract($"value", r, 4),schemaRecord).as("content")
    )


    val query: StreamingQuery = df.writeStream.option("checkpointLocation", "/tmp/checkpoint2/")
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        batchDF.write.format("hudi").
          options(hudiOptions).
          option("hoodie.upsert.shuffle.parallelism","100").
          option("hoodie.insert.shuffle.parallelism","100").
          //option(TABLE_NAME, "hastakimlik18").
          mode(SaveMode.Append).
          save(hudiTablePath)
    }
    .start()

    query.awaitTermination()
  }
}