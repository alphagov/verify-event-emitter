FROM govukverify/java8:latest

RUN apt-get update \
    && apt-get install -y python-pip build-essential maven sudo \
    && curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash - \
    && apt-get install -y nodejs \
    && pip install virtualenv

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
