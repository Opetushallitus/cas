import _ from 'lodash'


export function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

export function setCookie(name, val){
  document.cookie=name+"="+val+"; path=/";
}

export function getUrlParameters(){
  var pairs = location.search.substr(1).split('&').map(item => item.split('='));
  return _.zipObject(pairs)
}

export function getTargetService(){
  var service = getUrlParameters().service;
  if(service && service !== null){
    return service.replace(/j_spring_cas_security_check/g, "").replace(/login\/cas/g, "");
  }
  return "";
}

export function getBodyParams(){
  return {loginTicket: document.body.getAttribute("data-loginTicket"),
          executionKey: document.body.getAttribute("data-executionKey"),
          targetService: document.body.getAttribute("data-targetService"),
          hakaUrl: document.body.getAttribute("data-hakaUrl")}
}