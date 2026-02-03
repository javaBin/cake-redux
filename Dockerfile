FROM eclipse-temurin:25-jre-jammy
ARG TARGETARCH

# Install unzip and curl
RUN apt-get update -y && \
    apt-get install -y curl unzip && \
    rm -rf /var/lib/apt/lists/*

# Install SOPS
RUN curl -L -o /usr/local/bin/sops https://github.com/getsops/sops/releases/download/v3.11.0/sops-v3.11.0.linux.$TARGETARCH && \
    chmod +x /usr/local/bin/sops

    # Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" && \
    unzip /tmp/awscliv2.zip -d /tmp && \
    /tmp/aws/install && \
    rm -rf /tmp/aws /tmp/awscliv2.zip

WORKDIR /app
COPY target/cake-redux-jar-with-dependencies.jar app.jar
COPY ./config/.enc.prod.env /app/.enc.env

# Remove AWS profile from SOPS config to use container's AWS credentials
RUN sed -i 's/sops_kms__list_0__map_aws_profile=javabin/sops_kms__list_0__map_aws_profile=/' /app/.enc.env

EXPOSE 8081
ENTRYPOINT ["sops", "exec-env", ".enc.env", "java -jar app.jar"]
