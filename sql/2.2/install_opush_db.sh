#!/bin/su postgres

test $# -eq 2 || {
    echo "usage: $0 db user"
    exit 1
}

db=$1
user=$2


psql -U ${user} ${db} -f \
create_opush_2.2.sql > /tmp/opush.log 2>&1
grep -i error /tmp/opush.log && {
    echo "error in pg script"
    exit 1
}

echo "DONE."
