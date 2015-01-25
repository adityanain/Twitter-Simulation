package server

import akka.actor.Actor
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import spray.http.MediaTypes
import spray.routing.directives.RespondWithDirectives.respondWithMediaType

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable



class TweetDataActor(nUsers: Int) extends Actor {

  case class Tweet(userID: String, text: String, time: String)

  case class TweetsList(tweets: List[Tweet])

  case class SendTweet(userID: String, list: TweetsList)


  var tweetsCounter = 0
  val tweetsWithID = new ArrayBuffer[Tweet]()
  // Array Index is the tweet ID
  val friendsTweets = mutable.ParMap[Int, collection.mutable.Queue[Int]]()
  1 to nUsers map { user => friendsTweets += (user -> collection.mutable.Queue[Int]())}

  //import server.TweetJson._
  implicit val format = Serialization.formats(NoTypeHints)

  def receive = {
    case StoreTweetData(userID, tweetStr, time, followers) =>
      tweetsWithID += Tweet(userID, tweetStr, time)
      followers.foreach(follower => friendsTweets(follower).enqueue(tweetsCounter))
      tweetsCounter += 1

    case SendSingleTweet(ctx, tweetID) =>
      ctx.complete(writePretty(tweetsWithID(tweetID.toInt - 1)))

    case SendFriendsTweets(ctx, userID, tCount) =>
      var list = collection.mutable.ListBuffer[Tweet]()
      if (tCount equals "All") {
        friendsTweets(userID) foreach { tweetID => list += tweetsWithID(tweetID)}
      } else {
        val qSize = friendsTweets(userID).size
        val tweetCount: Int = if (tCount.toInt > qSize) qSize else tCount.toInt
        friendsTweets(userID).takeRight(tweetCount) foreach (tweetID => list += tweetsWithID(tweetID))
      }
      ctx.complete(writePretty(list))

    case _ => println("Tweet Data Actor received unknown message!!")
  }
}
