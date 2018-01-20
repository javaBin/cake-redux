#!/usr/bin/env bash

BASEDIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

# resolve symlinks
while [ -h "$BASEDIR/$0" ]; do
    DIR=$(dirname -- "$BASEDIR/$0")
    SYM=$(readlink $BASEDIR/$0)
    BASEDIR=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)
done
cd ${BASEDIR}

beanstalk_env=${1}
app=cakeredux
envs=$(eb list | sed 's/^\* //')

if [[ ${beanstalk_env} != ${app}-* ]]; then
  echo "Usage: ${0} ${app}-<environment>"
  echo
  echo "Available environments:"
  echo "${envs}"
  exit 1
elif [ $(echo "${envs}" | grep "^${beanstalk_env}$" -c) -eq 0 ]; then
  echo "Environment not recognized: '${beanstalk_env}'. Use one of the following:"
  echo
  echo "${envs}"
  exit 1
fi

local_version=$( grep -E "<version>[0-9]+(\.[0-9]+).*(SNAPSHOT)?</version>" pom.xml -m1 2> /dev/null | sed 's/.*<version>\(.*\)<\/version>/\1/' )

version_suggestion="[${local_version}] "
read -p "Version? ${version_suggestion}" version
[ -z ${version} ] && version="${local_version}"

env=$(echo ${beanstalk_env} | sed s/${app}-//g)

trap "{ rm -f app.zip app.jar ${secret_properties_file} ${authorization_file} ; exit 255; }" EXIT

./package.sh ${env} ${version}
if [ $? -ne 0 ]; then
  echo "> Package failed!"
  exit 1
fi

if [ "${2}" == "debug" ]; then
  echo "Debug mode. Skipping deploy."
  rm -f app.zip
  exit 0
fi

echo "> Deploying sleepingPillCore to ${beanstalk_env}"
eb deploy "${beanstalk_env}"

echo "> Deleting app.zip"
rm -f app.zip

echo "> Deploy complete.'"
exit 0
