seq(samskivert.POMUtil.pomToSettings("pom.xml") :_*)

crossPaths := false

autoScalaLibrary := false // we get scala-library from the POM

scalacOptions += "-optimize" // TODO: extract from POM

// allows SBT to run junit tests
libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"
