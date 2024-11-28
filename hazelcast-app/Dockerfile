# Build Stage

FROM registry.fedoraproject.org/fedora:latest AS BuildStage

RUN dnf --setopt=install_weak_deps=False install -y java-21-openjdk.x86_64 java-21-openjdk-devel.x86_64 maven.noarch

WORKDIR /hazelcast-app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

# Deploy Stage

FROM registry.fedoraproject.org/fedora:latest

RUN dnf --setopt=install_weak_deps=False install -y java-21-openjdk.x86_64

WORKDIR /app

COPY --from=BuildStage /hazelcast-app/target/hazelcast-app-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "hazelcast-app-0.0.1-SNAPSHOT.jar"]