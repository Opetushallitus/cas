import Bacon from 'baconjs';
import Dispatcher from './dispatcher.js'
import axios from 'axios'
import Promise from 'bluebird'
import {initChangeListeners} from './changeListeners.js'
import {resolveLang, changeLang} from './translations.js'
import {getCookie, setCookie, getTargetService, getConfiguration, getLoginError} from './util'

const ax = Promise.promisifyAll(axios);

const events = {
  changeLang: 'changeLang',
  acceptCookies : 'acceptCookies',
};

const dispatcher = new Dispatcher();
const controller = initChangeListeners(dispatcher, events);
const notificationUrl = '/login-notifications/api/notifications';

export function getController(){
  return controller;
}

const csrf = getCookie("CSRF");
if(csrf) {
  axios.defaults.headers.common["CSRF"]=csrf;
}

axios.defaults.headers.post['Content-Type'] = 'application/json';

export function initAppState() {

  const initialState = {lang: resolveLang(),
                        notices: [],
                        cookiesAccepted: cookiesAccepted(),
                        targetService: getTargetService(),
                        configuration: getConfiguration(),
                        loginError: getLoginError()};

  const notificationsS = Bacon.fromPromise(ax.get(notificationUrl));

  function clearNotices(state){
    return {...state, ["loginError"]: null}
  }
  
  function setLang(state, {lang}){
    changeLang(lang);
    return {...state, ['lang']: lang}
  }

  function onFetchNotices(state, notifications){
    return {...state, ['notices']: notifications.data}
  }

  function acceptCookies(state){
    setCookie("oph-cookies-accepted", true);
    return {...state, ['cookiesAccepted']: true}
  }

  function cookiesAccepted(){
    return getCookie("oph-cookies-accepted");
  }

  return Bacon.update(initialState,
    [dispatcher.stream(events.changeLang)], setLang,
    [dispatcher.stream(events.acceptCookies)], acceptCookies,
    [notificationsS], onFetchNotices)
}
