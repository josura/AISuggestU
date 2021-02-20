#!/bin/bash
if [ "$#" -lt 1 ]; then
    echo "Use: $0 mod"
    exit 1
fi

readonly mod=$1

cd /app/

if [ "$mod" == "daily" ]; then
    python data-parser.py daily
elif [ "$mod" == "default" ]; then
    if [ -z "$2" ]; then
        python data-parser.py default 5 
    else
        python data-parser.py default "$2"
    fi
else
    echo "Unkown mod"
    exit 1
fi
