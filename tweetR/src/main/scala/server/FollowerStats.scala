package server

import java.awt.{BasicStroke, Color}
import org.jfree.chart.{ChartFrame, ChartFactory}
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}

import scala.collection.mutable
import scala.collection.mutable.Set
import scala.util.Random
import probability_monad.Distribution._

object FollowerStats {

  def GenFriends(nUsers: Int) = {

    0 to nUsers map { user =>
      Database.followersMap(user) -= user
      Database.friendsMap += collection.mutable.Set()
    }

    1 to nUsers map {
      user => Database.followersMap(user).foreach(
        follower => Database.friendsMap(follower) += user)
    }

    println("Size of the followers Map : " + Database.followersMap.size)
    println("Size of the friendsMap : " + Database.friendsMap.size)
  }

  def paretoD(nUsers: Int) = {
    Database.followersMap += mutable.Set(0)
    val size = nUsers
    val xm = if (size <= 10000) 1 else if (size <= 100000) 2 else if (size <= 400000) 3 else 3 * size / 400000
    val pdf = pareto(math.log10(5) / math.log10(4), xm)
    val samples = pdf.given(_ < size).sample(size).sortWith(_ > _)
    var totalSize = 0
    1 to size map { user =>
      val followers = if (samples(user - 1) - samples(user - 1).toInt < 0.45)
        samples(user - 1).floor.toInt
      else samples(user - 1).ceil.toInt
      totalSize += followers
      val follSet = Stream.fill(followers)(Random.nextInt(size) + 1)
      var follmSet = mutable.Set(follSet: _*)
      while (follmSet.size < followers) follmSet += Random.nextInt(size) + 1

      Database.followersMap += follmSet
    }

   //printInfo()
    GenFriends(nUsers)

    println("Total followers size : " + totalSize)
  }

  def SimpleTwitterStat(nUsers: Int) = {
    val sampleSize = 400000
    var strBatchSz = 1
    var endBatchSz = (10 * nUsers) / 100
    // 10% user have less than 3 followers
    Database.followersMap += mutable.Set(0)
    var scaledValue = ((3 * nUsers) / sampleSize).toInt
    var follCount = if (scaledValue >= 1) scaledValue else 1
    (strBatchSz to endBatchSz) map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      //arr.foreach(x => println(x))
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((20 * nUsers) / 100).toInt
    scaledValue = ((9 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 3
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      //val set = Set(arr : _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((30 * nUsers) / 100).toInt
    scaledValue = ((19 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 9
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((40 * nUsers) / 100).toInt
    scaledValue = ((36 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 19
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((50 * nUsers) / 100).toInt
    scaledValue = ((61 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 36
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((60 * nUsers) / 100).toInt
    scaledValue = ((99 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 61
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((70 * nUsers) / 100).toInt
    scaledValue = ((154 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 98
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((80 * nUsers) / 100).toInt
    scaledValue = ((246 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 154
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((90 * nUsers) / 100).toInt
    scaledValue = ((458 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 246
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((95 * nUsers) / 100).toInt
    scaledValue = ((819 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 458
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((96 * nUsers) / 100).toInt
    scaledValue = ((978 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 819
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((97 * nUsers) / 100).toInt
    scaledValue = ((1211 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 978
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((98 * nUsers) / 100).toInt
    scaledValue = ((1675 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 1211
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((99 * nUsers) / 100).toInt
    scaledValue = ((2991 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 1675
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    strBatchSz = endBatchSz + 1
    endBatchSz = ((99.9 * nUsers) / 100).toInt
    scaledValue = ((24964 * nUsers) / sampleSize).toInt
    follCount = if (scaledValue >= 1) scaledValue else 2991
    strBatchSz to endBatchSz map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    val nFoll = (0.5 * nUsers).toInt
    (endBatchSz + 1 to nUsers) map { user =>
      val arr = Array.fill(follCount)(Random.nextInt(nUsers) + 1).toSeq
      var set = mutable.Set(arr: _*)
      Database.followersMap += set
    }

    GenFriends(nUsers)

  }

  def printInfo() = {
    println("Size of followers map : " + Database.followersMap.length)
    for (idx <- 1 until Database.followersMap.length) {
      print(idx + " -> ")
      Database.followersMap(idx).foreach(ele => print(ele + " "))
      println()
    }

  }

  def plotDistribution() = {
    val map = Database.followersMap.clone()
    val series = new XYSeries("User vs # of followers")
    val data = for (i <- 1 until map.length) series.add(i, map(i).size)
    val dataSet = new XYSeriesCollection()
    dataSet.addSeries(series)
    plotGraph(dataSet)

  }

  def plotGraph(dataSet: XYSeriesCollection) = {
    val sc = ChartFactory.createXYLineChart("User vs # of followers, using Pareto distribution", "UserX", "# of followers", dataSet,
      PlotOrientation.VERTICAL, true, true, false)
    val plot = sc.getXYPlot
    val renderer = new XYLineAndShapeRenderer()
    plot.setOutlinePaint(Color.BLACK)
    plot.setOutlineStroke(new BasicStroke(2.0f))
    plot.setBackgroundPaint(Color.WHITE)
    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(Color.BLACK)
    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(Color.BLACK)
    renderer.setSeriesShapesVisible(0, false)
    plot.setRenderer(renderer)
    val frame = new ChartFrame("First", sc)
    frame.pack()
    frame.setVisible(true)
    frame.setForeground(Color.WHITE)
  }
}
