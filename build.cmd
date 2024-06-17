echo "Use 'mvn package -DskipTests' when you want to skip tests"

REM mvn dependency:copy-dependencies && ^
REM mvn dependency:copy-dependencies -DoutputDirectory=${project.build.directory}/classes/lib -DincludeScope=compile -DexcludeScope=test -DexcludeGroupIds=com.github.spotbugs && ^
REM mvn dependency:copy-dependencies -DoutputDirectory=${project.build.directory}/lib -DincludeScope=compile -DexcludeScope=test -DexcludeGroupIds=com.github.spotbugs && ^

mvn -version && ^
mvn clean && ^
mvn package && ^
mvn compile assembly:single
