ThisBuild / scalaVersion := "2.13.18"

lazy val docs = project.in(file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    name := "scotty-docs",
    publish / skip := true,
    libraryDependencies += "xyz.entangled" %% "scotty" % "0.6.0",
    mdocIn := baseDirectory.value / ".." / "docs" / "src",
    mdocOut := baseDirectory.value / ".." / "docs" / "generated"
  )
