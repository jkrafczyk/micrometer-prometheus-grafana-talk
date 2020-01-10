#!/usr/bin/env bash

if [[ -z "$URL" ]]
then
    URL=http://127.0.0.1:8080
fi

for i in $(seq 30)
do
    curl -v -XPOST "$URL/leak"
done