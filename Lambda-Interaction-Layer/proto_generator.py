from grpc_tools import protoc
import os

# Path to your .proto file
proto_file = "bedrock_service.proto"

# Output directory for generated files
output_dir = "."

# Construct the `protoc` command
command = [
    "grpc_tools.protoc",
    f"--proto_path={os.path.dirname(proto_file)}",
    f"--python_out={output_dir}",
    f"--grpc_python_out={output_dir}",
    proto_file,
]

# Run the command
if protoc.main(command) != 0:
    print("Failed to generate gRPC code.")
else:
    print("gRPC code generated successfully!")
