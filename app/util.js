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
  return _.fromPairs(pairs)
}

function isEncoded(str){return decodeURIComponent(str) !== str;}

export function getTargetService(){
  var service = getUrlParameters().service;
  if(service && service !== null){

    if(isEncoded(service)){
      return service;
    } else{
      return encodeURIComponent(service.replace(/j_spring_cas_security_check/g, "").replace(/login\/cas/g, ""));
    }
  }
  return "";
}

export function getLoginError(){
  return document.body.getAttribute("data-loginError");
}

export function getConfiguration(){
  return {loginTicket: document.body.getAttribute("data-loginTicket"),
          executionKey: document.body.getAttribute("data-executionKey"),
          hakaUrl: document.body.getAttribute("data-hakaUrl")}
}