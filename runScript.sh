export JAVA_TOOL_OPTIONS=
./scala-2.12.13/bin/scala -J-Xmx50m -J-Xms20m -J-Xss512k -J-Djava.security.manager -J-Djava.security.policy=mypolicy $1
