clojure -Spom
clj -X:deployer:dev:generate-pom > p.xml
mv p.xml pom.xml

clj -X:deployer:dev:build

clj -X:deployer:dev:deploy-clojars | sh

rm pom.xml
