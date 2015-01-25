package server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.routing.RoundRobinGroup
import com.typesafe.config.ConfigFactory
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import spray.http.MediaTypes
import spray.httpx.unmarshalling._
import scala.language.postfixOps


import spray.can.Http
import spray.routing.HttpService


object RESTServer {

  def main(args: Array[String]) {
    if (args.length < 5) {
      println("Please input the following arguments : ")
      println("<No. of Server Actors> <No of Users> <Data Display Interval> <Server IP> <Server Port> <Twiiter Statistics P for Pareto S for Simple>")
    }
    else {

      val nServers = args(0).toInt
      val nUsers = args(1).toInt
      val dInterval = args(2).toInt
      val IP = args(3)
      val Port = args(4).toInt
      val dist = args(5)

      val usersPerServer = (nUsers.toDouble / nServers).ceil.toInt

      implicit val system = ActorSystem("RESTServer", ConfigFactory.load("server.conf"))

      val counterActor = system.actorOf(Props(new Counter(dInterval, nServers)), "Counter")
      val tweetDataActor = system.actorOf(Props(new TweetDataActor(nUsers)), "TweetDataActor")

      if(dist equalsIgnoreCase  "P")
        FollowerStats.paretoD(nUsers)
      else
        FollowerStats.SimpleTwitterStat(nUsers)

      //var start = 1
      //var end = usersPerServer
      for (rng <- 1 to nServers) {
        //val s = start; val e = end
        system.actorOf(Props(new ServerActor(counterActor, tweetDataActor, dInterval, nServers, nUsers)), "Server" + rng)
        //start = end + 1
        //end = if (end + usersPerServer > nUsers) nUsers else end + usersPerServer
      }

      var serverNo = 0
      val serverPList = List.fill(nServers) {
        serverNo += 1
        "/user/Server" + serverNo
      }
      val router = system.actorOf(RoundRobinGroup(serverPList).props(), "Router")

      val RESTSActor = system.actorOf(Props(new RESTServerActor(router)), "Main")
      IO(Http) ! Http.Bind(RESTSActor, interface = IP, port = Port)
    }
  }
}

class RESTServerActor(val router: ActorRef) extends Actor with RESTService {

  def actorRefFactory = context

  def receive = runRoute(StatusRoute ~ FriendshipRoute)
}


trait RESTService extends HttpService {
  implicit val router: ActorRef

  import server.TweetJson._
  //implicit val format = Serialization.formats(NoTypeHints)

  lazy val StatusRoute = {
    pathPrefix("statuses") {
      path("show" | "user_timeline") {
        respondWithMediaType(MediaTypes.`application/json`) {
          get {
            parameters('tweetID.as[String] ?, 'tweetCount.as[String] ?, 'userID.as[String] ?) { (tID, tCount, uID) =>
              ctx =>
                val tweetID = tID getOrElse "Show All"
                //println(tweetID)
                if (tweetID == "Show All") {
                  val userID = uID getOrElse "None"
                  //println(userID)
                  if (userID != "None") {
                    val tweetCount = tCount getOrElse "All"
                    //println(tweetCount)
                    router ! ShowTimeline(ctx, userID, tweetCount)
                  }
                  else {
                    ctx.complete("Please provide the userID.")
                  }
                } else {
                  router ! ShowSingleTweet(ctx, tweetID)
                }
            }
          }
        }
      } ~
        path("home_timeline") {
          respondWithMediaType(MediaTypes.`application/json`) {
            get {
              parameters('userID.as[String] ?, 'tweetCount.as[String] ?) { (uID, tCount) =>
                ctx =>
                  val userID = uID getOrElse "None"
                  if (userID != "None") {
                    val tweetCount = tCount getOrElse "All"
                    //println(tweetCount)
                    router ! ShowFriendsTweets(ctx, userID, tweetCount)
                  }
                  else {
                    ctx.complete("Please provide the userID.")
                  }

              }
            }
          }
        } ~
        path("update") {
          post {
            entity(as[TweetString]) { tweet =>
              ctx =>
                //println("Got update tweet a request")
                router ! TweetMessage(ctx, tweet)

            }
          }
        } ~
        path("destroy") {
          get {
            parameter('tweetID) { tID =>
              ctx => router ! DeleteTweet(ctx, tID)
            }
          }
        }
    }
  }

  lazy val FriendshipRoute = {
    pathPrefix("followers") {
      path("ids" | "list") {
        respondWithMediaType(MediaTypes.`application/json`) {
          get {
            parameters('userID, 'count.as[String] ? "all") { (uID, count) =>
              ctx =>
                //println("Got show followers message")
                router ! SendFollowers(ctx, uID, count)

            }
          }
        }
      }
    } ~
      pathPrefix("friends") {
        path("follow") {
          put {
            parameters('myID.as[String], 'friendID.as[String]) { (myID, friendID) =>
              ctx => router ! BeFriend(ctx, myID, friendID)
            }
          }
        } ~
          path("ids") {
            respondWithMediaType(MediaTypes.`application/json`)
            get {
              parameter('myID) { myUserID =>
                ctx => router ! FriendsID(ctx, myUserID)
              }
            }
          } ~
          path("unfollow") {
            put {
              //println("Unfollow Request")
              parameters('myID.as[String], 'friendID.as[String]) { (myID, frnID) =>
                ctx => router ! UnFollow(ctx, myID, frnID)
              }
            }
          }
      }
  }
}