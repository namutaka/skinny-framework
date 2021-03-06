package skinny.task.generator

import java.util.Locale
import skinny.{ DBSettings, SkinnyEnv, ParamType }
import scalikejdbc.metadata.Column

/**
 * Reverse Model generator.
 */
object ReverseModelGenerator extends ReverseModelGenerator {
}

trait ReverseModelGenerator extends CodeGenerator {

  def withId: Boolean = true

  def primaryKeyName: String = "id"

  def primaryKeyType: ParamType = ParamType.Long

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-model table_name [className] [skinnyEnv]""")
    println("")
  }

  def run(args: List[String]) {
    val (tableName: String, nameWithPackage: Option[String], skinnyEnv: Option[String]) = args match {
      case tableName :: Nil => (tableName, None, None)
      case tableName :: nameWithPackage :: Nil => (tableName, Some(nameWithPackage), None)
      case tableName :: nameWithPackage :: env :: Nil => (tableName, Some(nameWithPackage), Some(env))
      case _ =>
        showUsage
        return
    }

    System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
    DBSettings.initialize()

    val columns: List[Column] = extractColumns(tableName)

    val hasId: Boolean = columns.filter(_.isPrimaryKey).size == 1
    val pkName: Option[String] = if (hasId) columns.find(_.isPrimaryKey).map(_.name.toLowerCase(Locale.ENGLISH)).map(toCamelCase) else None
    val pkType: Option[ParamType] = if (hasId) columns.find(_.isPrimaryKey).map(_.typeCode).map(convertJdbcSqlTypeToParamType) else None
    val fields: List[String] = if (hasId) {
      columns
        .map(column => toScaffoldFieldDef(column))
        .filter(param => param != "id:Long" && param != "id:Option[Long]")
        .filter(param => !param.startsWith(pkName.get + ":"))
        .map(param => toCamelCase(param))
    } else {
      columns
        .map(column => toScaffoldFieldDef(column))
        .map(param => toCamelCase(param))
    }

    println(if (hasId) {
      s"""
        | *** Skinny Reverse Engineering Task ***
        |
        |  Table     : ${tableName}
        |  ID        : ${pkName.getOrElse("")}:${pkType.getOrElse("")}
        |
        |  Columns:
        |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin
    } else {
      s"""
        | *** Skinny Reverse Engineering Task ***
        |
        |  Table  : ${tableName}
        |
        |  Columns:
        |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin
    })

    val (self, _tableName) = (this, tableName)
    val generator = new ModelGenerator {
      override def withId = hasId
      override def primaryKeyName = pkName.getOrElse(self.primaryKeyName)
      override def primaryKeyType = pkType.getOrElse(self.primaryKeyType)
      override def withTimestamps: Boolean = false
      override def tableName = Some(_tableName)
    }

    generator.run(nameWithPackage.getOrElse(toClassName(tableName)).split("\\.") ++ fields)

  }

}
