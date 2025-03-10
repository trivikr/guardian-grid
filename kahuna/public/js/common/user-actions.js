import angular from 'angular';
import 'angular-cookies';
import template from './user-actions.html';
import '../components/gr-feature-switch-panel/gr-feature-switch-panel';

export const COOKIE_SHOULD_BLUR_GRAPHIC_IMAGES = 'SHOULD_BLUR_GRAPHIC_IMAGES';
const cookieOptions = {domain: `.${window.location.host}`, path: '/'};

export var userActions = angular.module('kahuna.common.userActions', ['gr.featureSwitchPanel', 'ngCookies']);

userActions.controller('userActionCtrl',
    [
        '$cookies', function($cookies) {
            var ctrl = this;

            ctrl.$onInit = () => {
              ctrl.feedbackFormLink = window._clientConfig.feedbackFormLink;
              ctrl.logoutUri = document.querySelector('link[rel="auth-uri"]').href + "logout";
              ctrl.additionalLinks = window._clientConfig.additionalNavigationLinks;
              ctrl.shouldBlurGraphicImages = $cookies.get(COOKIE_SHOULD_BLUR_GRAPHIC_IMAGES) === "true";
              ctrl.syncShouldBlurGraphicImages = () => {
                if (ctrl.shouldBlurGraphicImages){
                  $cookies.put(COOKIE_SHOULD_BLUR_GRAPHIC_IMAGES, "true", cookieOptions);
                } else {
                  $cookies.remove(COOKIE_SHOULD_BLUR_GRAPHIC_IMAGES, cookieOptions);
                }
                window.location.reload();
              };
            };
        }]);

userActions.directive('uiUserActions', [function() {
    return {
        restrict: 'E',
        controller: 'userActionCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template,
        scope: {} // ensure isolated scope
    };
}]);
