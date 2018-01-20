#!/usr/bin/env bash

mvn release:prepare -Darguments="-DskipTests" && mvn release:perform -Darguments="-DskipTests"
