#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/srvbidrag-dokument/username;
then
    export  SRVBIDRAG_USERNAME=$(cat /var/run/secrets/nais.io/srvbidrag-dokument/username)
fi
if test -f /var/run/secrets/nais.io/srvbidrag-dokument/password;
then
    export  SRVBIDRAG_PASSWORD=$(cat /var/run/secrets/nais.io/srvbidrag-dokument/password)
fi