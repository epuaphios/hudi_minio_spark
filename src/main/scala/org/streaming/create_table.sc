
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

val s3accessKey = "t8JW7DmAsRMSWrH2"
val s3secretKey = "heBYbecZBmz8bOslW37hUOpyQbo7MgoY"
val connectionTimeOut = "600000"
val s3endPointLoc: String = "http://0.0.0.0"
val hivemetastore: String = "thrift://0.0.0.0:9083"
val config = new SparkConf()
config.set("spark.sql.extensions", "org.apache.spark.sql.hudi.HoodieSparkSessionExtension")
config.set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.hudi.catalog.HoodieCatalog")
config.set("spark.hadoop.fs.s3a.endpoint", s3endPointLoc)
config.set("spark.hadoop.fs.s3a.access.key", s3accessKey)
config.set("spark.hadoop.fs.s3a.secret.key", s3secretKey)
config.set("hive.exec.dynamic.partition", "true")
config.set("hive.exec.dynamic.partition.mode", "nonstrict")
config.set("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
config.set("hive.metastore.uris", hivemetastore)
config.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")


val spark=SparkSession.builder().config(config).master("local[1]").appName("SparkByExamples").enableHiveSupport().getOrCreate()

spark.sql("DROP TABLE IF EXISTS ")
val sql_delta_table = """CREATE EXTERNAL TABLE
(`_hoodie_commit_time` string,`_hoodie_commit_seqno` string,`_hoodie_record_key` string,`_hoodie_partition_path` string,`_hoodie_file_name` string)
PARTITIONED BY ()
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hudi.hadoop.HoodieParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
LOCATION 's3a://hudi/'"""
spark.sql(sql_delta_table)
spark.sql("ALTER TABLE  ADD\n  PARTITION (cinsiyet = '') LOCATION 's3a://hudi///'")
spark.sql("ALTER TABLE  ADD\n  PARTITION (cinsiyet = '') LOCATION 's3a://hudi///'")



//val sdf = spark.sql("select * from syss3")
//sdf.printSchema()
//sdf.show()
spark.close()
