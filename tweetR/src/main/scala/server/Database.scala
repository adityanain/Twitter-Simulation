package server

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Set}

/**
 * Created by Aditya on 12/12/2014.
 */
object Database {
  //var userCount = 0
  //val mapID = mutable.HashMap[String, Int]()
  val followersMap = new ArrayBuffer[mutable.Set[Int]]()
  // Initial followers list
  val friendsMap = new ArrayBuffer[mutable.Set[Int]]()
  // Initial friends list
  val actorsMap = collection.mutable.HashMap[String, String]()
  //val FriendsMap = new ArrayBuffer[mutable.Set[Int]]()
  //val messageQBuff = new ConcurrentHashMap[Int, mutable.Queue[(String, String)]]()
}
