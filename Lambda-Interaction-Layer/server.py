import grpc
from concurrent import futures
import bedrock_service_pb2_grpc
import bedrock_service_pb2
import boto3
import json
import os


# Define the gRPC server implementation
class BedrockServiceServicer(bedrock_service_pb2_grpc.BedrockServiceServicer):
    def __init__(self):
        # Initialize the AWS Lambda client using the default boto3 credentials
        self.lambda_client = boto3.client(
            'lambda',
            region_name='us-east-1'
        )

    def GenerateText(self, request, context):
        try:
            # Extract prompt from the request
            prompt = request.prompt
            print(f"Received prompt: {prompt}")

            # Prepare the payload for Lambda
            lambda_payload = {
                "prompt": prompt
            }

            # Invoke the Lambda function
            response = self.lambda_client.invoke(
                FunctionName="bedrock_query_lambda",
                InvocationType="RequestResponse",
                Payload=json.dumps(lambda_payload)
            )

            # Parse the Lambda response
            response_payload = json.loads(response['Payload'].read())
            generated_text = json.loads(response_payload['body']).get('generated_text', '')

            return bedrock_service_pb2.GenerateTextResponse(generated_text=generated_text)

        except Exception as e:
            context.set_details(f"Error: {str(e)}")
            context.set_code(grpc.StatusCode.INTERNAL)
            return bedrock_service_pb2.GenerateTextResponse()


# Main server function
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    bedrock_service_pb2_grpc.add_BedrockServiceServicer_to_server(BedrockServiceServicer(), server)
    server.add_insecure_port('[::]:50052')
    print("gRPC server running on port 50052...")
    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    os.environ['AWS_SHARED_CREDENTIALS_FILE'] = '.aws/credentials'
    session = boto3.Session()
    credentials = session.get_credentials()

    aws_access_key_id = credentials.access_key
    aws_secret_access_key = credentials.secret_key

    if aws_access_key_id and aws_secret_access_key:
        print("Using environment variables for AWS credentials.")
    else:
        print("No environment AWS credentials found. Using default AWS profile or IAM roles.")

    serve()
