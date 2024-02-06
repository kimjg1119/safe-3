import java.io.{File, FileOutputStream, InputStream, OutputStream}
import java.net.URL

// temporal
logLevel := Level.Error

def downloadFile(from: URL, to: File): Unit = {
  val connection = from.openConnection()
  connection.setConnectTimeout(5000)
  connection.setReadTimeout(5000)
  val inStream: InputStream = connection.getInputStream
  val outStream: OutputStream = new FileOutputStream(to)

  val buffer = new Array[Byte](1024)
  var bytesRead = inStream.read(buffer)
  while (bytesRead != -1) {
    outStream.write(buffer, 0, bytesRead)
    bytesRead = inStream.read(buffer)
  }

  outStream.close()
  inStream.close()
}

def testOnlyTask(testClassNames: String*): Def.Initialize[Task[Unit]] =
  Def.task {
    (testOnly in Test).toTask(testClassNames.mkString(" ", " ", "")).value
  }

// lazy val checkCopyrights = taskKey[Unit]("Checks copyrights of source files")
lazy val buildParsers = taskKey[Unit]("Builds parsers")
lazy val deleteParserDir = taskKey[Unit]("Delete java parser directory")

// short-cuts
lazy val basicAnalyzeTest = taskKey[Unit]("Launch basic analyze tests")
lazy val benchTest = taskKey[Unit]("Launch benchmarks tests")
lazy val cfgBuildTest = taskKey[Unit]("Launch cfg build tests")
lazy val htmlTest = taskKey[Unit]("Launch html tests")
lazy val test262Test = taskKey[Unit]("Launch test262 tests")

lazy val root = (project in file(".")).settings(
  name := "SAFE",
  version := "2.0",
  organization := "kr.ac.kaist.safe",
  scalaVersion := "2.13.11",
  // checkCopyrights in Compile := {
  //   val violated: String = (baseDirectory.value + "/bin/checkCopyrights.sh" !!)
  //   if (violated != "") {
  //     throw new Error("\nFix the copyright(s) of the following:\n" + violated)
  //   }
  // },
  buildParsers in Compile := {
    // xtc
    val xtcFile = new File("./lib/xtc.jar")
    if (!xtcFile.exists) {
      // TODO exception handling: not downloaded
      downloadFile(
        new URL("https://repo1.maven.org/maven2/xtc/rats/2.4.0/rats-2.4.0.jar"),
        xtcFile
      )
    }

    // webix
    val webixJsFile = new File("./src/main/resources/assets/js/webix.js")
    val webixCssFile = new File("./src/main/resources/assets/css/webix.css")
    if (!webixJsFile.exists)
      downloadFile(new URL("http://cdn.webix.com/edge/webix.js"), webixJsFile)
    if (!webixCssFile.exists)
      downloadFile(new URL("http://cdn.webix.com/edge/webix.css"), webixCssFile)

    val options = ForkOptions().withBootJars(Seq(xtcFile).toVector)
    val srcDir = baseDirectory.value + "/src/main"
    val inDir = srcDir + "/scala/kr/ac/kaist/safe/parser/"
    val outDir = srcDir + "/java/kr/ac/kaist/safe/parser/"
    val outFile = file(outDir)
    if (!outFile.exists) IO.createDirectory(outFile)
    val arguments = Seq(
      "-in",
      srcDir + "/scala",
      "-enc-out",
      "UTF-8",
      "-out",
      outDir,
      inDir + "JS.rats"
    )
    val mainClass = "xtc.parser.Rats"
    val cache =
      FileFunction.cached(outFile, FilesInfo.lastModified, FilesInfo.exists) {
        in: Set[File] =>
          {
            Fork.java(options, mainClass +: arguments)
            Set(file(inDir + "JS.rats"))
          }
      }
    cache(file(inDir).asFile.listFiles.toSet)
  },
  compile := (compile in Compile)
    // .dependsOn(buildParsers in Compile, checkCopyrights in Compile)
    .dependsOn(buildParsers in Compile)
    .value,
  test := (testOnly in Test)
    .toTask(
      " kr.ac.kaist.safe.CFGBuildTest" +
        " kr.ac.kaist.safe.BasicAnalyzeTest" +
        " kr.ac.kaist.safe.HTMLAnalyzeTest"
    )
    .dependsOn(compile)
    .value,
  basicAnalyzeTest := testOnlyTask("kr.ac.kaist.safe.BasicAnalyzeTest")
    .dependsOn(compile)
    .value,
  benchTest := testOnlyTask("kr.ac.kaist.safe.BenchAnalyzeTest")
    .dependsOn(compile)
    .value,
  cfgBuildTest := testOnlyTask("kr.ac.kaist.safe.CFGBuildTest")
    .dependsOn(compile)
    .value,
  htmlTest := testOnlyTask("kr.ac.kaist.safe.HTMLAnalyzeTest")
    .dependsOn(compile)
    .value,
  test262Test := testOnlyTask("kr.ac.kaist.safe.Test262AnalyzeTest")
    .dependsOn(compile)
    .value
)

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions"
)

unmanagedJars in Compile ++= Seq(
  file("lib/xtc.jar"),
  file("lib/jericho-html-3.3.jar")
)
cleanFiles ++= Seq(file("src/main/java/kr/ac/kaist/safe/parser/"))

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "scala-tool",
  "org.scalatest" %% "scalatest" % "3.2.17" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  "io.spray" %% "spray-json" % "1.3.6",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.9.1",
  "org.jline" % "jline" % "3.10.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
)

scalacOptions += "-Xsource:3"
javacOptions ++= Seq("-encoding", "UTF-8")

retrieveManaged := true
