/**
 * Created by Aditya on 12/12/2014.
 */


package server

import java.util.Date

import akka.actor.{Actor, ActorRef}
import spray.routing.RequestContext
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import spray.http.MediaTypes
import spray.routing.directives.RespondWithDirectives.respondWithMediaType

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

class ServerActor(counter: ActorRef, tweetDataActor: ActorRef, displayInterval: Int,
                  nServers: Int, nUsers: Int /*,iDst: Int, iDen: Int*/) extends Actor {

  var nTweetsRecv = 0
  var nTweetsSent = 0
  val thisServerIDNo = self.path.name.drop(6).toInt
  val usersPerServer = (nUsers.toDouble / nServers).ceil.toInt

  // Server Data
  val msgQ = mutable.Map[Int, mutable.Queue[(String, String)]]()
  //val friendsTweetsQ = mutable.Map[Int, mutable.Queue[(Int, String, String)]]()

  /*  iDst to iDen map { id =>
      msgQ += (id -> mutable.Queue[(String, String)]())
      //friendsTweetsQ += (id -> mutable.Queue[(Int, String, String)]())
    }*/

  val followersMap = mutable.HashMap[Int, mutable.Set[Int]]()
  val friendsMap = mutable.HashMap[Int, mutable.Set[Int]]()

  for (user <- thisServerIDNo to nUsers by nServers) {
    msgQ += (user -> mutable.Queue[(String, String)]())
    followersMap += (user -> Database.followersMap(user))
    friendsMap += (user -> Database.friendsMap(user))
  }

  //println("Followers Map for Server" + thisServerIDNo + " : " + followersMap)
  //println("Friends Map for Server" + thisServerIDNo + " : " +friendsMap)


  implicit def reset(): Unit = {
    counter ! TweetsStat(nTweetsRecv, nTweetsSent, nTweetsRecv + nTweetsSent)
    nTweetsRecv = 0
    nTweetsSent = 0
    //println("Server : " + self + " reset the tweet counter")
  }

  override def preStart() = {
    import context.dispatcher
    context.system.scheduler.schedule(0 second, displayInterval milliseconds, self, ResetCounter)
  }

  def now() = new Date().toString

  def storeTweet(userID: String, tweet: String, timestamp: String) = {
    val uIDNo = userID.drop(4).toInt // Database.mapID(userID)
    if (msgQ(uIDNo).size > 100)
      msgQ(uIDNo).dequeue()

    msgQ(uIDNo).enqueue(Tuple2(tweet, timestamp))
  }

  case class Tweet(text: String, time: String)

  case class TweetsList(tweets: List[Tweet])

  case class SendTweet(userID: String, list: TweetsList)

  case class FollowersList(userID: String, no_of_followers: String, followers: mutable.Set[String])

  case class FriendsList(MyID: String, no_of_friends: String, friends: mutable.Set[String])

  //import server.TweetJson._

  implicit val format = Serialization.formats(NoTypeHints)
  /*
    def storeFrTweet(friendsIDNo: Int, myIDNo: Int, tweetStr: String, time: String) = {
      if(friendsTweetsQ(myIDNo).size > 10000)
        friendsTweetsQ(myIDNo).dequeue()
      friendsTweetsQ(myIDNo).enqueue(Tuple3(friendsIDNo, tweetStr, time))
    }*/

  def receive = {
    case ShowTimeline(ctx, userID, tCount) =>
      //println("Got Show Timeline message")
      val uIDNo = userID.drop(4).toInt
      val hServerNo = (uIDNo - 1) % nServers + 1
      if (thisServerIDNo != hServerNo) context.actorSelection("../Server" + hServerNo) ! ShowTimeline(ctx, userID, tCount)
      else {
        val tweetQLen = msgQ(uIDNo).size
        val tweetCount = if (tCount equals "All") tweetQLen else if (tCount.toInt < tweetQLen) tCount.toInt else tweetQLen
        var tweetList = collection.mutable.ListBuffer[Tweet]()
        msgQ(uIDNo).takeRight(tweetCount) foreach (msg => tweetList += Tweet(msg._1, msg._2))
        val tweetJson = writePretty(SendTweet(userID, TweetsList(tweetList.toList)))
        //println(tweetJson)
        ctx.complete(tweetJson)
      }

    case ShowSingleTweet(ctx, tweetID) =>
      //println("Displaying the tweet soon!!")
      tweetDataActor ! SendSingleTweet(ctx, tweetID)

    case ShowFriendsTweets(ctx, userID, tCount) =>
      val uID = userID.drop(4).toInt
      tweetDataActor ! SendFriendsTweets(ctx, uID, tCount)

    case TweetMessage(ctx, tweet) =>
      val uID = tweet.userID
      val uIDNo = uID.drop(4).toInt
      val hServerNo = (uIDNo - 1) % nServers + 1
      if (thisServerIDNo != hServerNo) context.actorSelection("../Server" + hServerNo) ! TweetMessage(ctx, tweet)
      else {
        //println("Updating tweet !!")
        nTweetsRecv += 1
        val tStr = tweet.tweet
        //println(uID, tStr)
        val time = now()
        //println(time)
        storeTweet(uID, tStr, time)
        val followers = followersMap(uIDNo)
        tweetDataActor ! StoreTweetData(uID, tStr, time, followers)
        // Can be changed
        /*        for(follNo <- Database.followersMap(uIDNo)) {
                  val cServerNo = (follNo - 1) % nServers + 1
                  context.actorSelection("../Server" + cServerNo) ! UpdateFollowers(uIDNo, follNo, tStr, time)
                  nTweetsSent += 1
                }*/
        ctx.complete("Tweet by user : %s updated on %s".format(uID, time))
      }

    //@todo
    case DeleteTweet(ctx, tweetID) =>
      //println("Deleted tweet with id : " + tweetID)
      ctx.complete("Deleted")

    case SendFollowers(ctx, userID, count) =>
      val uIDNo = userID.drop(4).toInt
      val hServerNo = (uIDNo - 1) % nServers + 1
      if (thisServerIDNo != hServerNo) context.actorSelection("../Server" + hServerNo) ! SendFollowers(ctx, userID, count)
      else {
        val follsSet = followersMap(uIDNo)
        val noOfFollowers = follsSet.size
        val n = if (count equals "all") noOfFollowers else if (count.toInt < noOfFollowers) count.toInt else noOfFollowers
        val followers = follsSet.map(no => "User" + no).take(n)
        val follsJson = writePretty(FollowersList(userID, follsSet.size.toString, followers))
        //println(follsJson)
        ctx.complete(follsJson)
      }

    case AddFriend(myIDNo, fIDNo) =>
      friendsMap(myIDNo) += fIDNo

    case BeFriend(ctx, myID, frnID) =>
      val fIDNo = frnID.drop(4).toInt
      val myIDNo = myID.drop(4).toInt
      val myServerNo = (myIDNo - 1) % nServers + 1
      val fServerNo = (fIDNo - 1) % nServers + 1

      if (thisServerIDNo != fServerNo) context.actorSelection("../Server" + fServerNo) ! BeFriend(ctx, myID, frnID)
      else {
        followersMap(fIDNo) += myIDNo

        if (thisServerIDNo == myServerNo) friendsMap(myIDNo) += fIDNo
        else context.actorSelection("../Server" + myServerNo) ! AddFriend(myIDNo, fIDNo)

        ctx.complete("You (%s) have started following %s".format(myID, frnID))
      }

    case FriendsID(ctx, myID) =>
      val uIDNo = myID.drop(4).toInt
      val hServerNo = (uIDNo - 1) % nServers + 1
      if (thisServerIDNo != hServerNo) context.actorSelection("../Server" + hServerNo) ! FriendsID(ctx, myID)
      else {
        val friendsSet = friendsMap(uIDNo)
        val friends = friendsSet.map(no => "User" + no)
        val frnJson = writePretty(FriendsList(myID, friendsSet.size.toString, friends))
        //println(frnJson)
        ctx.complete(frnJson)
      }

    case RemoveFriend(myIDNo, fIDNo) =>
      friendsMap(myIDNo) -= fIDNo

    case UnFollow(ctx, myID, frnID) =>
      val fIDNo = frnID.drop(4).toInt
      val myIDNo = myID.drop(4).toInt
      val myServerNo = (myIDNo - 1) % nServers + 1
      val fServerNo = (fIDNo - 1) % nServers + 1

      if (thisServerIDNo != fServerNo) context.actorSelection("../Server" + fServerNo) ! BeFriend(ctx, myID, frnID)
      else {
        followersMap(fIDNo) -= myIDNo

        if (thisServerIDNo == myServerNo) friendsMap(myIDNo) -= fIDNo
        else context.actorSelection("../Server" + myServerNo) ! RemoveFriend(myIDNo, fIDNo)

        ctx.complete("You (%s) are no longer following %s".format(myID, frnID))
      }

    case ResetCounter =>
      //println("Tweet Processed by : " + self.path.name)
      //println("Tweets Received : " + nTweetsRecv)
      //println("Tweets Sent : " + nTweetsSent)
      //val total = nTweetsRecv + nTweetsSent
      //println("Tweets Total : " + total )

      counter ! TweetsStat(nTweetsRecv, nTweetsSent, nTweetsRecv + nTweetsSent)
      nTweetsRecv = 0
      nTweetsSent = 0

    case _ => println("Server %s received unknown message ".format(self.path.name))
  }
}
