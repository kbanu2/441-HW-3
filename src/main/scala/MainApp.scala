import JsonFormats.promptFormat
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, RejectionHandler, Route}
import bedrock.bedrock_service.{BedrockServiceGrpc, GenerateTextRequest, GenerateTextResponse}
import io.grpc.ManagedChannelBuilder
import spray.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

// Define JSON formats for parsing request body
object JsonFormats extends DefaultJsonProtocol {
  implicit val promptFormat: RootJsonFormat[Prompt] = jsonFormat1(Prompt)
}

case class Prompt(prompt: String)

class MainApp(grpcClient: BedrockServiceGrpc.BedrockService) extends Directives {

  // Custom rejection handler
  val rejectionHandler: RejectionHandler = RejectionHandler.newBuilder()
    .handleNotFound {
      complete(StatusCodes.NotFound, """{"error": "Endpoint not found"}""")
    }
    .result()

  // Define the route using the gRPC client passed in
  def route: Route =
    handleRejections(rejectionHandler) { // Apply rejection handler here
      path("generate-text") {
        post {
          entity(as[String]) { requestBody =>
            try {
              val prompt = requestBody.parseJson.convertTo[Prompt].prompt

              // Call the Python gRPC server
              val grpcRequest = GenerateTextRequest(prompt)
              val grpcResponseFuture: Future[GenerateTextResponse] = grpcClient.generateText(grpcRequest)

              // Convert the gRPC response to JSON and return
              onSuccess(grpcResponseFuture) { grpcResponse =>
                complete(StatusCodes.OK, s"""{"generated_text": "${grpcResponse.generatedText}"}""")
              }
            } catch {
              case ex: Exception =>
                complete(StatusCodes.BadRequest, s"""{"error": "Invalid JSON format. Error: ${ex.getMessage}"}""")
            }
          }
        }
      }
    }
}

object MainApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("rest-grpc-system")
    implicit val ec: ExecutionContext = system.dispatcher

    // Create a gRPC channel to the Python gRPC server
    val channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build()
    val grpcClient = BedrockServiceGrpc.stub(channel)

    // Create the MainApp instance, passing the gRPC client
    val app = new MainApp(grpcClient)

    // Start the HTTP server
    val bindingFuture = Http().newServerAt("localhost", 5555).bind(app.route)

    println("REST API running at http://localhost:5555/")
    println("Press RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => {
      println("Shutting down gRPC channel")
      channel.shutdownNow() // Shutdown the gRPC channel when the server stops
      system.terminate()
    })
  }
}
