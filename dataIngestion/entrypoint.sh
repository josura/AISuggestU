#!/bin/sh
# start cron
echo "Starting crond..."

echo "00 4 * * * /app/execute.sh > /dev/stdout" > /etc/crontabs/root
/usr/sbin/crond -f -l 8