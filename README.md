https://youtu.be/1-3o7o350Zo

# REST API with Scala & gRPC Integration to AWS Lambda (Bedrock Model)
This project sets up a RESTful API in Scala that communicates with a Python-based gRPC server, which in turn interacts with an AWS Lambda function that queries the Bedrock model for generating text. Below is a guide on how to use and configure the project.

# Overview
## Components of the Project
### Scala RESTful API:

The Scala-based RESTful API is built using Akka HTTP.
It exposes a single endpoint /generate-text that accepts a POST request with a JSON body containing a prompt.
This API communicates with a Python gRPC server to process the request and return a response.
### Python gRPC Server:

This server is built using the grpc Python library and defines a BedrockService that listens for incoming gRPC requests.
When a request is received, it calls an AWS Lambda function to query the Bedrock model (via Amazon's Bedrock service) and retrieves the generated text.
The Python gRPC server is responsible for making the call to Lambda and returning the result back to the Scala API.

### AWS Lambda Function:

The Lambda function processes the incoming prompt, queries the Bedrock model, and returns the generated text.
The Lambda function uses AWS SDKs and APIs to interact with the Bedrock model.
# How the Components Interact
### Lambda Function & Bedrock Model:

The Lambda function is the core part of this architecture. It takes a prompt (text input), sends it to the Bedrock model, and returns a generated text response.
The Lambda function is invoked by the Python gRPC server.
### Python gRPC Server & Lambda:

When the Python gRPC server receives a GenerateText request, it extracts the prompt, formats it as a payload, and sends it to the Lambda function via the AWS SDK (boto3).
The Lambda function processes the prompt and sends back the generated text.
The Python gRPC server then returns this generated text as a GenerateTextResponse to the Scala REST API.
### Scala REST API & Python gRPC Server:

The Scala REST API listens for requests on the /generate-text endpoint.
When a request is made, the Scala API sends a gRPC request to the Python server using the BedrockService's GenerateText method.
The Python server processes this request and returns the generated text, which is then returned by the Scala API to the client.
# How to Use the Project
## Prerequisites
### Before running the project, ensure that you have the following installed:

Scala & SBT: To run the Scala-based REST API.
Python 3.x: To run the Python gRPC server.
gRPC Libraries: Both Python and Scala projects require the gRPC library to function correctly.
AWS CLI & AWS Credentials: To interact with AWS services. Ensure your AWS credentials are set up in your ~/.aws/credentials file.
# Setup the Python Server
### Install Dependencies:

### Install the required Python libraries:
pip install grpcio grpcio-tools boto3
Set Up AWS Credentials:

Make sure your AWS credentials are available in the ~/.aws/credentials file. This file should contain your AWS access_key_id and secret_access_key:
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
Running the Python gRPC Server:

Once everything is set up, run the Python server:
python server.py
This will start the gRPC server on port 50052.
### Lambda Function:

The Lambda function should be configured on AWS. It should query the Bedrock model for text generation using the prompt provided in the payload.
Ensure the function name in the Python code matches the one deployed in AWS.
Setup the Scala RESTful API
# Install Dependencies:

### Make sure you have the following installed:
Scala: Version 2.13 or later.
SBT: Scala Build Tool for managing project dependencies and running your project.
Running the Scala REST API:

### Navigate to the Scala project directory and use SBT to run the application:
sbt run
The REST API will be available at http://localhost:5555.
### Testing the API:

To test the /generate-text endpoint, send a POST request with a JSON body containing the prompt. You can use a tool like curl, Postman, or any HTTP client to do this:
curl -X POST http://localhost:5555/generate-text -d '{"prompt": "Hello, world!"}' -H "Content-Type: application/json"
How to Test the Scala REST API
There are test cases implemented for the Scala REST API using the Akka HTTP testkit. You can run the tests to ensure the application behaves as expected.

### Running Tests:
Use sbt test to run the test cases:
sbt test
Test Scenarios:

Valid Request: Send a valid JSON prompt to /generate-text and verify that the response contains the generated text.
Invalid JSON: Send an invalid JSON request and verify that the API responds with a 400 Bad Request error.
Invalid Endpoint: Test accessing an invalid endpoint and ensure that the API responds with a 404 Not Found error.
Notes
AWS Lambda:

The Lambda function is responsible for interacting with the Bedrock model and generating text based on the provided prompt. You need to configure the Lambda function with the correct permissions to access the Bedrock model.
Secrets Management:

AWS credentials are required for the Python gRPC server to invoke Lambda. Ensure that these credentials are stored securely and never hardcoded in the code. The credentials can be provided via environment variables, or via the ~/.aws/credentials file.
Running in Production:

For production environments, consider using AWS IAM roles for more secure management of credentials. You can use the boto3 library’s default credentials chain (which includes IAM roles if running in AWS infrastructure) for better security.
