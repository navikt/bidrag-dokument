#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/srvbisys/username;
then
    export  SRVBISYS_USERNAME=$(cat /var/run/secrets/nais.io/srvbisys/username)
fi
if test -f /var/run/secrets/nais.io/srvbisys/password;
then
    export  SRVBISYS_PASSWORD=$(cat /var/run/secrets/nais.io/srvbisys/password)
fi