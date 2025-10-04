#!/bin/sh
# Usage: ./run-local.sh [path_prefix]
# Example: ./run-local.sh moresleep

PATH_PREFIX="$1"

echo "Setting PATH_PREFIX to: '$PATH_PREFIX'"

# Build the Docker image
docker build -t cake-app:latest .

# Run the container with AWS credentials mounted
docker run --rm -it \
  -p 8081:8081 \
  -v "$HOME/.aws:/root/.aws:ro" \
  -e AWS_PROFILE=javabin \
  -e PATH_PREFIX="$PATH_PREFIX" \
  cake-app:latest