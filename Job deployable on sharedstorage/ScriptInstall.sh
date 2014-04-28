echo "CLONE DIRECTORY WATCHER AND WIPERDOG FROM GIT"
git clone https://github.com/WiperDogLabo/MonitorJobsDeployable.git

echo ">> INSTALL DIRECTORY WATCHER <<"
cd MonitorJobsDeployable/org.wiperdog.directorywatcher/
mvn install
echo "DIRECTORY WATCHER WAS INSTALLED !!!"

echo ">>> INSTALL WIPERDOG <<<"
cd ..
cd wiperdogInstaller/
mvn install
echo "WIPERDOG WAS BUILD !!!"

cd target/
java -jar wiperdog-0.2.6-SNAPSHOT-unix.jar -d wiperdogTest
echo "WIPERDOG WAS INSTALLED !!!"

echo "============= FINAL, RUN WIPERDOG ============="
cd wiperdogTest/bin

./startWiperdog.sh