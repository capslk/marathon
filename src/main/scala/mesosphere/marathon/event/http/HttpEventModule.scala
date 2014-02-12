package mesosphere.marathon.event.http

import scala.language.postfixOps
import com.google.inject.{Scopes, Singleton, Provides, AbstractModule}
import akka.actor.{Props, ActorRef, ActorSystem}
import com.google.inject.name.Named
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.rogach.scallop.ScallopConf
import java.util.logging.Logger
import scala.concurrent.duration._
import akka.util.Timeout

trait HttpEventConfiguration extends ScallopConf {

  lazy val httpEventEndpoints = opt[List[String]]("http_endpoints",
    descr = "The URLs of the event endpoints master",
    required = false,
    noshort = true)
}

class HttpEventModule extends AbstractModule {

  val log = Logger.getLogger(getClass.getName)

  def configure() {
    bind(classOf[HttpCallbackEventSubscriber]).asEagerSingleton()
    bind(classOf[HttpCallbackSubscriptionService]).in(Scopes.SINGLETON)
  }

  @Provides
  @Singleton
  def provideActorSystem(): ActorSystem = {
    ActorSystem("MarathonEvents")
  }

  @Provides
  @Named(HttpEventModule.StatusUpdateActor)
  def provideActor(system: ActorSystem): ActorRef = {
    system.actorOf(Props[HttpEventActor])
  }

//  @Provides
//  @Singleton
//  def provideCallbackSubscriber(@Named(EventModule.busName) bus: Option[EventBus],
//    @Named(HttpEventModule.StatusUpdateActor) actor : ActorRef): HttpCallbackEventSubscriber = {
//    val callback = new HttpCallbackEventSubscriber(actor)
//    if (bus.nonEmpty) {
//      bus.get.register(callback)
//      log.warning("Registered HttpCallbackEventSubscriber with Bus." )
//    }
//    callback
//  }
}

object HttpEventModule {
  final val StatusUpdateActor = "EventsActor"

  val executorService = Executors.newCachedThreadPool()
  val executionContext = ExecutionContext.fromExecutorService(executorService)

  //TODO(everpeace) this should be configurable option?
  val timeout = Timeout(5 seconds)
}

