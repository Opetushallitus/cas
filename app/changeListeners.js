export function initChangeListeners(dispatcher, events){

  function modeChange(value){
    // console.log("Mode changed: val: "+value)
    dispatcher.push(events.changeMode, value)
  }

  function changeLang(lang) {
    return () => {
      dispatcher.push(events.changeLang, {lang: lang})
    }
  }

  function acceptCookies(){
    dispatcher.push(events.acceptCookies)
  }

  function doLogin(username, password){
    dispatcher.push(events.doLogin, {username: username, password: password})
  }

  function requestPassword(username){
    dispatcher.push(events.requestPassword, {username: username})
  }

  function loginError() {
    dispatcher.push(events.loginError)
  }

  function submitForm() {
    dispatcher.push(events.submitForm)
  }

  return{
    modeChange : modeChange,
    changeLang : changeLang,
    acceptCookies : acceptCookies,
    doLogin : doLogin,
    requestPassword : requestPassword,
    loginError : loginError,
    submitForm : submitForm
  }
}