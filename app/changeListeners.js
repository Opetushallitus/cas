export function initChangeListeners(dispatcher, events){

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

  function loginError() {
    dispatcher.push(events.loginError)
  }

  function submitForm() {
    dispatcher.push(events.submitForm)
  }

  return{
    changeLang : changeLang,
    acceptCookies : acceptCookies,
  }
}