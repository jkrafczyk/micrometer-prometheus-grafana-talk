#!/usr/bin/env bash

NAMES=(
    Carl
    Bob
    Alice
    Vardan
    Shoushanik
    Nicole
    Yorgos
    Shion
    Rei
    Konstantinos
    Li
    Na
    Guiying
    )

if [[ -z "$URL" ]]
then
    URL=http://127.0.0.1:8080
fi

function doLeak() {
    curl -XPOST "$URL/leak" 
}

function doSlow() {
    curl -XGET "$URL/slow"
}

function doGreet() {
    local size=${#NAMES[@]}
    local index=$(($RANDOM % $size))
    local name="${NAMES[$index]}"
    curl -G "$URL/greet" -d "name=$name"
}

index=0
while true
do
    if [[ "$index % 30" -eq "0" ]]
    then
        (doLeak
        printf "\n")&
    fi
    if [[ "$index % 3" -eq "0" ]]
    then
        (doGreet
        printf "\n")&
    fi

    (doSlow
    printf "\n")&

    index=$(( $index + 1))
    sleep 3
    wait
done