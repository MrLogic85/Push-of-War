import AssemblyKeys._ // put this at the top of the file

assemblySettings

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.2.0"
 
libraryDependencies +=
  "com.typesafe.akka" %% "akka-remote" % "2.2.0"