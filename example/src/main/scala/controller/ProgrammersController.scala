package controller

import skinny._
import skinny.validator._
import model._

class ProgrammersController extends SkinnyResource {
  protectFromForgery()

  override def skinnyCRUDMapper = Programmer
  override def resourcesName = "programmers"
  override def resourceName = "programmer"

  beforeAction(only = Seq('index, 'new, 'create, 'edit, 'update)) {
    set("companies", Company.findAll())
    set("skills", Skill.findAll())
  }

  override def createForm = validation(
    paramKey("name") is required & maxLength(64),
    paramKey("companyId") is required & numeric
  )
  override def createFormStrongParameters = Seq("name" -> ParamType.String, "companyId" -> ParamType.Long)

  override def updateForm = validation(
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("companyId") is required & numeric
  )
  override def updateFormStrongParameters = Seq("name" -> ParamType.String, "companyId" -> ParamType.Long)

  override def destroyResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    skinnyCRUDMapper.findById(id).map { m =>
      skinnyCRUDMapper.deleteByIdCascade(id)
      status = 200
    } getOrElse haltWithBody(404)
  }

  def addSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
      skillId <- params.getAs[Long]("skillId")
      skill <- Skill.findById(skillId)
    } yield {
      try programmer.addSkill(skill)
      catch { case e: Exception => halt(409) }
    }) getOrElse halt(404)
  }

  def deleteSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
      skillId <- params.getAs[Long]("skillId")
      skill <- Skill.findById(skillId)
    } yield {
      programmer.deleteSkill(skill)
    }) getOrElse haltWithBody(404)
  }

  def joinCompany = {
    (for {
      companyId <- params.getAs[Long]("companyId")
      company <- Company.findById(companyId)
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
    } yield {
      Programmer.withColumns { c =>
        Programmer.updateById(programmerId).withNamedValues(c.companyId -> company.id)
      }
    }) getOrElse haltWithBody(404)
  }

  def leaveCompany = {
    (for {
      companyId <- params.getAs[Long]("companyId")
      company <- Company.findById(companyId)
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
    } yield {
      Programmer.withColumns { c =>
        Programmer.updateById(programmerId).withNamedValues(c.companyId -> None)
      }
    }) getOrElse haltWithBody(404)
  }

}
