#!/bin/bash

test $# -eq 3 || {
    echo "usage: $0 db user host"
    exit 1
}

db=$1
user=$2
host=$3


mysql -h ${host} -u ${user} -p ${db} < \
create_opush_mysql.sql > /tmp/opush.log 2>&1
grep -i error /tmp/opush.log && {
    echo "error in mysql script, look at /tmp/opush.log"
    exit 1
}

echo "DONE."
