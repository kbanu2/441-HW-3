import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes
import bedrock.bedrock_service.{BedrockServiceGrpc, GenerateTextRequest, GenerateTextResponse}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import spray.json._
import scala.concurrent.Future

case class Prompt(prompt: String)

object JsonFormats extends DefaultJsonProtocol {
  implicit val promptFormat: RootJsonFormat[Prompt] = jsonFormat1(Prompt)
}

class MainAppTest extends AnyFlatSpec with ScalatestRouteTest with Matchers {

  // Mocked version of the Python gRPC client to simulate interactions with the Python server
  class MockGrpcClient extends BedrockServiceGrpc.BedrockService {
    override def generateText(request: GenerateTextRequest): Future[GenerateTextResponse] = {
      // Mock response based on the prompt
      Future.successful(GenerateTextResponse(s"Generated text for: ${request.prompt}"))
    }
  }

  // Create instance of MainApp with mocked gRPC client
  val mockGrpcClient = new MockGrpcClient()
  val app = new MainApp(mockGrpcClient)

  // Route definition (same as in your application, in the MainApp object)
  val route = app.route

  "The REST API" should "return a generated text response" in {
    // Mock request body as JSON
    val requestBody = """{"prompt": "Hello, world!"}"""

    // Send a POST request to the route
    Post("/generate-text", requestBody) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] should include("Generated text for: Hello, world!")
    }
  }

  it should "return a 200 OK response if the prompt is empty" in {
    val requestBody = """{"prompt": ""}"""

    Post("/generate-text", requestBody) ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "handle invalid JSON input gracefully" in {
    val invalidJson = """{prompt: Hello}"""  // Invalid JSON (missing quotes around "prompt")

    Post("/generate-text", invalidJson) ~> route ~> check {
      status shouldEqual StatusCodes.BadRequest
    }
  }

  it should "return a 500 Internal Server Error if the gRPC server fails" in {
    // Simulate gRPC failure by throwing an exception
    val mockFailingGrpcClient = new MockGrpcClient {
      override def generateText(request: GenerateTextRequest): Future[GenerateTextResponse] = {
        Future.failed(new RuntimeException("gRPC server error"))
      }
    }

    val appWithFailingGrpcClient = new MainApp(mockFailingGrpcClient)

    val requestBody = """{"prompt": "Hello, world!"}"""

    Post("/generate-text", requestBody) ~> appWithFailingGrpcClient.route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  it should "return a 404 Not Found for non-existent routes" in {
    val requestBody = """{"prompt": "Test"}"""

    // Trying a route that doesn't exist
    Post("/asdf", requestBody) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }
}
