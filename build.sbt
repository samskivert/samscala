seq(samskivert.POMUtil.pomToSettings("pom.xml") :_*)

crossPaths := false

autoScalaLibrary := false // we get scala-library from the POM

scalacOptions += "-optimize" // TODO: extract from POM
