#!/bin/bash

BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

# resolve symlinks
while [ -h "$BASEDIR/$0" ]; do
    DIR=$(dirname -- "$BASEDIR/$0")
    SYM=$(readlink $BASEDIR/$0)
    BASEDIR=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)
done
cd ${BASEDIR}

# --------------------------------------

set -e

yellow() { echo -e "\033[33m$1\033[0m"; }
green() { echo -e "\033[32m$1\033[0m"; }
red() { echo -e "\033[31m$1\033[0m"; }
bold() { echo -e "\033[1;37m$1\033[0m"; }
_fancymessage() {
  echo ""
  green "\033[32m--> $1 \033[0m"
}

info() { bold "$1"; }
ask() { _fancymessage "$1"; }
fail() { red "$1"; exit 1; }

# read -i funker ikke på OSX - derav mer kronglete løsning :(
_readWithDefault() {
    local default=$1
    read answer
    if [ "$answer" = "" ]; then
         answer="$default"
    fi
    echo $answer
}

info "BYGGER"
mvn clean install

yellow ""
yellow "  deployer cakeredux"
yellow ""

DEFAULT_JAR=`find . -name *jar-with-dependencies.jar`
DEFAULT_WAR=`find . -name *.war`
DEFAULT_VERSION=`date +%Y%m%d%H%M%S`-SNAPSHOT

ask "Hvor ligger jar-filen? [$DEFAULT_JAR]"
JAR=$(_readWithDefault $DEFAULT_JAR)

ask "Hvor ligger war-filen? [$DEFAULT_WAR]"
WAR=$(_readWithDefault $DEFAULT_WAR)

ask "Hvilken versjon? [$DEFAULT_VERSION]"
VERSION=$(_readWithDefault $DEFAULT_VERSION)

ask "Til test eller prod? [test]"
ENV=$(_readWithDefault "test")

if [ ! -f $JAR ]; then
	fail "Fant ikke $JAR :("
fi

if [ $ENV != "test" -a $ENV != "prod" ]; then
	fail "Miljø må være enten 'test' eller 'prod'"
fi

if [ $ENV == "prod" ]; then
	HOST="2014.javazone.no"
	BASE="/home/javabin/web/cakeredux"
elif [ $ENV == "test" ]; then
	HOST="test.2014.javazone.no"
	BASE="/home/javabin/web/cakeredux"
else
	fail "Det du sa gav null mening!"
fi

info "Deployer til $EVN på $HOST:$BASE med versjon $VERSION med jar $JAR og war $WAR"

ssh javabin@$HOST "mkdir -p $BASE/$VERSION"
info "Laster opp jar"
scp $JAR javabin@$HOST:$BASE/$VERSION/cakeredux.jar
info "Laster opp war"
scp $WAR javabin@$HOST:$BASE/$VERSION/cakeredux.war
ssh javabin@$HOST "ln -s -f $VERSION -T $BASE/current"
ssh javabin@$HOST "$BASE/cakeredux.sh stop"
ssh javabin@$HOST "$BASE/cakeredux.sh start"
