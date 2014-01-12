import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyFrameworkBuild extends Build {

  val Organization = "org.skinny-framework"
  val Version = "0.9.24"
  val ScalatraVersion = "2.2.2"
  val Json4SVersion = "3.2.6"
  val ScalikeJDBCVersion = "1.7.3"
  val ScalateVeresion = "1.6.1"
  val H2Version = "1.3.174"

  // In some cases, Jety 9.1 looks very slow (didn't investigate the reason)
  //val JettyVersion = "9.1.0.v20131115"
  val JettyVersion = "9.0.7.v20131107"

  lazy val baseSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version := Version,
    resolvers ++= Seq(
      "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
      "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
    ),
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishMavenStyle := true,
    sbtPlugin := false,
    scalacOptions ++= _scalacOptions,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    pomExtra := _pomExtra
  )

  lazy val common = Project (id = "common", base = file("common"),
   settings = baseSettings ++ Seq(
      name := "skinny-common",
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "com.typesafe" %  "config" % "1.0.2" % "compile"
      ) ++ servletApiDependencies 
        ++ slf4jApiDependencies 
        ++ jodaDependencies 
        ++ testDependencies
    ) ++ _jettyOrbitHack
  ) 

  lazy val framework = Project (id = "framework", base = file("framework"),
   settings = baseSettings ++ Seq(
      name := "skinny-framework",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(common, validator, orm)

  lazy val assets = Project (id = "assets", base = file("assets"),
    settings = baseSettings ++ Seq(
      name := "skinny-assets",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "ro.isdc.wro4j" %  "rhino"      % "1.7R5-20130223-1",
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    )
  ) dependsOn(framework)

  lazy val task = Project (id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      name := "skinny-task",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-io"             %  "commons-io" % "2.4",
        "org.fusesource.scalamd" %% "scalamd"    % "1.6"
      ) ++ testDependencies
    )
  ) dependsOn(assets, orm)

  lazy val orm = Project (id = "orm", base = file("orm"), 
    settings = baseSettings ++ Seq(
      name := "skinny-orm",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalikejdbcDependencies ++ servletApiDependencies ++ Seq(
        "com.googlecode.flyway" %  "flyway-core"       % "2.3.1"        % "compile",
        "org.hibernate"         %  "hibernate-core"    % "4.3.0.Final"  % "test",
        "com.h2database"        %  "h2"                % H2Version      % "test",
        "ch.qos.logback"        %  "logback-classic"   % "1.0.13"       % "test"
      ) ++ testDependencies
    )
  ) dependsOn(common)

  lazy val freemarker = Project (id = "freemarker", base = file("freemarker"),
    settings = baseSettings ++ Seq(
      name := "skinny-freemarker",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-beanutils" %  "commons-beanutils"  % "1.9.0"   % "compile",
        "org.freemarker"    %  "freemarker"         % "2.3.20"  % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val thymeleaf = Project (id = "thymeleaf", base = file("thymeleaf"),
    settings = baseSettings ++ Seq(
      name := "skinny-thymeleaf",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.thymeleaf"             %  "thymeleaf" % "2.1.2.RELEASE" % "compile",
        "net.sourceforge.nekohtml"  %  "nekohtml"  % "1.9.19"        % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val validator = Project (id = "validator", base = file("validator"),
    settings = baseSettings ++ Seq(
      name := "skinny-validator",
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "com.typesafe" %  "config"       % "1.0.2" % "compile"
      ) ++ jodaDependencies ++ testDependencies
    )
  ) dependsOn(common)

  lazy val test = Project (id = "test", base = file("test"),
   settings = baseSettings ++ Seq(
      name := "skinny-test",
      scalaVersion := "2.10.0",
      libraryDependencies ++= scalatraDependencies ++ testDependencies ++ Seq(
        "org.scalikejdbc" %% "scalikejdbc-test"   % ScalikeJDBCVersion % "compile",
        "org.scalatra"    %% "scalatra-specs2"    % ScalatraVersion    % "compile",
        "org.scalatra"    %% "scalatra-scalatest" % ScalatraVersion    % "compile"
      )
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val example = Project (id = "example", base = file("example"),
    settings = baseSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      name := "skinny-framework-example",
      scalaVersion := "2.10.3",
      libraryDependencies ++= Seq(
        "org.scalatra"       %% "scalatra-specs2"    % ScalatraVersion % "test",
        "org.scalatra"       %% "scalatra-scalatest" % ScalatraVersion % "test",
        "com.h2database"     %  "h2"                 % H2Version,
        "ch.qos.logback"     % "logback-classic"     % "1.0.13"              % "runtime",
        "org.eclipse.jetty"  % "jetty-webapp"        % JettyVersion          % "container",
        "org.eclipse.jetty"  % "jetty-plus"          % JettyVersion          % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
           artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      mainClass := Some("TaskLauncher"),
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") } 
    ) 
  ) dependsOn(framework, assets, thymeleaf, test, task)

  val servletApiDependencies = Seq(
    "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
  )

  val slf4jApiDependencies = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.5" % "compile"
  )

  val scalatraDependencies = Seq(
    "org.scalatra"  %% "scalatra"           % ScalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-scalate"   % ScalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-json"      % ScalatraVersion  % "compile",
    "org.json4s"    %% "json4s-jackson"     % Json4SVersion    % "compile",
    "org.json4s"    %% "json4s-ext"         % Json4SVersion    % "compile",
    "org.scalatra"  %% "scalatra-scalatest" % ScalatraVersion  % "test"
  ) ++ servletApiDependencies ++ slf4jApiDependencies

  val scalikejdbcDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc"               % ScalikeJDBCVersion % "compile",
    "org.scalikejdbc" %% "scalikejdbc-interpolation" % ScalikeJDBCVersion % "compile",
    "org.scalikejdbc" %% "scalikejdbc-config"        % ScalikeJDBCVersion % "compile",
    "org.scalikejdbc" %% "scalikejdbc-test"          % ScalikeJDBCVersion % "test"
  )

  val jodaDependencies = Seq(
    "joda-time" %  "joda-time"    % "2.3"   % "compile",
    "org.joda"  %  "joda-convert" % "1.5"   % "compile"
  )

  // WARNIG: Sufferred strange errors with ScalaTest 1.9.2
  // Could not run test skinny.controller.ParamsSpec: java.lang.IncompatibleClassChangeError: Implementing class
  val testDependencies = Seq(
    "org.scalatest" %% "scalatest"   % "1.9.1" % "test"
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  val _scalacOptions = Seq("-deprecation", "-unchecked", "-feature")

  val _pomExtra = {
    <url>http://skinny-framework.org/</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:skinny-framework/skinny-framework.git</url>
        <connection>scm:git:git@github.com:skinny-framework/skinny-framework.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhuiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
      </developers>
  }

  val _jettyOrbitHack = Seq(
    ivyXML := <dependencies>
      <exclude org="org.eclipse.jetty.orbit" />
    </dependencies>
  )

}
