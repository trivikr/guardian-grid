package lib

import controllers.{ExampleSwitch, FeatureSwitch, FeatureSwitchController}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.mvc.Cookie

object NoCookieSwitch extends FeatureSwitch(
  key = "no-cookie-switch",
  title = "A switch with no matching cookie",
  default = false
)

class FeatureSwitchControllerTest extends AnyFreeSpec with Matchers{
  val featureSwitchController = new FeatureSwitchController(List(ExampleSwitch, NoCookieSwitch))
  val exampleSwitchCookie = new Cookie("example-switch", "true");

  val mockCookieRetriever = (key: String) => {
    key match {
      case "feature-switch-example-switch" => Some(exampleSwitchCookie)
      case _ => None
    }
  }

  "getFeatureSwitchCookies" - {
    "should return a list of feature switches alongside an option of matching cookies" in {
      val matches =  featureSwitchController.getFeatureSwitchCookies(mockCookieRetriever)
      matches shouldBe List(
        (ExampleSwitch, Some(exampleSwitchCookie)),
        (NoCookieSwitch, None)
      )
    }
  }

  "getClientSwitchValues" - {
    "should return a map from feature switch to boolean representing cookie values" in {
      val matches = featureSwitchController.getFeatureSwitchCookies(mockCookieRetriever)
      val clientSwitchValues = featureSwitchController.getClientSwitchValues(matches)

      clientSwitchValues shouldBe Map(
        ExampleSwitch -> true,
        NoCookieSwitch -> false,
      )
    }
  }
}
