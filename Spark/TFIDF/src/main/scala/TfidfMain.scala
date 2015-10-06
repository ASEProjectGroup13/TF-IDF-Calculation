/**
 * Created by Venu on 10/6/15.
 */

import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.{SparkContext, SparkConf}
object TfidfMain {

  def main(args: Array[String]) {

    val conf = new SparkConf()
      .setAppName(s"IPApp")
      .setMaster("local[*]")
      .set("spark.executor.memory", "2g")

    val sparkContext = new SparkContext(conf)

    val dataFiles = sparkContext.textFile("src/main/resources/data").map(l => l.split(" ").toSeq)

    println("dataaaaaaa")

    val hashingTF = new HashingTF()

    println("hiiiiiii")

    val tf = hashingTF.transform(dataFiles)


    tf.cache()

    val idf = new IDF(minDocFreq = 1).fit(tf)

    val tfidf = idf.transform(tf)

    tfidf.saveAsTextFile("src/main/resources/output")






  }

}
