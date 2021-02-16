#!/bin/bash

python3 -m dnslib.fixedresolver -r "_acme-challenge.$CERTBOT_DOMAIN. 10 IN TXT \"$CERTBOT_VALIDATION\"" -p 53 &>/dev/null &disown
sleep 5
