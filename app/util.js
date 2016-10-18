export function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

export function setCookie(name, val){
  document.cookie=name+"="+val+"; path=/";
}

export function getTargetService(){
  let service = document.body.getAttribute("data-targetService");
  return (service && service.length > 0) ? service :  window.location.origin + '/virkailijan-stp-ui';
}

export function getLoginError(){
  return document.body.getAttribute("data-loginError");
}

export function getConfiguration(){
  return {loginTicket: document.body.getAttribute("data-loginTicket"),
          executionKey: document.body.getAttribute("data-executionKey"),
          hakaUrl: document.body.getAttribute("data-hakaUrl")}
}