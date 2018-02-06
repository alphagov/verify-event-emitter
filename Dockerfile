FROM govukverify/java8:latest
# Be careful not to couple this file with the base image
# in case of upstream changes - in fact, you should prefer not to 
# edit this dockerfile if possible.
ENTRYPOINT ["./gradlew", "--daemon"]
CMD ["test"]

