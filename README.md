# content-connector-openaire

### Running service:
To update the docker service in dev:\
`docker service update --network-add stack_esnet --env-add registry.host=https://dev.openminted.eu --env-add jms.host=tcp://jms:61616 --env-add redis.host=redis --force --image docker.openminted.eu/omtd-content-service --with-registry-auth omtd-content-service`

To update the docker service in services:\
`docker service update --network-add stack_esnet --env-add registry.host=https://services.openminted.eu --env-add jms.host=tcp://jms:61616 --env-add redis.host=redis --force --image docker.openminted.eu/omtd-content-service --with-registry-auth omtd-content-service`