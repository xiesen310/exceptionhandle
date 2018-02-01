/**
  * Created by Allen on 2018/1/30.
  */
object App {

  def main(args: Array[String]): Unit = {
    val time = "20180130-152448"
    val timeSplit = time.split("-")
    val timeSection = (timeSplit(1).charAt(0).toString + timeSplit(1).charAt(1).toString).toInt
    println(timeSection)

  }
}
