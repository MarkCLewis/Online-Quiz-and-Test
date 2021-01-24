package models

import sys.process._

object ScalaSetup {
  val url = "https://downloads.lightbend.com/scala/2.12.13/scala-2.12.13.tgz"
  val scalaHome = "./scala-2.12.13/bin/"

  /**
    * I'm including this mainly for Heroku, but it also allows me to control what version of Scala I am using on
    * other platforms for testing purposes.
    */
  def downloadScala(): Unit = {
    Seq("wget", url).!
    val tarFile = url.takeRight(url.length - url.lastIndexOf("/") - 1)
    Seq("tar", "xzf", tarFile).!
    Seq("rm", s"$tarFile").!
  }
}