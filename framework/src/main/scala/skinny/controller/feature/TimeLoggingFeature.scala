package skinny.controller.feature

import skinny.util.TimeLogging
import org.scalatra.ScalatraContext
import skinny.controller.SkinnyControllerBase

/**
 * Enables time logging.
 */
trait TimeLoggingFeature extends TimeLogging with SensitiveParametersFeature { self: SkinnyControllerBase =>

  def warnElapsedTimeWithRequest[A](millis: Long, additionalLines: Seq[String] = Nil)(action: => A)(
    implicit context: ScalatraContext): A = {
    warnElapsedTime(millis, additionalLines ++ {
      val req = context.request
      val params: Seq[String] = req.parameters.toSeq
        .filterNot(p => sensitiveParameterNames.contains(p._1))
        .map(p => " " + p._1 + ": " + p._2)
      val headers = req.headers.map(h => " " + h._1 + ": " + h._2)
      Seq(
        "",
        s" ${req.getMethod} ${req.getRequestURI}",
        "",
        "--- Request Parameters ---",
        ""
      ) ++ params ++
        Seq(
          "",
          "--- Request Headers ---",
          ""
        ) ++ headers
    })(action)
  }

}
