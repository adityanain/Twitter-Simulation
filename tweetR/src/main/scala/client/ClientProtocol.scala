package client

import org.json4s.{NoTypeHints, DefaultFormats}
import org.json4s.jackson.Serialization
import spray.httpx.Json4sSupport
import spray.json.DefaultJsonProtocol

import scala.collection.mutable

object ClientProtocol extends Json4sSupport with DefaultJsonProtocol{
    implicit def json4sFormats: DefaultFormats.type = DefaultFormats
    //implicit val format = Serialization.formats(NoTypeHints)
}

case class Tweet(text: String, time: String)
case class TweetsList(tweets : List[Tweet])
case class SendTweet(userID: String, list : TweetsList)
case class FollowersList(userID: String, no_of_followers : String, followers : List[String])
case class FriendsList(MyID : String, no_of_friends : String, friends : mutable.Set[String])
case class RespString(text : String)

case object ShowUserTimelineMessage

case object SendUnFollowMessage

case object SendFollowMessage

case object ShowFriendsMessage

case object ShowFollowersMessage

case object ShowHomeTimelineMessage

case object SendTweetMessage
case class TweetString(userID: String, tweet: String)
