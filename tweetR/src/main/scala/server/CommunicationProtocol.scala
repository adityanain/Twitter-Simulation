package server

import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.json.DefaultJsonProtocol
import spray.routing.RequestContext


/**
 * Created by Aditya on 12/12/2014.
 */
//sealed trait CommunicationProtocol
//trait Tweets

object TweetJson extends Json4sSupport with DefaultJsonProtocol {
  implicit def json4sFormats: DefaultFormats.type = DefaultFormats
}


case class TweetString(userID: String, tweet: String)

case class DeleteTweet(ctx: RequestContext, tID: String)

case class SendFollowers(ctx: RequestContext, uID: String, count: String)

case class BeFriend(ctx: RequestContext, myID: String, friendID: String)

case class FriendsID(ctx: RequestContext, myID: String)

case class UnFollow(ctx: RequestContext, myID: String, frnID: String)


case class ShowFriendsTweets(ctx: RequestContext, userID: String, tCount: String)

case class TweetMessage(ctx: RequestContext, tweet: TweetString)

case class ShowSingleTweet(ctx: RequestContext, tweetID: String)

case class DelTweet(tweetID: String)

case class ShowTimeline(ctx: RequestContext, userID: String, tweetCount: String)


case class FollowersID(ctx: RequestContext, userID: String, count: Option[String])

case class FollowAUser(ctx: RequestContext, myID: String, userID: String)

//extends Friendship

//with CommunicationProtocol
//case class FriendsID(ctx : RequestContext, myID: String) extends Friendship

//with CommunicationProtocol
//case class UnFollow(ctx : RequestContext, myID: String, userID: String) extends Friendship

//with CommunicationProtocol

/*object TwitterJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  //implicit val tweetFormat = jsonFormat2(TweetString)
  //implicit val ShowSingleTweetFormat = jsonFormat1(ShowSingleTweet)
  //implicit val delTweetFormat = jsonFormat1(DelTweet)
  //implicit val ShowTimelineFormat = jsonFormat2(ShowTimeline)

  //implicit val followersIDFormat = jsonFormat2(FollowersID)
  //implicit val followAUserFormat = jsonFormat2(FollowAUser)
  //implicit val friendsIDFormat = jsonFormat1(FriendsID)
  //implicit val unFollowFormat = jsonFormat2(UnFollow)
}*/

object CommunicationProtocol {
  /*  private implicit val formats = Serialization.formats(
      ShortTypeHints(List(
        classOf[Tweet],
        classOf[ShowSingleTweet],
        classOf[DelTweet],
        classOf[ShowTimeline],
        classOf[FollowersID],
        classOf[FollowAUser],
        classOf[FriendsID],
        classOf[UnFollow],
        classOf[Tweets]
      )
      )
    )*/
}
