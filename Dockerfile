FROM govukverify/java8:latest

RUN apt-get update \
    && apt-get install -y python-pip build-essential maven sudo \
    && curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash - \
    && apt-get install -y nodejs

RUN pip install virtualenv

RUN groupadd -g 116 team
RUN useradd -r -u 109 -g 116 user
WORKDIR /home/user
RUN chown -R user:116 /home/user
RUN chmod 755 /home/user
USER user
