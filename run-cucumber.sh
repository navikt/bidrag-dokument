#!/bin/bash
set -e

if [ -z "$INPUT_MAVEN_IMAGE" ]
then
  >&2 echo "::error No docker image for maven is given..."
  exit 1;
fi

if [ -z "$INPUT_CUCUMBER_TAG" ]
then
  >&2 echo "::error No tag for running of cucumber tests is given..."
  exit 1;
fi

if [ -z "$INPUT_ENVIRONMENT" ]
then
  >&2 echo "::error No environment where to run cucumber tests is given..."
  exit 1;
fi

if [ ! -d bidrag-cucumber-backend ]
then
  git clone https://github.com/navikt/bidrag-cucumber-backend
fi

cd bidrag-cucumber-backend
git pull

if [ -z "$INPUT_ADDITIONAL_CUCUMBER_OPTIONS" ]
then
  CUCUMBER_OPTIONS="'-- tags \"$INPUT_CUCUMBER_TAG\", $INPUT_ADDITIONAL_CUCUMBER_OPTIONS"
else
  CUCUMBER_OPTIONS="'-- tags \"$INPUT_CUCUMBER_TAG\""
fi

docker run -it --rm -v $PWD:/usr/src/mymaven -v ~/.m2:/root/.m2 -w /usr/src/mymaven "$INPUT_MAVEN_IMAGE" mvn clean test \
  -Dcucumber.options="$CUCUMBER_OPTIONS" \
  -DENVIRONMENT="$INPUT_ENVIRONMENT" \
  "$MAVEN_CUCUMBER_CREDENTIALS"
