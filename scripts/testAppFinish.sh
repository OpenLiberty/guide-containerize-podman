#!/bin/bash
set -euxo pipefail

##############################################################################
##
##  Travis CI test script
##
##############################################################################

cd ../finish

mvn -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -q clean package
    
podman pull openliberty/open-liberty:kernel-java8-openj9-ubi

podman build -t system system/.
podman build -t inventory inventory/.

podman images -f "label=org.opencontainers.image.authors=Your Name" | grep system
podman images -f "label=org.opencontainers.image.authors=Your Name" | grep inventory

podman run -d --name system -p 9080:9080 system
podman run -d --name inventory -p 9081:9081 inventory

sleep 120

systemStatus="$(curl --write-out "%{http_code}\n" --silent --output /dev/null "http://localhost:9080/system/properties/")"
inventoryStatus="$(curl --write-out "%{http_code}\n" --silent --output /dev/null "http://localhost:9081/inventory/systems/")"

if [ "$systemStatus" == "200" ] && [ "$inventoryStatus" == "200" ]
then
  echo ENDPOINT OK
else
  echo inventory status:
  echo "$inventoryStatus"
  echo system status:
  echo "$systemStatus"
  echo ENDPOINT
  exit 1
fi

podman stop inventory system
podman rm inventory system
