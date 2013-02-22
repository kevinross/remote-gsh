./build.sh
rm -rf release
mkdir release
mkdir release/lib

SERVER_JAR=`ls remote-gsh-server/target | grep "remote-gsh-server" | grep -v -e "sources.jar$"`
cp remote-gsh-server/target/$SERVER_JAR release/
mvn -f remote-gsh-server/pom.xml dependency:copy-dependencies -DincludeScope=compile -DexcludeScope=provided -DoutputDirectory=../release/lib
