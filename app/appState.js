import Bacon from 'baconjs';
import Dispatcher from './dispatcher.js'
import axios from 'axios'
import Promise from 'bluebird'
import {initChangeListeners} from './changeListeners.js'
import {resolveLang, changeLang} from './resources/translations.js'
import configuration from './config'
import {getCookie, setCookie, getTargetService, getBodyParams} from './util'

const ax = Promise.promisifyAll(axios);

const events = {
  changeMode: 'changeMode',
  changeLang: 'changeLang',
  acceptCookies : 'acceptCookies',
  requestPassword  : 'requestPassword',
  passwordReset: "passwordReset"
};

const dispatcher = new Dispatcher();
const controller = initChangeListeners(dispatcher, events);

export function getController(){
  return controller;
}

const csrf = getCookie("CSRF");
if(csrf) {
  axios.defaults.headers.common["CSRF"]=csrf;
}

axios.defaults.headers.post['Content-Type'] = 'application/json';

export function initAppState() {

  const initialState = {changingPassword: false,
                        lang: resolveLang(),
                        notices: [],
                        cookiesAccepted: cookiesAccepted(),
                        targetService: getTargetService(),
                        bodyParams: getBodyParams(),
                        error: getBodyParams().loginError,
                        passwordResetStatus: {reset: false}};

  function clearNotices(state){
    return {...state, ["error"]: null, ["passwordResetStatus"]: {reset: false}}
  }

  function toggleMode(state){
    return clearNotices({...state, ['changingPassword']: !state.changingPassword})
  }
  
  function setLang(state, {field, lang}){
    changeLang(lang);
    return {...state, ['lang']: lang}
  }

  function fetchNotices(){
    return Bacon.fromPromise(ax.get(configuration.noticeUrl))
  }

  function onFetchNotices(state, notices){
    return {...state, ['notices']: notices["data"]}
  }

  function acceptCookies(state){
    setCookie("oph-cookies-accepted", true);
    return {...state, ['cookiesAccepted']: true}
  }

  function cookiesAccepted(){
    return getCookie("oph-cookies-accepted");
  }

  function requestPassword(state, {username}) {
    ax.post("/authentication-service/resources/salasana/poletti", username)
      .then(controller.passwordResetResult(true))
      .catch(e => {controller.passwordResetResult(false)});
    return {...state}
  }

  function onPasswordReset(state, {success}){
    return {...state, ["passwordResetStatus"]: {reset: true, success: success}}
  }

  return Bacon.update(initialState,
    [dispatcher.stream(events.changeMode)], toggleMode,
    [dispatcher.stream(events.changeLang)], setLang,
    [dispatcher.stream(events.acceptCookies)], acceptCookies,
    [dispatcher.stream(events.requestPassword)], requestPassword,
    [dispatcher.stream(events.passwordReset)], onPasswordReset);
}