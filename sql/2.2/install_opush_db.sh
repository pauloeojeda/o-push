#!/bin/su postgres

test $# -eq 3 || {
    echo "usage: $0 db user password"
    exit 1
}

db=$1
user=$2
pw=$3


echo "  Delete old database"
dropdb ${db} || {
    echo "[ERROR] could not delete database, all connections are close ?"
    exit 1
}


dropuser ${user}

echo "Creating role '${user}' (pw: ${pw}) & db '${db}'..."
createuser --createdb --no-superuser --no-createrole --login ${user}

psql template1 <<EOF
ALTER USER ${user} WITH PASSWORD '${pw}'
\q
EOF

echo "  Create new $DB database"

createdb -O ${user} --encoding=UTF-8 ${db}

psql ${db} <<EOF
CREATE LANGUAGE plpgsql;
ALTER DATABASE ${db} SET TIMEZONE='GMT';
\q
EOF

psql -U ${user} ${db} -f \
create_opush_2.2.sql > /tmp/opush.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "DONE."
