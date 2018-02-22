FROM govukverify/java8:latest

RUN apt-get update \
    && apt-get install -y python-pip build-essential maven sudo \
    && curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash - \
    && apt-get install -y nodejs

RUN pip install virtualenv
RUN ls -al
RUN find . -name "gradlew" -type f
RUN ./gradlew build