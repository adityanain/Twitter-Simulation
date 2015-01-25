package server

import spray.routing.RequestContext

/**
 * Created by Aditya on 12/12/2014.
 */
sealed trait ServerCounterProtocol

case class TweetsStat(tweetsRecv: Int, tweetsSen: Int, nTweetsProcessed: Int) extends ServerCounterProtocol

case object ResetCounter extends ServerCounterProtocol

case object DisplayStat extends ServerCounterProtocol

case object TweetMore extends ServerCounterProtocol

case object UserConnected extends ServerCounterProtocol

case class createClients(nClients: Int) extends ServerCounterProtocol

case object SendServerInfo extends ServerCounterProtocol

case class ClientIDsRange(start: Int, end: Int) extends ServerCounterProtocol

case class SendClientIDs(clientSz: Int) extends ServerCounterProtocol


case class AddFriend(myIDNo: Int, fIDNo: Int)

case class RemoveFriend(myIDNo: Int, fIDNo: Int)

case class StoreTweetData(userID: String, tweetStr: String, time: String, followers: collection.mutable.Set[Int])

case class SendSingleTweet(ctx: RequestContext, tweetID: String)

case class SendFriendsTweets(ctx: RequestContext, userID: Int, tCount: String)

case class UpdateFollowers(myIDNo: Int, followerIDNo: Int, tweetStr: String, time: String)
