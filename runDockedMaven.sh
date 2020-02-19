#!/bin/bash
set -e

docker run -it --rm -v $PWD:/usr/src/mymaven -v ~/.m2:/root/.m2 -w /usr/src/mymaven maven:3.6.3-jdk-13 mvn