# Build
mvn clean compile assembly:single

# Run
java -jar test-smb-0.0.1-SNAPSHOT-jar-with-dependencies  host domain user share [password]