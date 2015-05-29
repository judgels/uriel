import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbtbuildinfo.Plugin._

lazy val uriel = (project in file("."))
    .enablePlugins(PlayJava, SbtWeb)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .dependsOn(urielcommons, sandalphoncommons, jophielcommons)
    .aggregate(urielcommons, sandalphoncommons, jophielcommons)
    .settings(
        name := "uriel",
        version := IO.read(file("version.properties")).trim,
        scalaVersion := "2.11.1",
        libraryDependencies ++= Seq(
            "org.apache.poi" % "poi" % "3.10-FINAL"
        )
    )
    .settings(TestNGPlugin.testNGSettings: _*)
    .settings(
        aggregate in test := false,
        aggregate in jacoco.cover := false,
        TestNGPlugin.testNGSuites := Seq("test/resources/testng.xml")
    )
    .settings(jacoco.settings: _*)
    .settings(
        parallelExecution in jacoco.Config := false
    )
    .settings(
        LessKeys.compress := true,
        LessKeys.optimization := 3,
        LessKeys.verbose := true
    )
    .settings(
        publishArtifact in (Compile, packageDoc) := false,
        publishArtifact in packageDoc := false,
        sources in (Compile,doc) := Seq.empty
    )
    .settings(buildInfoSettings: _*)
    .settings(
        sourceGenerators in Compile <+= buildInfo,
        buildInfoKeys := Seq[BuildInfoKey](name, version),
        buildInfoPackage := "org.iatoki.judgels.uriel"
    )

lazy val urielcommons = RootProject(file("../judgels-uriel-commons"))
lazy val sandalphoncommons = RootProject(file("../judgels-sandalphon-commons"))
lazy val jophielcommons = RootProject(file("../judgels-jophiel-commons"))
