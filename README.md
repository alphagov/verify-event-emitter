# Event Emitter

>**GOV.UK Verify has closed**
>
>This repository is out of date and has been archived

### Overview

Event Emitter is a library providing a Java Service to log Audit Events to a datastore.
The Java Service sends the Audit Events to AWS via one of two mechanisms:

1. An API Gateway API.
2. An SQS Queue.

Use of the library assumes that a corresponding implementation of the required AWS resources is in place.

Applications consuming the library should provide an implementation of the `uk.gov.ida.eventemitter.Configuration` class and provide configuration values.
An `apiGatewayUrl` must be provided when connecting via API Gateway, and a `sourceQueueName` provided when connecting via SQS Queue, 
as well as a concrete implementation of an `Event` or `AuditEvent`.

The library provides a Guice Module `EventEmitterModule` to get hold of the Service objects.
Use an `EventEmitter` to send events to the API Gateway API.  Use an `EventEmitterSQS` to send events to an SQS Queue.

### Building the project

`./gradlew clean build`

### Pre-Commit Script

In order to run the integration tests you will need to install `python`, `pip` and `docker` on your dev machine.

## Licence

[MIT Licence](LICENCE)

This code is provided for informational purposes only and is not yet intended for use outside GOV.UK Verify.
