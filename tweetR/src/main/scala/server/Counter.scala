package server

import akka.actor.Actor

/**
 * Created by Aditya on 12/12/2014.
 */
class Counter(duration: Int, nServers: Int) extends Actor {

  var totalTweetsProcessed = 0
  var serverCount = 0
  var currClientSize = 0

  def receive = {
/*    case SendClientIDs(clientSz) =>
      // Store the references of new actors that will be created by the client helper actor
      val parent = sender().path.parent

      (currClientSize + 1 to currClientSize + clientSz) map { x =>
        val userID = "User" + x
        val actorPath = parent + "/" + userID
        Database.actorsMap += (userID -> actorPath)
      }
      // Send the requested info to client helper
      sender ! ClientIDsRange(currClientSize + 1, currClientSize + clientSz)
      currClientSize += clientSz*/

    case TweetsStat(tweetsRec, tweetsSent, nTweetsProcessed) =>
      //println("Received from " + sender)
      totalTweetsProcessed += nTweetsProcessed
      serverCount += 1
      if (serverCount == nServers) {
        //self ! DisplayStat
        //println("Total tweets received : " + tweetsRec)
        //println("Total tweets sent : " + tweetsSent)
        println("Total tweets processed : " + totalTweetsProcessed)
        serverCount = 0
        totalTweetsProcessed = 0
      }

    case DisplayStat =>
      println("Number of tweets processed by server is : " + totalTweetsProcessed)
      serverCount = 0
      totalTweetsProcessed = 0

    case _ => //println("Unknown counter message!!")
  }
}
