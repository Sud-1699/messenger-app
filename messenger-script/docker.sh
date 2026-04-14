if [ -d "$HOME/postgres" ]; then
    mkdir -p ~/docker/postgres3/data
fi

docker compose -f ../messenger-app/docker-compose.yaml up -d

rc=$?
if [ $rc -ne 0 ] ; then
  echo Could not spinup postgres docker container, exit code [$rc]; exit $rc
fi

SQL_DDL_FILE_PATH="../messenger-app/messenger-server/src/main/resources/setup.sql"
export PGPASSWORD=Messenger@Admin1
psql postgresql://localhost:5435/messenger_admin -U postgres -f "$SQL_DDL_FILE_PATH"

rc=$?
if [ $rc -ne 0 ] ; then
  echo Could not setup database ddl, exit code [$rc]; exit $rc
fi