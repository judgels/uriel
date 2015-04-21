import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbtbuildinfo.Plugin._

lazy val uriel = (project in file("."))
    .enablePlugins(PlayJava, SbtWeb)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .dependsOn(frontendcommons)
    .aggregate(frontendcommons)
    .settings(
        name := "uriel",
        version := "0.2.1",
        scalaVersion := "2.11.1",
        libraryDependencies ++= Seq(
            "org.apache.poi" % "poi" % "3.10-FINAL",
            "org.webjars" % "momentjs" % "2.9.0",
            "org.webjars" % "Eonasdan-bootstrap-datetimepicker" % "4.0.0"
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
    .settings(buildInfoSettings: _*)
    .settings(
        sourceGenerators in Compile <+= buildInfo,
        buildInfoKeys := Seq[BuildInfoKey](name, version),
        buildInfoPackage := "org.iatoki.judgels.uriel"
    )

lazy val frontendcommons = RootProject(file("../judgels-frontend-commons"))
