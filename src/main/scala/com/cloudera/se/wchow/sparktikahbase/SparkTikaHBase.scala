package com.cloudera.se.wchow.sparktikahbase

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

// Tika stuff
import org.apache.tika
import org.apache.spark.input.PortableDataStream
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.BodyContentHandler
import java.io.InputStream
import java.io.DataInputStream

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}
import scala.collection.mutable.ArrayBuffer

// HBase stuff
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.spark.HBaseRDDFunctions._
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Put

object SparkTikaHBase {

  def main(args: Array[String]): Unit = {

    if (args.length < 3) {
      println("usage: SparkTikaHBase <input-directory> <tableName> <columnFamily> ")
      return
    }

    val inputDirName = args(0)
    val tableName = args(1)
    val columnFamily = args(2)

    val sparkConf = new SparkConf().setAppName("SparkTikaHBase " + tableName + " " + columnFamily)
    val sc = new SparkContext(sparkConf)

    try {

      // Initialize HBase
      val conf = HBaseConfiguration.create()
      // Pass the HBase configs and SparkContext to the HBaseContext
      // http://blog.cloudera.com/blog/2014/12/new-in-cloudera-labs-sparkonhbase/
      conf.addResource(new Path("/etc/hbase/conf/hbase-site.xml"))
      val hbaseContext = new HBaseContext(sc, conf)

      // Need to set this in order to recursively read Hadoop files from directory using Spark
      sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive","true")

      // flatMap to go through each file
      //  NOTE: flatMap allows us to return a rdd where each element is (Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])
      //   if we used map then each element would have been Array[(Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])]
      //   flatMap is a transformation that maps one element in the base RDD to multiple elements
      sc.binaryFiles(inputDirName, 1).flatMap((file: (String, PortableDataStream)) => {

        println("DEBUG processing file: " + file._1.toString)
        val myParser: AutoDetectParser = new AutoDetectParser()
        val inStream: InputStream = new DataInputStream(file._2.open)
        val contentHandler: BodyContentHandler = new BodyContentHandler(-1)
        val fileMetadata: Metadata = new Metadata()
        val context: ParseContext = new ParseContext()
        myParser.parse(inStream, contentHandler, fileMetadata, context)
        inStream.close()

        println("DEBUG ==content start ===============================================")
        println(contentHandler.toString())
        println("DEBUG ==content end   ===============================================")

        val metadataNames: Array[String] = fileMetadata.names()

        // Array of tuples with the format (Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])
        val stuffArrayBuffer: ArrayBuffer[(Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])] = ArrayBuffer()

        println("DEBUG metadataNames.size:" + metadataNames.size)

        // format dates to ISO 8601 format
        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"))

        // create a collection to hold everything
        // Collection should be like this:
        // Array[(Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])]
        // This corresponds to an array of:
        //     RowKey, [ ColumnFamily, Column, Value ]
        for ((name, index) <- metadataNames.zipWithIndex) {
          println(index + " : " + name + " : " + fileMetadata.get(name))

          // Remove non word characters with a underscore in the name. This will be the column name in HBase
          // Lowercase the column name as well
          // Only store the first 500 characters, anything else is probably not interesting
          if ( fileMetadata.get(name).length() > 500 ) {
            // Add tuple to the ArrayBuffer. NOTE: need to add another pair of parentheses
            stuffArrayBuffer+=((Bytes.toBytes(file._1.toString),
              Array((Bytes.toBytes(columnFamily), Bytes.toBytes(name.replaceAll("\\W", "_").toLowerCase()), Bytes.toBytes(fileMetadata.get(name).substring(0, 499))))))

          }
          else {
            if (name == "date") {
              // Make the date field a ISO 8601 value in UTC time
              // Note: For Tika, if the timezone isn't specified when the metadata is created then it is assumed to be in UTC time and
              //   this is not always accurate because it could be wrong.
              //   It would be more accurate to use the GPS Time-Stamp if it is present

              // Add tuple to the ArrayBuffer. NOTE: need to add another pair of parentheses
              stuffArrayBuffer+=((Bytes.toBytes(file._1.toString),
                Array((Bytes.toBytes(columnFamily), Bytes.toBytes(name.replaceAll("\\W", "_").toLowerCase()), Bytes.toBytes(dateFormatter.format(fileMetadata.getDate(TikaCoreProperties.CREATED)))))))
              println(index + " : DEBUG : " + name + " : " + dateFormatter.format(fileMetadata.getDate(TikaCoreProperties.CREATED)))

            }
            else {
              // Add tuple to the ArrayBuffer. NOTE: need to add another pair of parentheses
              stuffArrayBuffer+=((Bytes.toBytes(file._1.toString),
                Array((Bytes.toBytes(columnFamily), Bytes.toBytes(name.replaceAll("\\W", "_").toLowerCase()), Bytes.toBytes(fileMetadata.get(name))))))
            }
          }
        }
        println("DEBUG metadata extracted")

        stuffArrayBuffer

      }).hbaseBulkPut(hbaseContext, TableName.valueOf(tableName),
        (putRecord) => {
        val put = new Put(putRecord._1)
        putRecord._2.foreach((putValue) => put.addColumn(putValue._1, putValue._2, putValue._3))
        put
      })


    } finally {
      sc.stop()
    }
  }
}
