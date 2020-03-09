#!/bin/bash
# This script will build the project.

CLEANED_TAG=`echo $TRAVIS_TAG | xargs`

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo -e "Build Pull Request #$TRAVIS_PULL_REQUEST => Branch [$TRAVIS_BRANCH]"
  ./gradlew build
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$CLEANED_TAG" == "" ]; then
  echo -e 'Build Branch without Snapshot => Branch ['$TRAVIS_BRANCH']'
  ./gradlew -Prelease.travisci=true build --info
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$CLEANED_TAG" != "" ]; then
  echo -e 'Build Branch for Release => Branch ['$TRAVIS_BRANCH']  Tag ['$CLEANED_TAG']'
  case "$CLEANED_TAG" in
  *-rc\.*)
    ./gradlew -Prelease.travisci=true -Prelease.useLastTag=true candidate publishNebulaPublicationToBintrayRepository --info
    ;;
  *)
    ./gradlew -Prelease.travisci=true -Prelease.useLastTag=true final publishNebulaPublicationToBintrayRepository --info
    ;;
  esac
else
  echo -e 'WARN: Should not be here => Branch ['$TRAVIS_BRANCH']  Tag ['$CLEANED_TAG']  Pull Request ['$TRAVIS_PULL_REQUEST']'
  ./gradlew build
fi
