FROM govukverify/java8:latest


RUN apt-get update \
    && apt-get install -y  build-essential maven sudo

WORKDIR /home/user
RUN groupadd -g 116 team;\
    useradd -r -u 109 -g 116 user;\
    chown -R user:116 /home/user;\
    chmod 755 /home/user

USER user

COPY gradle gradle
COPY gradlew gradlew
RUN ./gradlew --no-daemon

COPY build.gradle build.gradle
RUN ./gradlew --no-daemon resolveDependencies

COPY . ./
