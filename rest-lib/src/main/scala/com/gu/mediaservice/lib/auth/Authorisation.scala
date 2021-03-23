package com.gu.mediaservice.lib.auth

import com.gu.mediaservice.lib.argo.ArgoHelpers
import com.gu.mediaservice.lib.auth.Authentication.{MachinePrincipal, Principal, Request, UserPrincipal}
import com.gu.mediaservice.lib.auth.Permissions.{DeleteImage, EditMetadata, PrincipalFilter, UploadImages}
import com.gu.mediaservice.lib.auth.provider.AuthorisationProvider
import play.api.mvc.{ActionFilter, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

class Authorisation(provider: AuthorisationProvider, executionContext: ExecutionContext) extends Results with ArgoHelpers {
  def unauthorized(permission: SimplePermission): Result = respondError(Unauthorized, "permission-denied", s"You do not have permission to ${permission.name}")

  def actionFilterFor(permission: SimplePermission, unauthorisedResult: Result): ActionFilter[Request] = new ActionFilter[Request] {
    override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
      if (hasPermissionTo(permission)(request.user)) {
        Future.successful(None)
      } else {
        Future.successful(Some(unauthorisedResult))
      }
    }
    override protected def executionContext: ExecutionContext = Authorisation.this.executionContext
  }
  def actionFilterFor(permission: SimplePermission): ActionFilter[Request] =
    actionFilterFor(
      permission,
      unauthorized(permission)
    )

  object CommonActionFilters {
    lazy val authorisedForUpload = actionFilterFor(UploadImages)
  }

  private def isUploaderOrHasPermission(
                                         principal: Principal,
                                         uploadedBy: String,
                                         permission: SimplePermission
                                       ) = {
    principal match {
      case user: UserPrincipal =>
        if (user.email.toLowerCase == uploadedBy) {
          true
        } else {
          hasPermissionTo(permission)(principal)
        }
      case MachinePrincipal(ApiAccessor(_, Internal), _) => true
      case _ => false
    }
  }

  def canUserWriteMetadata(principal: Principal, uploadedBy: String): Boolean =
    isUploaderOrHasPermission(principal, uploadedBy, EditMetadata)

  def canUserDeleteImage(principal: Principal, uploadedBy: String): Boolean =
    isUploaderOrHasPermission(principal, uploadedBy, DeleteImage)

  def hasPermissionTo(permission: SimplePermission): PrincipalFilter = {
    principal: Principal => {
      principal match {
        // a machine principal with internal tier can always see anything
        case MachinePrincipal(ApiAccessor(_, Internal), _) => true
        case _ => provider.hasPermissionTo(permission, principal)
      }
    }
  }
}
