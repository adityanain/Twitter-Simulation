package client

import akka.actor.{Actor, ActorSystem, Props}
import spray.client.pipelining._
import org.json4s.jackson.Serialization._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object Client{

  def main(args: Array[String]) {
    val serverIP = "127.0.0.1" //args(0)
    val serverPort = "5150" //args(1)
    val uIDStart = 1//args(2).toInt
    val uIDEnd = 5//args(3).toInt
    val nUsers = 10//args(4).toInt

    val tweetInterval = if(args.length > 5) args(5).toInt else 1
    val UserTimelineInterval = if(args.length > 6) args(6).toInt else 10
    val HomeTimelineInterval = if(args.length > 7) args(7).toInt  else 10
    val FollowersInterval = if(args.length > 8) args(8).toInt else 60
    val FriendsInterval = if(args.length > 9) args(9).toInt else 60
    val FollowInterval = if(args.length > 10) args(10).toInt else 120
    val UnFollowInterval = if(args.length > 11) args(11).toInt else 120

    implicit val system = ActorSystem("Client")
    uIDStart to uIDEnd map (client => system.actorOf(Props(new ClientActor(serverIP, serverPort, nUsers,
      tweetInterval,
      UserTimelineInterval,
      HomeTimelineInterval,
      FollowersInterval,
      FriendsInterval,
      FollowInterval,
      UnFollowInterval )), "User" + client ))
    //import system.dispatcher
    //val pipeline = sendReceive
  }

}

class ClientActor(serverIP: String, serverPort: String, nUsers : Int,
                  tweetInterval : Int,
                  UserTimelineInterval : Int,
                  HomeTimelineInterval : Int,
                  FollowersInterval : Int,
                  FriendsInterval : Int,
                  FollowInterval : Int,
                  UnFollowInterval : Int) extends Actor{

  import client.ClientProtocol._
  import context.dispatcher

  val myID = self.path.name
  val serverPath = "http://%s:%s".format(serverIP, serverPort)

  val pipeline = sendReceive

  override def preStart() = {

    context.system.scheduler.scheduleOnce(tweetInterval second, self, SendTweetMessage)
    context.system.scheduler.scheduleOnce(UserTimelineInterval second, self, ShowUserTimelineMessage)
    context.system.scheduler.scheduleOnce(HomeTimelineInterval second, self, ShowHomeTimelineMessage)
    context.system.scheduler.scheduleOnce(FollowersInterval second, self, ShowFollowersMessage)
    context.system.scheduler.scheduleOnce(FriendsInterval second, self, ShowFriendsMessage)
    context.system.scheduler.scheduleOnce(FollowInterval second, self, SendFollowMessage)
    context.system.scheduler.scheduleOnce(UnFollowInterval second, self, SendUnFollowMessage)

  }

  def receive = {

    case SendTweetMessage =>
      val text = Random.alphanumeric.take(10).mkString
      val response = pipeline{Post(serverPath + "/statuses/update" , TweetString(myID, text))} map(_.entity.asString)

      response onComplete{
        case Success(result) => println(result)
        case Failure(error) => println("An error has occurred while sending tweet: " + error.getMessage)
      }

      context.system.scheduler.scheduleOnce(tweetInterval second, self, SendTweetMessage)

    case ShowUserTimelineMessage =>
      val response = pipeline{Get(serverPath + "/statuses/user_timeline?userID=" + myID)}
      response onComplete{
        case Success(result) =>
          println("--------------------------User Timeline of %s ---------------- ----------------".format(myID))
          println(result.entity.asString)
        case Failure(error) => println("Attempt to get followers list failed with : " + error.getMessage)
      }

      context.system.scheduler.scheduleOnce(UserTimelineInterval second, self, ShowUserTimelineMessage)

    case ShowHomeTimelineMessage /* Friends Tweets */ =>
      val response = pipeline{Get(serverPath + "/statuses/home_timeline?userID=" + myID)}
      response onComplete{
        case Success(result) =>
          println("--------------------------Home Timeline of %s ---------------- ----------------".format(myID))
          println(result.entity.asString)
        case Failure(error) => println(error.getMessage)
      }

      context.system.scheduler.scheduleOnce(HomeTimelineInterval second, self, ShowHomeTimelineMessage)


    case ShowFollowersMessage =>
      val response = pipeline{Get(serverPath + "/followers/ids?userID=" + myID )}
      response onComplete{
        case Success(result) =>
          println("--------------------------%s Followers---------------- ----------------".format(myID))
          println(result.entity.asString)
         /*val json = parse(result.entity.asString)
          println(json)*/
        case Failure(error) => println("Attempt to get followers list failed with : " + error.getMessage)
      }

      context.system.scheduler.scheduleOnce(FollowersInterval second, self, ShowFollowersMessage)

    case ShowFriendsMessage =>
      val response = pipeline{Get(serverPath + "/friends/ids?myID=" + myID)}
      response onComplete{
        case Success(result) =>
          println("-----------------------------%s Friends--------------------------------".format(myID))
          println(result.entity.asString)
          /*val json = parse(result.entity.asString)
          println(json)*/
        case Failure(error) => println(error.getMessage)
      }

      context.system.scheduler.scheduleOnce(FriendsInterval second, self, ShowFriendsMessage)

    case SendFollowMessage =>
      val friendID = "User" + (Random.nextInt(nUsers) + 1)
      val response = pipeline{Put(serverPath + "/friends/follow?myID=%s&friendID=%s".format(myID, friendID))}

      response onComplete{
        case Success(result) => println(result.entity.asString)
        case Failure(error) => println("An error has occurred : " + error.getMessage)
      }

      context.system.scheduler.scheduleOnce(FollowInterval second, self, SendFollowMessage)


    case SendUnFollowMessage =>
      val friendID = "User" + (Random.nextInt(nUsers) + 1)
      val response = pipeline{Put(serverPath + "/friends/unfollow?myID=%s&friendID=%s".format(myID, friendID))}

      response onComplete{
        case Success(result) => println(result.entity.asString)
        case Failure(error) => println("An error has occurred : " + error.getMessage)
      }
      context.system.scheduler.scheduleOnce(UnFollowInterval second, self, SendUnFollowMessage)

    case msg => println("User" + self.path.name + " received unknown message : " + msg.toString)
  }
}


