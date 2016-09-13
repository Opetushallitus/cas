import Bacon from 'baconjs';
import Dispatcher from './dispatcher.js'
import axios from 'axios'
import Promise from 'bluebird'
import {initChangeListeners} from './changeListeners.js'
import {resolveLang, changeLang} from './resources/translations.js'
import configuration from './config'
import {getCookie, setCookie, getTargetService, getBodyParams} from './util'

const ax = Promise.promisifyAll(axios)

const events = {
  changeMode: 'changeMode',
  changeLang: 'changeLang',
  acceptCookies : 'acceptCookies',
  doLogin : 'doLogin',
  requestPassword  : 'requestPassword',
  loginError: 'loginError',
  submitForm: 'submitForm',
}

const dispatcher = new Dispatcher();
const listeners = initChangeListeners(dispatcher, events)

export function changeListeners(){
  return listeners;
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
                        username: '',
                        password: '',
                        targetService: getTargetService(),
                        bodyParams: getBodyParams(),
                        error: {},
                        submitForm: false};

  const configPath = 'api/configuration'

  function toggleMode(state){
    return {...state, ['changingPassword']: !state.changingPassword}
  }
  
  function setLang(state, {field, lang}){
    changeLang(lang)
    return {...state, ['lang']: lang}
  }

  function fetchNotices(){
    console.log("fetching notices");
    return Bacon.fromPromise(ax.get(configuration.noticeUrl))
  }

  function getConfiguration(){
    console.log("fetching configuration");
    return {};
    // return Bacon.fromPromise(ax.get(configPath))
  }

  function onGetConfiguration(state, configuration){
    console.log("Got configuration: "+ JSON.stringify(configuration["data"]))
    return {...state, ['configuration']: configuration["data"]}
  }

  function onFetchNotices(state, notices){
    console.log("Fetched notices: "+ JSON.stringify(notices["data"]))
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
    console.log("Password requested for user "+username)
    ax.post("/authentication-service/resources/salasana/poletti", username)
      .then(response => console.log("Password requested successfully"))
      .catch(error => console.log("Error requesting password"));
    return {...state}
  }

  function loginError(state){
    return {...state, ["error"]: {type: "login", message: "Invalid credentials!"}}
  }

  function clearNotices(state){
    return {...state, ["error"]: {}}
  }

  function login(state, credentials) {
    console.log("login for user "+credentials.username +" password: "+credentials.password)
    // var casAddress = state["configuration"]["cas.address"];
    ax.post("/cas/v1/tickets", "username="+credentials.username+"&password="+credentials.password)
      .then(response => {
        console.log("Login successful, ticket: "+response.headers.location)
        console.log(response.status);
        console.log(response.headers.location);
        listeners.submitForm()})
      .catch(error => {
        console.log("Login failed: "+error.status)
        listeners.loginError();
      });
    return clearNotices({...state})
  }

  function submitForm(state) {
    console.log("SubmitForm called")
    return {...state, ["submitForm"]: true}
  }

  return Bacon.update(initialState,
    [dispatcher.stream(events.changeMode)], toggleMode,
    [dispatcher.stream(events.changeLang)], setLang,
    [dispatcher.stream(events.acceptCookies)], acceptCookies,
    [dispatcher.stream(events.doLogin)], login,
    [dispatcher.stream(events.requestPassword)], requestPassword,
    [dispatcher.stream(events.loginError)], loginError,
    [dispatcher.stream(events.submitForm)], submitForm);
    // [noticeS], onFetchNotices,
}